<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Terminal } from '@xterm/xterm'
import { FitAddon } from 'xterm-addon-fit'
import '@xterm/xterm/css/xterm.css'
import { getEnv } from '@/utils/env'
import { buildWsUrl } from '@/utils/env.ts'

const env = getEnv()

interface Props {
    name: string
    visible: boolean
    connectionId?: string
}

const props = withDefaults(defineProps<Props>(), {
    visible: false,
    connectionId: undefined
})

const containerRef = ref<HTMLElement | null>(null)
const terminal = ref<Terminal | null>(null)
const fitAddon = ref<FitAddon | null>(null)
const isInitialized = ref(false)
const webSocket = ref<WebSocket | null>(null)
const isConnected = ref(false)

// 存储终端状态
const terminalState = ref<{
    buffer: string
    cursorX: number
    cursorY: number
}>({
    buffer: '',
    cursorX: 0,
    cursorY: 0
})

// 保存终端状态
const saveTerminalState = () => {
    if (!terminal.value) return

    try {
        const buffer = terminal.value.buffer.active
        let content = ''
        for (let i = 0; i < buffer.length; i++) {
            const line = buffer.getLine(i)
            if (line) {
                content += line.translateToString(true) + '\r\n'
            }
        }
        terminalState.value.buffer = content
        terminalState.value.cursorX = terminal.value.buffer.active.cursorX
        terminalState.value.cursorY = terminal.value.buffer.active.cursorY

        console.debug(`终端 ${props.name} 状态已保存`)
    } catch (error) {
        console.warn('保存终端状态失败:', error)
    }
}

// 恢复终端状态
const restoreTerminalState = () => {
    if (!terminal.value || !terminalState.value.buffer) return

    try {
        terminal.value.write('\r\n')
        terminal.value.write(terminalState.value.buffer)
        console.debug(`终端 ${props.name} 状态已恢复`)
    } catch (error) {
        console.warn('恢复终端状态失败:', error)
    }
}

// 安全清理资源
const safeDispose = () => {
    try {
        console.debug(`安全清理终端 ${props.name} 资源`)

        // 保存状态
        saveTerminalState()

        // 关闭WebSocket连接
        if (webSocket.value) {
            try {
                if (webSocket.value.readyState === WebSocket.OPEN) {
                    webSocket.value.close()
                }
            } catch (e) {
                console.warn('关闭WebSocket连接时出错:', e)
            } finally {
                webSocket.value = null
                isConnected.value = false
            }
        }

        // 清理终端实例
        if (terminal.value) {
            try {
                if (fitAddon.value) {
                    fitAddon.value.dispose()
                }
                terminal.value.dispose()
            } catch (error) {
                console.warn(`清理终端实例警告:`, error)
            } finally {
                fitAddon.value = null
                terminal.value = null
            }
        }

        isInitialized.value = false
        console.debug(`终端 ${props.name} 资源清理完成`)
    } catch (error) {
        console.error(`清理终端 ${props.name} 资源时出错:`, error)
    }
}

// WebSocket连接管理
const connectWebSocket = () => {
    if (!props.connectionId) {
        console.debug(`终端 ${props.name} 无连接ID，跳过WebSocket连接`)
        return
    }

    // 如果已有活跃连接，不再创建新连接
    if (webSocket.value && webSocket.value.readyState === WebSocket.OPEN) {
        console.debug(`终端 ${props.name} 已有活跃连接，跳过重新连接`)
        return
    }

    // 清理旧连接
    if (webSocket.value) {
        try {
            webSocket.value.close()
        } catch (e) {
            console.warn('关闭旧WebSocket连接时出错:', e)
        }
        webSocket.value = null
    }

    const wsUrl = buildWsUrl('terminal', props.connectionId)

    try {

        webSocket.value = new WebSocket(wsUrl)

        webSocket.value.onopen = () => {
            isConnected.value = true
            console.debug(`终端 ${props.name} WebSocket连接成功`)

            // 发送终端尺寸
            if (terminal.value) {
                const resizeMsg = JSON.stringify({
                    type: 'resize',
                    cols: terminal.value.cols,
                    rows: terminal.value.rows
                })
                webSocket.value?.send(resizeMsg)
            }

            // 只在没有保存状态时显示连接消息
            if (!terminalState.value.buffer) {
                terminal.value?.writeln('\r\n✅ SSH连接已建立')
            }
        }

        webSocket.value.onmessage = (event) => {
            if (terminal.value && isConnected.value) {
                try {
                    terminal.value.write(event.data)
                } catch (error) {
                    console.error('写入终端失败:', error)
                }
            }
        }

        webSocket.value.onclose = () => {
            console.debug(`终端 ${props.name} WebSocket连接关闭`)
            isConnected.value = false
        }

        webSocket.value.onerror = (error) => {
            console.error(`终端 ${props.name} WebSocket错误:`, error)
            isConnected.value = false
        }
    } catch (error) {
        console.error('创建WebSocket连接失败:', error)
    }
}

