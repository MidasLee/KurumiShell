package per.midas.kurumishell.websocket.service

import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import per.midas.kurumishell.repository.SSHConnectionRepository
import per.midas.kurumishell.entity.SSHConnection
import java.io.IOException
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.ConcurrentHashMap

@Service
class SSHService(
    private val sshConnectionRepository: SSHConnectionRepository
) {
    // 存储活跃的SSH会话 (WebSocketSession.id -> SSH资源)
    private val activeSessions = ConcurrentHashMap<String, SSHSessionResources>()

    data class SSHSessionResources(
        val jschSession: Session,
        val channel: ChannelShell,
        val pipedIn: PipedInputStream,
        val pipedOut: PipedOutputStream
    )

    fun connectToSSH(wsSession: WebSocketSession, connectionId: String) {
        try {
            val connection = sshConnectionRepository.findById(connectionId)
                .orElseThrow { RuntimeException("SSH Connection not found with id: $connectionId") }

            val jsch = JSch()
            
            // 配置JSch以支持更多的密钥格式
            JSch.setConfig("PubkeyAcceptedKeyTypes", "ssh-rsa,rsa-sha2-512,rsa-sha2-256,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521,ssh-ed25519")
            JSch.setConfig("PreferredAuthentications", "publickey,password")

            // 配置Session
            val jschSession = jsch.getSession(connection.username, connection.host, connection.port).apply {
                setConfig("StrictHostKeyChecking", "no")

                // 优先使用密码认证（如果存在）
                if (connection.password != null) {
                    val password = connection.password
                    if (password != null) {
                        this.setPassword(password)
                        JSch.setConfig("PreferredAuthentications", "password")
                        println("Using password authentication for user: ${connection.username}")
                    }
                } 
                // 密码不存在时，尝试使用私钥认证
                else if (connection.privateKey != null) {
                    val privateKey = connection.privateKey
                    if (privateKey != null) {
                        jsch.addIdentity("id_rsa", privateKey.toByteArray(), null, connection.keyPassphrase?.toByteArray())
                        JSch.setConfig("PreferredAuthentications", "publickey")
                        println("Using private key authentication for user: ${connection.username}")
                    }
                }
                // 两种认证方式都不存在时，抛出异常
                else {
                    throw RuntimeException("No authentication method provided: neither password nor private key available")
                }

                timeout = 60000
            }

            jschSession.connect()

            val channel = jschSession.openChannel("shell") as ChannelShell
            channel.setPtyType("xterm-256color")
            channel.setAgentForwarding(true)

            val pipedIn = PipedInputStream()
            val pipedOut = PipedOutputStream(pipedIn)

            channel.inputStream = pipedIn
            channel.outputStream = object : PipedOutputStream() {
                override fun write(b: Int) {
                    try {
                        if (wsSession.isOpen) {
                            wsSession.sendMessage(TextMessage(b.toChar().toString()))
                        }
                    } catch (e: IOException) {
                        cleanupResources(wsSession.id)
                    }
                }

                override fun write(b: ByteArray, off: Int, len: Int) {
                    try {
                        if (wsSession.isOpen) {
                            wsSession.sendMessage(TextMessage(String(b, off, len)))
                        }
                    } catch (e: IOException) {
                        cleanupResources(wsSession.id)
                    }
                }
            }

            channel.connect()

            activeSessions[wsSession.id] = SSHSessionResources(
                jschSession = jschSession,
                channel = channel,
                pipedIn = pipedIn,
                pipedOut = pipedOut
            )

        } catch (e: JSchException) {
            sendErrorMessage(wsSession, "SSH Connection Failed: ${e.message}")
        } catch (e: Exception) {
            sendErrorMessage(wsSession, "Error: ${e.message}")
        }
    }

    fun handleClientMessage(sessionId: String, message: String) {
        try {
            // 尝试解析JSON消息，检查是否为调整大小的命令
            if (message.startsWith("{")) {
                try {
                    val json = ObjectMapper().readTree(message)
                    if (json.has("type") && json.get("type").asText() == "resize") {
                        val cols = json.get("cols").asInt()
                        val rows = json.get("rows").asInt()
                        resizeTerminal(sessionId, cols, rows)
                        return
                    }
                } catch (e: Exception) {
                    // 不是有效的JSON或不是resize命令，作为普通文本处理
                }
            }

            // 普通文本消息，直接写入SSH通道
            activeSessions[sessionId]?.let { resources ->
                resources.pipedOut.write(message.toByteArray())
                resources.pipedOut.flush()
            }
        } catch (e: IOException) {
            cleanupResources(sessionId)
        }
    }

    fun resizeTerminal(sessionId: String, cols: Int, rows: Int) {
        activeSessions[sessionId]?.channel?.setPtySize(cols, rows, cols * 8, rows * 8)
    }

    fun cleanupResources(sessionId: String) {
        activeSessions[sessionId]?.let { resources ->
            try {
                resources.channel.disconnect()
            } catch (e: Exception) { /* Ignore */
            }
            try {
                resources.jschSession.disconnect()
            } catch (e: Exception) { /* Ignore */
            }
            try {
                resources.pipedIn.close()
                resources.pipedOut.close()
            } catch (e: Exception) { /* Ignore */
            }
            activeSessions.remove(sessionId)
        }
    }

    private fun sendErrorMessage(session: WebSocketSession, message: String) {
        try {
            if (session.isOpen) {
                session.sendMessage(TextMessage(message))
                session.close()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun getActiveConnections(): Map<String, String> {
        return activeSessions.mapValues {
            "${it.value.jschSession.host}:${it.value.jschSession.port}"
        }
    }
    
    // 获取SSH连接信息用于资源监控
    fun getConnectionById(connectionId: String): SSHConnection {
        return sshConnectionRepository.findById(connectionId)
            .orElseThrow { RuntimeException("SSH Connection not found with id: $connectionId") }
    }
}