package per.midas.kurumishell.service

import com.jcraft.jsch.*
import jakarta.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import per.midas.kurumishell.entity.SSHConnection
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeoutException

/**
 * SSH连接池管理类
 * 用于复用SSH连接，避免每次操作都重新建立连接
 */
@Component
class SSHConnectionPool {
    
    private val connectionPool = ConcurrentHashMap<String, PooledConnection>()
    
    // 连接池最大容量
    private val MAX_POOL_SIZE = 20
    
    // 连接获取超时时间（毫秒）
    private val ACQUIRE_TIMEOUT = 10000L
    
    // 信号量，用于控制连接池大小
    private val semaphore = Semaphore(MAX_POOL_SIZE)
    
    /**
     * 初始化定时清理任务
     */
    @PostConstruct
    fun init() {
        println("SSH连接池初始化完成，将定期清理过期连接")
    }
    
    /**
     * 每10分钟执行一次过期连接清理
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    fun scheduledCleanup() {
        try {
            cleanupExpiredConnections()
            println("SSH连接池定时清理完成，当前连接数: ${connectionPool.size}")
        } catch (e: Exception) {
            println("SSH连接池定时清理失败: ${e.message}")
        }
    }
    
    /**
     * 获取SSH会话
     */
    fun getSession(connection: SSHConnection): Session {
        val poolKey = generatePoolKey(connection)
        
        // 尝试获取信号量许可
        if (!semaphore.tryAcquire(ACQUIRE_TIMEOUT, TimeUnit.MILLISECONDS)) {
            throw TimeoutException("获取SSH连接超时，连接池已满")
        }
        
        try {
            return synchronized(connectionPool) {
                val pooledConnection = connectionPool[poolKey]
                
                if (pooledConnection != null && pooledConnection.isValid()) {
                    // 连接有效，直接返回
                    pooledConnection.lastUsed = System.currentTimeMillis()
                    pooledConnection.session
                } else {
                    // 创建新连接
                    val newSession = createNewSession(connection)
                    val newPooledConnection = PooledConnection(newSession)
                    connectionPool[poolKey] = newPooledConnection
                    println("创建新的SSH连接: $poolKey，当前连接数: ${connectionPool.size}")
                    newSession
                }
            }
        } catch (e: Exception) {
            // 如果创建连接失败，释放信号量
            semaphore.release()
            throw e
        }
    }
    
    /**
     * 执行SSH操作（自动管理连接）
     */
    fun <T> executeWithSession(connection: SSHConnection, operation: (Session) -> T): T {
        val session = getSession(connection)
        
        try {
            return operation(session)
        } catch (e: Exception) {
            // 操作失败，移除无效连接
            removeInvalidConnection(connection)
            throw e
        } finally {
            // 无论操作成功还是失败，都释放信号量
            semaphore.release()
        }
    }
    
    /**
     * 关闭指定连接
     */
    fun closeConnection(connection: SSHConnection) {
        val poolKey = generatePoolKey(connection)
        synchronized(connectionPool) {
            connectionPool.remove(poolKey)?.let { pooledConnection ->
                try {
                    if (pooledConnection.session.isConnected) {
                        pooledConnection.session.disconnect()
                    }
                    println("关闭SSH连接: $poolKey")
                    // 释放信号量
                    semaphore.release()
                } catch (e: Exception) {
                    // 忽略关闭异常
                    // 即使关闭失败，也要释放信号量
                    semaphore.release()
                }
            }
        }
    }
    
    /**
     * 清理过期连接（30分钟未使用）
     */
    fun cleanupExpiredConnections() {
        val now = System.currentTimeMillis()
        val expiredKeys = mutableListOf<String>()
        
        connectionPool.forEach { (key, pooledConnection) ->
            if (now - pooledConnection.lastUsed > TimeUnit.MINUTES.toMillis(30)) {
                expiredKeys.add(key)
            }
        }
        
        expiredKeys.forEach { key ->
            synchronized(connectionPool) {
                connectionPool.remove(key)?.let { pooledConnection ->
                    try {
                        if (pooledConnection.session.isConnected) {
                            pooledConnection.session.disconnect()
                        }
                        println("清理过期SSH连接: $key")
                        // 释放信号量
                        semaphore.release()
                    } catch (e: Exception) {
                        // 忽略关闭异常
                        // 即使关闭失败，也要释放信号量
                        semaphore.release()
                    }
                }
            }
        }
    }
    
    /**
     * 获取当前连接池统计信息
     */
    fun getPoolStats(): Map<String, Any> {
        return mapOf(
            "totalConnections" to connectionPool.size,
            "activeConnections" to connectionPool.count { it.value.isValid() },
            "lastCleanupTime" to System.currentTimeMillis()
        )
    }
    
    /**
     * 生成连接池键
     */
    private fun generatePoolKey(connection: SSHConnection): String {
        return "${connection.id}_${connection.host}_${connection.port}_${connection.username}"
    }
    
    /**
     * 创建新的SSH会话
     */
    private fun createNewSession(connection: SSHConnection): Session {
        val jsch = JSch()
        
        // 设置私钥（如果有且不为空字符串）
        val privateKey = connection.privateKey?.trim()
        if (!privateKey.isNullOrBlank()) {
            try {
                val passphrase = connection.keyPassphrase?.trim()
                if (passphrase != null) {
                    jsch.addIdentity("ssh_key", privateKey.toByteArray(), null, passphrase.toByteArray())
                } else {
                    jsch.addIdentity("ssh_key", privateKey.toByteArray(), null, null)
                }
            } catch (e: Exception) {
                println("私钥格式错误: ${e.message}")
                // 继续使用密码认证
            }
        }
        
        val session = jsch.getSession(connection.username, connection.host, connection.port)
        
        // 检查是否有密码
        val password = connection.password?.trim()
        if (!password.isNullOrBlank()) {
            session.setPassword(password)
        }
        
        // 配置SSH参数
        session.setConfig("StrictHostKeyChecking", "no")
        session.setConfig("ConnectTimeout", "10000")
        
        // 设置认证方式优先级
        val hasPrivateKey = !privateKey.isNullOrBlank()
        val hasPassword = !password.isNullOrBlank()
        
        when {
            hasPrivateKey && hasPassword -> {
                session.setConfig("PreferredAuthentications", "publickey,password")
            }
            hasPrivateKey -> {
                session.setConfig("PreferredAuthentications", "publickey")
            }
            hasPassword -> {
                session.setConfig("PreferredAuthentications", "password")
            }
            else -> {
                throw IllegalArgumentException("SSH连接需要密码或私钥认证")
            }
        }
        
        session.connect(30000) // 30秒超时
        return session
    }
    
    /**
     * 移除无效连接
     */
    private fun removeInvalidConnection(connection: SSHConnection) {
        val poolKey = generatePoolKey(connection)
        synchronized(connectionPool) {
            connectionPool.remove(poolKey)?.let { pooledConnection ->
                try {
                    if (pooledConnection.session.isConnected) {
                        pooledConnection.session.disconnect()
                    }
                    println("移除无效SSH连接: $poolKey")
                    // 释放信号量
                    semaphore.release()
                } catch (e: Exception) {
                    // 忽略关闭异常
                    // 即使关闭失败，也要释放信号量
                    semaphore.release()
                }
            }
        }
    }
    
    /**
     * 池化连接对象
     */
    private class PooledConnection(
        val session: Session,
        var lastUsed: Long = System.currentTimeMillis()
    ) {
        fun isValid(): Boolean {
            return session.isConnected
        }
    }
}