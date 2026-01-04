<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { TimeOutline, DesktopOutline, GlobeOutline } from '@vicons/ionicons5'
import { NProgress, NSpin, NEmpty, NIcon, NGrid, NGi, NCard } from 'naive-ui'
import { getEnv } from '@/utils/env.ts'
import type { DiskMount, NetworkInterface } from '@/dto/ServerResource.ts'
import { buildWsUrl } from '@/utils/env.ts'

const env = getEnv()

interface Props {
  connectionId?: string
}

const props = withDefaults(defineProps<Props>(), {
  connectionId: undefined
})

// 状态变量
const loading = ref(true)
const hasActiveConnection = ref(false)
const cpuUsage = ref(0)
const memoryUsage = ref(0)
const memoryTotal = ref(0)
const memoryUsed = ref(0)
const diskUsage = ref(0)
const diskTotal = ref(0)
const diskUsed = ref(0)
const uptime = ref(0)
const timestamp = ref(new Date())

// 新增系统信息
const hostname = ref('')
const ipAddress = ref('')
const osName = ref('')
const osVersion = ref('')
const kernelVersion = ref('')
const architecture = ref('')

// CPU详细信息
const cpuModel = ref('')
const cpuCores = ref(0)
const cpuThreads = ref(0)
const cpuFrequency = ref('')

// 磁盘挂载点
const diskMounts = ref<DiskMount[]>([])

// 网络接口
const networkInterfaces = ref<NetworkInterface[]>([])

// 系统负载
const loadAvg1m = ref(0)
const loadAvg5m = ref(0)
const loadAvg15m = ref(0)

// 进程信息
const totalProcesses = ref(0)
const runningProcesses = ref(0)

// 交换空间
const swapTotal = ref(0)
const swapUsed = ref(0)
const swapUsage = ref(0)

// 系统时间
const systemTime = ref('')

// WebSocket连接
const webSocket = ref<WebSocket | null>(null)

// 连接WebSocket
const connectWebSocket = () => {
  if (!props.connectionId) {
    hasActiveConnection.value = false
    return
  }

  // 关闭现有连接
  if (webSocket.value) {
    try {
      webSocket.value.close()
    } catch (e) {
      console.warn('关闭旧WebSocket连接时出错:', e)
    }
  }

  // 创建新连接
  const wsUrl = buildWsUrl('resources', props.connectionId)

  try {
    webSocket.value = new WebSocket(wsUrl)

    webSocket.value.onopen = () => {
      console.log('资源监控WebSocket连接成功')
      hasActiveConnection.value = true

    }

    webSocket.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        updateResourceData(data)
        loading.value = false
      } catch (error) {
        console.error('解析资源数据失败:', error)
      }
    }

    webSocket.value.onclose = () => {
      console.log('资源监控WebSocket连接关闭')
      hasActiveConnection.value = false
      loading.value = false
    }

    webSocket.value.onerror = (error) => {
      console.error('资源监控WebSocket错误:', error)
      hasActiveConnection.value = false
      loading.value = false
    }
  } catch (error) {
    console.error('创建资源监控WebSocket连接失败:', error)
    loading.value = false
    hasActiveConnection.value = false
  }
}

// 更新资源数据
const updateResourceData = (data: any) => {
  cpuUsage.value = Number(data.cpuUsage) || 0
  memoryUsage.value = Number(data.memoryUsage) || 0
  memoryTotal.value = Number(data.memoryTotal) || 0
  memoryUsed.value = Number(data.memoryUsed) || 0
  diskUsage.value = Number(data.diskUsage) || 0
  diskTotal.value = Number(data.diskTotal) || 0
  diskUsed.value = Number(data.diskUsed) || 0
  uptime.value = Number(data.uptime) || 0
  timestamp.value = data.timestamp ? new Date(data.timestamp) : new Date()

  // 更新系统信息
  hostname.value = data.hostname || ''
  ipAddress.value = data.ipAddress || ''
  osName.value = data.osName || ''
  osVersion.value = data.osVersion || ''
  kernelVersion.value = data.kernelVersion || ''
  architecture.value = data.architecture || ''

  // 更新CPU信息
  cpuModel.value = data.cpuModel || ''
  cpuCores.value = Number(data.cpuCores) || 0
  cpuThreads.value = Number(data.cpuThreads) || 0
  cpuFrequency.value = data.cpuFrequency || ''

  // 更新磁盘挂载点
  diskMounts.value = data.diskMounts || []

  // 更新网络接口
  networkInterfaces.value = data.networkInterfaces || []

  // 更新系统负载
  loadAvg1m.value = Number(data.loadAvg1m) || 0
  loadAvg5m.value = Number(data.loadAvg5m) || 0
  loadAvg15m.value = Number(data.loadAvg15m) || 0

  // 更新进程信息
  totalProcesses.value = Number(data.totalProcesses) || 0
  runningProcesses.value = Number(data.runningProcesses) || 0

  // 更新交换空间
  swapTotal.value = Number(data.swapTotal) || 0
  swapUsed.value = Number(data.swapUsed) || 0
  swapUsage.value = Number(data.swapUsage) || 0

  // 更新系统时间
  systemTime.value = data.systemTime || ''
}

