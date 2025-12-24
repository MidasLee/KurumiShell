// 服务器资源监控数据接口定义

export interface DiskMount {
    filesystem: string    // 文件系统
    size: number          // 总大小
    used: number          // 已使用
    available: number     // 可用空间
    usage: number         // 使用率
    mountPoint: string    // 挂载点
}

export interface NetworkInterface {
    name: string         // 接口名称
    ipAddress: string   // IP地址
    macAddress: string   // MAC地址
    status: string      // 状态
    rxBytes: number     // 接收字节数
    txBytes: number     // 发送字节数
}

export interface ServerResource {
    // 基础资源信息
    cpuUsage: number          // CPU使用率百分比
    memoryUsage: number       // 内存使用率百分比
    memoryTotal: number       // 总内存（字节）
    memoryUsed: number        // 已用内存（字节）
    diskUsage: number        // 磁盘使用率百分比
    diskTotal: number         // 总磁盘空间（字节）
    diskUsed: number         // 已用磁盘空间（字节）
    uptime: number           // 服务器运行时间（秒）
    
    // 系统信息
    hostname: string          // 主机名
    ipAddress: string        // IP地址
    osName: string            // 操作系统名称
    osVersion: string         // 操作系统版本
    kernelVersion: string     // 内核版本
    architecture: string      // 系统架构
    
    // CPU详细信息
    cpuModel: string         // CPU型号
    cpuCores: number         // CPU核心数
    cpuThreads: number       // CPU线程数
    cpuFrequency: string     // CPU频率
    
    // 磁盘详细信息
    diskMounts: DiskMount[]  // 磁盘挂载点信息
    
    // 网络信息
    networkInterfaces: NetworkInterface[] // 网络接口信息
    
    // 系统负载
    loadAvg1m: number        // 1分钟平均负载
    loadAvg5m: number        // 5分钟平均负载
    loadAvg15m: number       // 15分钟平均负载
    
    // 进程信息
    totalProcesses: number    // 总进程数
    runningProcesses: number  // 运行中进程数
    
    // 交换空间
    swapTotal: number        // 交换空间总大小
    swapUsed: number         // 已用交换空间
    swapUsage: number        // 交换空间使用率
    
    // 系统时间
    systemTime: string       // 系统时间
    
    timestamp: string        // 数据采集时间
}