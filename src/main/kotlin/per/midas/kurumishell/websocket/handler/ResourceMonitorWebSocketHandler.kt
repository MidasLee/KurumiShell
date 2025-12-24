package per.midas.kurumishell.websocket.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.jcraft.jsch.ChannelExec
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import per.midas.kurumishell.entity.DiskMount
import per.midas.kurumishell.entity.NetworkInterface
import per.midas.kurumishell.entity.ServerResource
import per.midas.kurumishell.entity.SSHConnection
import per.midas.kurumishell.service.SSHConnectionPool
import per.midas.kurumishell.websocket.service.SSHService
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.Executors

@Component
class ResourceMonitorWebSocketHandler(
    private val sshService: SSHService,
    private val objectMapper: ObjectMapper,
    private val sshConnectionPool: SSHConnectionPool
) : TextWebSocketHandler() {

    // 存储活跃的监控会话 (connectionId -> WebSocketSession)
    private val monitorSessions = ConcurrentHashMap<String, WebSocketSession>()
    
    // 存储资源收集任务 (connectionId -> ScheduledFuture)
    private val resourceTasks = ConcurrentHashMap<String, ScheduledFuture<*>>()
    
    // 线程池用于执行资源收集任务
    private val scheduler = Executors.newScheduledThreadPool(5)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        try {
            val connectionId = session.uri?.query?.split("=")?.last()
                ?: throw IllegalArgumentException("Missing connectionId parameter")

            println("Resource monitor WebSocket connected for connection: $connectionId")
            
            // 保存会话
            monitorSessions[connectionId] = session
            
            // 启动资源收集任务（每2秒收集一次）
            val task = scheduler.scheduleAtFixedRate({
                collectAndSendResourceInfo(connectionId)
            }, 0, 2, java.util.concurrent.TimeUnit.SECONDS)
            
            resourceTasks[connectionId] = task
            
        } catch (e: Exception) {
            session.sendMessage(TextMessage("Error: ${e.message}"))
            session.close()
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        try {
            val connectionId = session.uri?.query?.split("=")?.last()
            if (connectionId != null) {
                println("Resource monitor WebSocket closed for connection: $connectionId")
                
                // 移除会话和任务
                monitorSessions.remove(connectionId)
                val task = resourceTasks.remove(connectionId)
                task?.cancel(true)   
            }
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }

    private fun collectAndSendResourceInfo(connectionId: String) {
        try {
            val session = monitorSessions[connectionId]
            if (session != null && session.isOpen) {
                // 获取SSH连接信息
                val connection = sshService.getConnectionById(connectionId)
                    ?: throw RuntimeException("SSH connection not found for id: $connectionId")
                
                // 使用连接池执行资源收集操作
                try {
                    val resource = sshConnectionPool.executeWithSession(connection) { sshSession ->
                        collectServerResources(sshSession)
                    }
                    
                    // 发送资源信息到前端 - 再次检查会话是否打开
                    if (session.isOpen) {
                        val message = objectMapper.writeValueAsString(resource)
                        session.sendMessage(TextMessage(message))
                    }
                } catch (e: Exception) {
                    // 出错时发送错误消息给前端 - 再次检查会话是否打开
                    if (session.isOpen) {
                        session.sendMessage(TextMessage("Error: ${e.message}"))
                    }
                    println("Error collecting resource info for connection $connectionId: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Critical error in resource monitoring: ${e.message}")
        }
    }
    
    /**
     * 收集服务器资源信息（使用已连接的SSH会话）
     */
    private fun collectServerResources(sshSession: com.jcraft.jsch.Session): ServerResource {
        // 1. 收集系统基本信息
        val hostname = try {
            executeCommand(sshSession, "hostname").trim()
        } catch (e: Exception) {
            println("Error getting hostname: ${e.message}")
            "Unknown"
        }
        
        val ipAddress = try {
            executeCommand(sshSession, "hostname -I | awk '{print $1}'").trim()
        } catch (e: Exception) {
            println("Error getting IP address: ${e.message}")
            "Unknown"
        }
        
        // 获取操作系统信息
        val osInfo = try {
            executeCommand(sshSession, "cat /etc/os-release").trim()
        } catch (e: Exception) {
            println("Error getting OS info: ${e.message}")
            ""
        }
        
        var osName = ""
        var osVersion = ""
        if (osInfo.isNotEmpty()) {
            osInfo.lines().forEach { line ->
                when {
                    line.startsWith("PRETTY_NAME=") -> osName = line.substringAfter('"').substringBefore('"')
                    line.startsWith("VERSION_ID=") -> osVersion = line.substringAfter('"').substringBefore('"')
                }
            }
        }
        
        if (osName.isEmpty()) {
            osName = try {
                executeCommand(sshSession, "uname -s").trim()
            } catch (e: Exception) {
                "Unknown"
            }
        }
        
        val kernelVersion = try {
            executeCommand(sshSession, "uname -r").trim()
        } catch (e: Exception) {
            println("Error getting kernel version: ${e.message}")
            "Unknown"
        }
        
        val architecture = try {
            executeCommand(sshSession, "uname -m").trim()
        } catch (e: Exception) {
            println("Error getting architecture: ${e.message}")
            "Unknown"
        }
        
        // 2. 获取CPU详细信息
        val cpuInfo = try {
            executeCommand(sshSession, "cat /proc/cpuinfo").trim()
        } catch (e: Exception) {
            println("Error getting CPU info: ${e.message}")
            ""
        }
        
        var cpuModel = "Unknown"
        var cpuCores = 0
        var cpuThreads = 0
        var cpuFrequency = "Unknown"
        
        if (cpuInfo.isNotEmpty()) {
            val processorLines = cpuInfo.lines()
            for (line in processorLines) {
                when {
                    line.startsWith("model name") -> cpuModel = line.substringAfter(':').trim()
                    line.startsWith("cpu cores") -> cpuCores = line.substringAfter(':').trim().toIntOrNull() ?: 0
                    line.startsWith("siblings") -> cpuThreads = line.substringAfter(':').trim().toIntOrNull() ?: 0
                    line.startsWith("cpu MHz") -> cpuFrequency = "${line.substringAfter(':').trim().toDoubleOrNull() ?: 0.0} MHz"
                }
            }
            
            // 如果没有获取到线程数，计算物理核心数
            if (cpuThreads == 0) {
                cpuThreads = processorLines.count { it.startsWith("processor") }
                if (cpuCores == 0) {
                    cpuCores = Runtime.getRuntime().availableProcessors()
                }
            }
        }
        
        // 3. 获取CPU使用率
        var cpuUsage = 0.0
        try {
            val cpuOutput = executeCommand(sshSession, "vmstat 1 2 | tail -1").trim()
            if (cpuOutput.isNotEmpty()) {
                val parts = cpuOutput.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                if (parts.size >= 13) {
                    val idleCpu = parts[12].toDouble()
                    cpuUsage = 100.0 - idleCpu
                } else if (parts.size >= 10) {
                    val idleCpu = parts[9].toDouble()
                    cpuUsage = 100.0 - idleCpu
                }
            }
        } catch (e: Exception) {
            println("Error getting CPU usage: ${e.message}")
        }
        
        // 4. 获取内存信息
        var memoryTotal = 0L
        var memoryUsed = 0L
        var swapTotal = 0L
        var swapUsed = 0L
        
        try {
            val memOutput = executeCommand(sshSession, "cat /proc/meminfo").trim()
            if (memOutput.isNotEmpty()) {
                memOutput.lines().forEach { line ->
                    when {
                        line.startsWith("MemTotal:") -> memoryTotal = line.substringAfter(':').trim().split("\\s+".toRegex())[0].toLong() * 1024
                        line.startsWith("MemFree:") -> {
                            val freeMem = line.substringAfter(':').trim().split("\\s+".toRegex())[0].toLong() * 1024
                            memoryUsed = memoryTotal - freeMem
                        }
                        line.startsWith("SwapTotal:") -> swapTotal = line.substringAfter(':').trim().split("\\s+".toRegex())[0].toLong() * 1024
                        line.startsWith("SwapFree:") -> {
                            val freeSwap = line.substringAfter(':').trim().split("\\s+".toRegex())[0].toLong() * 1024
                            swapUsed = swapTotal - freeSwap
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error getting memory info: ${e.message}")
        }
        
        val memoryUsage = if (memoryTotal > 0) (memoryUsed.toDouble() / memoryTotal.toDouble()) * 100 else 0.0
        val swapUsage = if (swapTotal > 0) (swapUsed.toDouble() / swapTotal.toDouble()) * 100 else 0.0
        
        // 5. 获取磁盘挂载点信息
        val diskMounts = mutableListOf<DiskMount>()
        try {
            val diskOutput = executeCommand(sshSession, "df -k").trim()
            if (diskOutput.isNotEmpty()) {
                val lines = diskOutput.lines().drop(1) // 跳过标题行
                for (line in lines) {
                    if (line.isNotBlank()) {
                        val parts = line.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                        if (parts.size >= 6) {
                            val filesystem = parts[0]
                            val size = parts[1].toLong() * 1024
                            val used = parts[2].toLong() * 1024
                            val available = parts[3].toLong() * 1024
                            val usage = parts[4].replace("%", "").toDouble()
                            val mountPoint = parts[5]
                            
                            diskMounts.add(DiskMount(filesystem, size, used, available, usage, mountPoint))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error getting disk mounts: ${e.message}")
        }
        
        // 计算根目录使用情况（兼容旧逻辑）
        var diskTotal = 0L
        var diskUsed = 0L
        val rootMount = diskMounts.firstOrNull { it.mountPoint == "/" }
        if (rootMount != null) {
            diskTotal = rootMount.size
            diskUsed = rootMount.used
        }
        val diskUsage = if (diskTotal > 0) (diskUsed.toDouble() / diskTotal.toDouble()) * 100 else 0.0
        
        // 6. 获取网络接口信息
        val networkInterfaces = mutableListOf<NetworkInterface>()
        try {
            val netOutput = executeCommand(sshSession, "ip -o addr show").trim()
            if (netOutput.isNotEmpty()) {
                netOutput.lines().forEach { line ->
                    val parts = line.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                    if (parts.size >= 6) {
                        val interfaceName = parts[1]
                        val ipAddress = parts[4].substringBefore("/")
                        
                        // 获取MAC地址
                        val macAddress = try {
                            executeCommand(sshSession, "cat /sys/class/net/$interfaceName/address").trim()
                        } catch (e: Exception) {
                            "Unknown"
                        }
                        
                        // 获取接口状态
                        val status = try {
                            executeCommand(sshSession, "cat /sys/class/net/$interfaceName/operstate").trim()
                        } catch (e: Exception) {
                            "Unknown"
                        }
                        
                        networkInterfaces.add(NetworkInterface(
                            name = interfaceName,
                            ipAddress = ipAddress,
                            macAddress = macAddress,
                            status = status,
                            rxBytes = 0L, // 需要额外的命令获取
                            txBytes = 0L  // 需要额外的命令获取
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            println("Error getting network interfaces: ${e.message}")
        }
        
        // 7. 获取系统负载
        var loadAvg1m = 0.0
        var loadAvg5m = 0.0
        var loadAvg15m = 0.0
        
        try {
            val loadAvgOutput = executeCommand(sshSession, "cat /proc/loadavg").trim()
            if (loadAvgOutput.isNotEmpty()) {
                val parts = loadAvgOutput.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                if (parts.size >= 3) {
                    loadAvg1m = parts[0].toDoubleOrNull() ?: 0.0
                    loadAvg5m = parts[1].toDoubleOrNull() ?: 0.0
                    loadAvg15m = parts[2].toDoubleOrNull() ?: 0.0
                }
            }
        } catch (e: Exception) {
            println("Error getting load average: ${e.message}")
        }
        
        // 8. 获取进程信息
        var totalProcesses = 0
        var runningProcesses = 0
        
        try {
            val processOutput = executeCommand(sshSession, "ps aux").trim()
            if (processOutput.isNotEmpty()) {
                val lines = processOutput.lines()
                totalProcesses = lines.size - 1 // 减去标题行
                runningProcesses = lines.count { it.contains("R+") || it.contains("R ") } // 运行中的进程
            }
        } catch (e: Exception) {
            println("Error getting process info: ${e.message}")
        }
        
        // 9. 获取服务器运行时间
        var uptime = 0L
        try {
            val uptimeOutput = executeCommand(sshSession, "cat /proc/uptime").trim()
            if (uptimeOutput.isNotEmpty()) {
                val parts = uptimeOutput.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                if (parts.size >= 1) {
                    uptime = parts[0].toDouble().toLong()
                }
            }
        } catch (e: Exception) {
            println("Error getting uptime: ${e.message}")
        }
        
        // 10. 获取系统时间
        val systemTime = try {
            executeCommand(sshSession, "date '+%Y-%m-%d %H:%M:%S'").trim()
        } catch (e: Exception) {
            println("Error getting system time: ${e.message}")
            "Unknown"
        }
        
        return ServerResource(
            cpuUsage = cpuUsage,
            memoryUsage = memoryUsage,
            memoryTotal = memoryTotal,
            memoryUsed = memoryUsed,
            diskUsage = diskUsage,
            diskTotal = diskTotal,
            diskUsed = diskUsed,
            uptime = uptime,
            hostname = hostname,
            ipAddress = ipAddress,
            osName = osName,
            osVersion = osVersion,
            kernelVersion = kernelVersion,
            architecture = architecture,
            cpuModel = cpuModel,
            cpuCores = cpuCores,
            cpuThreads = cpuThreads,
            cpuFrequency = cpuFrequency,
            diskMounts = diskMounts,
            networkInterfaces = networkInterfaces,
            loadAvg1m = loadAvg1m,
            loadAvg5m = loadAvg5m,
            loadAvg15m = loadAvg15m,
            totalProcesses = totalProcesses,
            runningProcesses = runningProcesses,
            swapTotal = swapTotal,
            swapUsed = swapUsed,
            swapUsage = swapUsage,
            systemTime = systemTime
        )
    }
    
    /**
     * 执行SSH命令（使用已连接的SSH会话）
     */
    private fun executeCommand(session: com.jcraft.jsch.Session, command: String): String {
        // 使用bash -c包装命令，确保命令正确执行
        val wrappedCommand = "bash -c '$command'"
        
        val channel = session.openChannel("exec") as ChannelExec
        channel.setCommand(wrappedCommand)
        
        try {
            // 连接通道
            channel.connect(5000)
            
            // 获取输入流和错误流
            val inputStream = channel.inputStream
            val errorInputStream = channel.errStream
            
            // 读取标准输出
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            
            // 读取错误输出
            val errorOutput = ByteArrayOutputStream()
            while (errorInputStream.read(buffer).also { bytesRead = it } != -1) {
                errorOutput.write(buffer, 0, bytesRead)
            }
            
            // 等待命令执行完成
            while (!channel.isClosed) {
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
            
            // 关闭流
            inputStream.close()
            errorInputStream.close()
            
            // 检查错误输出
            val errorString = errorOutput.toString(StandardCharsets.UTF_8).trim()
            if (errorString.isNotEmpty()) {
                println("Command error output for '$command': $errorString")
            }
            
            // 检查退出状态
            val exitStatus = channel.exitStatus
            if (exitStatus != 0) {
                println("Warning: Command '$command' completed with exit status $exitStatus")
            }
            
            val resultOutput = output.toString(StandardCharsets.UTF_8).trim()
            return resultOutput
        } finally {
            if (channel.isConnected) {
                channel.disconnect()
            }
        }
    }
}