// 根据使用率获取进度条颜色
const getProgressColor = (percentage: number): string => {
  if (percentage < 50) return '#52c41a'
  if (percentage < 80) return '#faad14'
  return '#ff4d4f'
}

// 格式化字节数
const formatBytes = (bytes: number): string => {
  if (bytes === 0) return '0 B'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化运行时间
const formatUptime = (seconds: number): string => {
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  let result = ''
  if (days > 0) result += `${days}天 `
  if (hours > 0) result += `${hours}小时 `
  if (minutes > 0) result += `${minutes}分钟`

  return result || '少于1分钟'
}

// 格式化日期时间
const formatDateTime = (date: Date): string => {
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 监听连接ID变化
watch(() => props.connectionId, () => {
  connectWebSocket()
})

// 组件挂载时连接WebSocket
onMounted(() => {
  connectWebSocket()
})

// 组件卸载时关闭WebSocket连接
onUnmounted(() => {
  if (webSocket.value) {
    try {
      webSocket.value.close()
    } catch (e) {
      console.warn('关闭WebSocket连接时出错:', e)
    }
  }
})
</script>

<template>
  <div class="resource-monitor-container">
    <div v-if="loading" class="loading">
      <n-spin :show="loading" description="正在获取资源信息..." />
    </div>
    <div v-else-if="!hasActiveConnection" class="empty-state">
      <n-empty description="请先连接到服务器" />
    </div>
    <div v-else class="resource-content">
      <!-- 服务器基本信息 -->
      <n-grid :cols="2" :x-gap="12" :y-gap="12">
        <n-gi>
          <n-card title="服务器信息" size="small">
            <div class="info-list">
              <div class="info-item">
                <n-icon :component="DesktopOutline" />
                <span class="info-label">主机名:</span>
                <span class="info-value">{{ hostname || 'Unknown' }}</span>
              </div>
              <div class="info-item">
                <n-icon :component="GlobeOutline" />
                <span class="info-label">IP地址:</span>
                <span class="info-value">{{ ipAddress || 'Unknown' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">操作系统:</span>
                <span class="info-value">{{ osName }} {{ osVersion }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">内核版本:</span>
                <span class="info-value">{{ kernelVersion || 'Unknown' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">系统架构:</span>
                <span class="info-value">{{ architecture || 'Unknown' }}</span>
              </div>
              <div class="info-item">
                <n-icon :component="TimeOutline" />
                <span class="info-label">运行时间:</span>
                <span class="info-value">{{ formatUptime(uptime) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">系统时间:</span>
                <span class="info-value">{{ systemTime || 'Unknown' }}</span>
              </div>
            </div>
          </n-card>
        </n-gi>

        <n-gi>
          <n-card title="CPU信息" size="small">
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">型号:</span>
                <span class="info-value">{{ cpuModel || 'Unknown' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">核心数:</span>
                <span class="info-value">{{ cpuCores }}核 {{ cpuThreads }}线程</span>
              </div>
              <div class="info-item">
                <span class="info-label">频率:</span>
                <span class="info-value">{{ cpuFrequency || 'Unknown' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">使用率:</span>
                <span class="info-value">{{ cpuUsage.toFixed(1) }}%</span>
              </div>
              <div class="progress-item">
                <n-progress :percentage="cpuUsage" :indicator-placement="'inside'" :color="getProgressColor(cpuUsage)"
                  :height="6" />
              </div>
            </div>
          </n-card>
        </n-gi>
      </n-grid>

      <!-- 内存和交换空间 -->
      <n-grid :cols="2" :x-gap="12" :y-gap="12">
        <n-gi>
          <n-card title="内存使用情况" size="small">
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">使用率:</span>
                <span class="info-value">{{ memoryUsage.toFixed(1) }}%</span>
              </div>
              <div class="progress-item">
                <n-progress :percentage="memoryUsage" :indicator-placement="'inside'"
                  :color="getProgressColor(memoryUsage)" :height="6" />
              </div>
              <div class="info-item">
                <span class="info-label">已使用:</span>
                <span class="info-value">{{ formatBytes(memoryUsed) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">总内存:</span>
                <span class="info-value">{{ formatBytes(memoryTotal) }}</span>
              </div>
            </div>
          </n-card>
        </n-gi>

        <n-gi>
          <n-card title="交换空间" size="small">
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">使用率:</span>
                <span class="info-value">{{ swapUsage.toFixed(1) }}%</span>
              </div>
              <div class="progress-item">
                <n-progress :percentage="swapUsage" :indicator-placement="'inside'" :color="getProgressColor(swapUsage)"
                  :height="6" />
              </div>
              <div class="info-item">
                <span class="info-label">已使用:</span>
                <span class="info-value">{{ formatBytes(swapUsed) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">总交换:</span>
                <span class="info-value">{{ formatBytes(swapTotal) }}</span>
              </div>
            </div>
          </n-card>
        </n-gi>
      </n-grid>

      <!-- 系统负载和进程 -->
      <n-grid :cols="2" :x-gap="12" :y-gap="12">
        <n-gi>
          <n-card title="系统负载" size="small">
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">1分钟平均:</span>
                <span class="info-value">{{ loadAvg1m.toFixed(2) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">5分钟平均:</span>
                <span class="info-value">{{ loadAvg5m.toFixed(2) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">15分钟平均:</span>
                <span class="info-value">{{ loadAvg15m.toFixed(2) }}</span>
              </div>
            </div>
          </n-card>
        </n-gi>

        <n-gi>
          <n-card title="进程信息" size="small">
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">总进程数:</span>
                <span class="info-value">{{ totalProcesses }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">运行中进程:</span>
                <span class="info-value">{{ runningProcesses }}</span>
              </div>
            </div>
          </n-card>
        </n-gi>
      </n-grid>

      <!-- 磁盘使用情况 -->
      <n-card title="磁盘使用情况" size="small" v-if="diskMounts && diskMounts.length > 0">
        <div class="disk-mounts">
          <div v-for="mount in diskMounts" :key="mount.mountPoint" class="mount-item">
            <div class="mount-header">
              <span class="mount-point">{{ mount.mountPoint }}</span>
              <span class="mount-usage">{{ mount.usage.toFixed(1) }}%</span>
            </div>
            <n-progress :percentage="mount.usage" :indicator-placement="'inside'" :color="getProgressColor(mount.usage)"
              :height="6" />
            <div class="mount-details">
              <small>{{ formatBytes(mount.used) }} / {{ formatBytes(mount.size) }} ({{ formatBytes(mount.available) }}
                可用)</small>
              <small>文件系统: {{ mount.filesystem }}</small>
            </div>
          </div>
        </div>
      </n-card>

      <!-- 网络接口 -->
      <n-card title="网络接口" size="small" v-if="networkInterfaces && networkInterfaces.length > 0">
        <div class="network-interfaces">
          <div v-for="iface in networkInterfaces" :key="iface.name" class="interface-item">
            <div class="interface-header">
              <span class="interface-name">{{ iface.name }}</span>
              <span :class="['interface-status', iface.status === 'up' ? 'status-up' : 'status-down']">
                {{ iface.status === 'up' ? '启用' : '禁用' }}
              </span>
            </div>
            <div class="interface-details">
              <div>IP地址: {{ iface.ipAddress || 'N/A' }}</div>
              <div>MAC地址: {{ iface.macAddress || 'Unknown' }}</div>
            </div>
          </div>
        </div>
      </n-card>

      <!-- 最后更新时间 -->
      <div class="last-update">
        <small>最后更新: {{ formatDateTime(timestamp) }}</small>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>