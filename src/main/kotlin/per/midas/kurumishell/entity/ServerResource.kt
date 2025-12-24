package per.midas.kurumishell.entity

import java.time.LocalDateTime

data class ServerResource(
    // 基础资源信息
    val cpuUsage: Double,          // CPU使用率百分比
    val memoryUsage: Double,       // 内存使用率百分比
    val memoryTotal: Long,         // 总内存（字节）
    val memoryUsed: Long,          // 已用内存（字节）
    val diskUsage: Double,         // 磁盘使用率百分比
    val diskTotal: Long,           // 总磁盘空间（字节）
    val diskUsed: Long,            // 已用磁盘空间（字节）
    val uptime: Long,              // 服务器运行时间（秒）
    
    // 系统信息
    val hostname: String = "",     // 主机名
    val ipAddress: String = "",    // IP地址
    val osName: String = "",       // 操作系统名称
    val osVersion: String = "",    // 操作系统版本
    val kernelVersion: String = "", // 内核版本
    val architecture: String = "",  // 系统架构
    
    // CPU详细信息
    val cpuModel: String = "",     // CPU型号
    val cpuCores: Int = 0,         // CPU核心数
    val cpuThreads: Int = 0,       // CPU线程数
    val cpuFrequency: String = "", // CPU频率
    
    // 磁盘详细信息
    val diskMounts: List<DiskMount> = emptyList(), // 磁盘挂载点信息
    
    // 网络信息
    val networkInterfaces: List<NetworkInterface> = emptyList(), // 网络接口信息
    
    // 系统负载
    val loadAvg1m: Double = 0.0,   // 1分钟平均负载
    val loadAvg5m: Double = 0.0,   // 5分钟平均负载
    val loadAvg15m: Double = 0.0,  // 15分钟平均负载
    
    // 进程信息
    val totalProcesses: Int = 0,    // 总进程数
    val runningProcesses: Int = 0,  // 运行中进程数
    
    // 交换空间
    val swapTotal: Long = 0,        // 交换空间总大小
    val swapUsed: Long = 0,         // 已用交换空间
    val swapUsage: Double = 0.0,    // 交换空间使用率
    
    // 系统时间
    val systemTime: String = "",    // 系统时间
    
    val timestamp: LocalDateTime = LocalDateTime.now() // 数据采集时间
)

// 磁盘挂载点信息
data class DiskMount(
    val filesystem: String,    // 文件系统
    val size: Long,           // 总大小
    val used: Long,           // 已使用
    val available: Long,      // 可用空间
    val usage: Double,        // 使用率
    val mountPoint: String    // 挂载点
)

// 网络接口信息
data class NetworkInterface(
    val name: String,         // 接口名称
    val ipAddress: String,   // IP地址
    val macAddress: String,   // MAC地址
    val status: String,      // 状态
    val rxBytes: Long,       // 接收字节数
    val txBytes: Long        // 发送字节数
)