// 初始化终端
const initTerminal = () => {
    if (!containerRef.value) {
        console.warn('容器引用为空，延迟初始化')
        return
    }

    try {
        // 如果终端已初始化，只重新附加到DOM
        if (isInitialized.value && terminal.value) {
            console.debug(`终端 ${props.name} 已初始化，重新附加到DOM`)

            // 检查是否已附加到正确容器
            if (terminal.value.element?.parentElement !== containerRef.value) {
                terminal.value.open(containerRef.value)
                restoreTerminalState()
            }

            // 调整大小
            resizeTerminal()
            return
        }

        // 创建新的终端实例
        console.debug(`创建新的终端实例: ${props.name}`)

        terminal.value = new Terminal({
            cursorBlink: true,
            theme: {
                background: '#1e1e1e',
                foreground: '#f0f0f0',
                cursor: '#ffffff'
            },
            fontSize: 14,
            fontFamily: 'Consolas, "Courier New", monospace',
            scrollback: 10000
        })

        fitAddon.value = new FitAddon()
        terminal.value.loadAddon(fitAddon.value)

        terminal.value.open(containerRef.value)

        // 恢复之前的状态或显示初始内容
        if (terminalState.value.buffer) {
            restoreTerminalState()
        } else if (props.connectionId) {
            terminal.value.writeln(`\r\n🔄 正在连接到SSH服务器...`)
        } else {
            terminal.value.writeln('本地终端')
            terminal.value.write('$ ')
        }

        // 处理用户输入
        terminal.value.onData((data: string) => {
            if (props.connectionId && isConnected.value && webSocket.value) {
                webSocket.value.send(data)
            } else {
                terminal.value?.write(data)
            }
        })

        isInitialized.value = true

        // 初始化WebSocket连接
        if (props.connectionId) {
            connectWebSocket()
        }

        // 调整终端大小
        resizeTerminal()

    } catch (error) {
        console.error(`终端 ${props.name} 初始化失败:`, error)
    }
}

// 调整终端大小
const resizeTerminal = () => {
    if (fitAddon.value && containerRef.value) {
        try {
            fitAddon.value.fit()

            // 发送调整大小的消息
            if (isConnected.value && webSocket.value && terminal.value) {
                const resizeMsg = JSON.stringify({
                    type: 'resize',
                    cols: terminal.value.cols,
                    rows: terminal.value.rows
                })
                webSocket.value.send(resizeMsg)
            }
        } catch (error) {
            console.error('调整终端大小失败:', error)
        }
    }
}

// 监听可见性变化
watch(() => props.visible, (visible) => {
    console.debug(`终端 ${props.name} 可见性: ${visible}`)

    if (visible) {
        nextTick(() => {
            setTimeout(() => {
                initTerminal()
            }, 50)
        })
    } else {
        // 不可见时保存状态但不清理资源
        saveTerminalState()
    }
}, { immediate: true })

// 监听连接ID变化
watch(() => props.connectionId, (newId, oldId) => {
    if (newId !== oldId) {
        console.debug(`终端 ${props.name} 连接ID变化: ${oldId} -> ${newId}`)

        // 保存当前状态
        saveTerminalState()

        // 关闭旧连接
        if (webSocket.value) {
            webSocket.value.close()
            webSocket.value = null
            isConnected.value = false
        }

        // 重置终端显示新连接
        if (terminal.value && isInitialized.value) {
            terminal.value.reset()
            if (newId) {
                terminal.value.writeln(`\r\n🔄 切换到新连接: ${newId}`)
                connectWebSocket()
            }
        }
    }
})

// 生命周期
onMounted(() => {
    console.debug(`终端 ${props.name} 组件挂载`)
    window.addEventListener('resize', resizeTerminal)
})

onUnmounted(() => {
    console.debug(`终端 ${props.name} 组件卸载`)
    safeDispose()
    window.removeEventListener('resize', resizeTerminal)
})

// 公开方法
defineExpose({
    resize: resizeTerminal,
    safeDispose,
    isConnected: () => isConnected.value
})
</script>

<template>
    <div ref="containerRef" class="terminal-container"></div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>