<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import type { Component } from 'vue'
import { NSplit, NDropdown, NIcon, NButton, NTabs, NTabPane, NTree, createDiscreteApi, NEmpty } from 'naive-ui'
import type { TreeOption, DropdownOption } from 'naive-ui'
import { FlowConnection } from '@vicons/carbon'
import { TerminalOutline } from '@vicons/ionicons5'
import { GroupList24Regular } from '@vicons/fluent'
import {
    Folder,
    FolderOpenOutline
} from '@vicons/ionicons5'
import { getGroup } from '@/pages/UtilityTools/SSH/service/getGroup.ts'
import { deleteGroup } from '@/pages/UtilityTools/SSH/service/deleteGroup.ts'
import { deleteConnection } from '@/pages/UtilityTools/SSH/service/deleteConnection.ts'
import TerminalComponent from '@/pages/UtilityTools/SSH/component/TerminalComponent/index.vue'
import GroupCreateModal from '@/pages/UtilityTools/SSH/component/GroupCreateModal/index.vue'
import ConnectionCreateModal from '@/pages/UtilityTools/SSH/component/ConnectionCreateModal/index.vue'
import ResourceMonitor from '@/pages/UtilityTools/SSH/component/ResourceMonitor/index.vue'
import FileManager from '@/pages/UtilityTools/SSH/component/FileManager/index.vue'

const { message, dialog } = createDiscreteApi(
    ['message', 'dialog']
)

const groupCreateModalRef = ref()
const connectionCreateModalRef = ref()

function renderIcon(icon: Component) {
    return () => h(NIcon, null, { default: () => h(icon) })
}

const sessionDropdownOptions = [
    {
        label: '新建连接',
        key: 'newConnection',
        icon: renderIcon(TerminalOutline)
    },
    {
        label: '新建分组',
        key: 'newGroup',
        icon: renderIcon(GroupList24Regular)
    },
]

const sessionDropdownSelectHandler = (key: string) => {
    if (key === 'newGroup') {
        groupCreateModalRef.value.show()
    } else if (key === 'newConnection') {
        connectionCreateModalRef.value.show()
    }
}

const updatePrefixWithExpaned = (
    _keys: Array<string | number>,
    _option: Array<TreeOption | null>,
    meta: {
        node: TreeOption | null
        action: 'expand' | 'collapse' | 'filter'
    }
) => {
    if (!meta.node)
        return
    switch (meta.action) {
        case 'expand':
            meta.node.prefix = () =>
                h(NIcon, null, {
                    default: () => h(FolderOpenOutline)
                })
            break
        case 'collapse':
            meta.node.prefix = () =>
                h(NIcon, null, {
                    default: () => h(Folder)
                })
            break
    }
}

// 节点点击处理
const nodeProps = ({ option }: { option: TreeOption }) => {
    return {
        onClick() {
            // 根据类型处理点击事件
            if (option.type === 'connection' || (!option.children && !option.disabled)) {
                // 传递节点key作为参数
                addNewSession(option.key as string, option.label as string)
            }
        },
        onContextmenu(e: MouseEvent): void {
            e.stopPropagation() // 阻止事件冒泡，避免菜单无法正常显示
            selectedNode.value = option

            // 根据节点类型设置右键菜单选项
            if (option.type === 'connection' || !option.children) {
                dropdownOptions.value = [
                    {
                        label: '删除连接',
                        key: 'deleteConnection'
                    }
                ]
            } else {
                dropdownOptions.value = [
                    {
                        label: '删除分组',
                        key: 'deleteGroup'
                    },
                    {
                        label: '在分组中新建连接',
                        key: 'newConnectionInGroup'
                    }
                ]
            }

            dropdownShow.value = true
            dropdownX.value = e.clientX
            dropdownY.value = e.clientY
            e.preventDefault()
        },
        disabled: false // 确保所有节点都可点击
    }
}

const treeData = ref([])

const dropdownShow = ref(false)
const dropdownX = ref(0)
const dropdownY = ref(0)
const dropdownOptions = ref<DropdownOption[]>([])
const selectedNode = ref<TreeOption | null>(null)

// 会话管理
const activeSessionName = ref<string>('')
const sessions = ref<Record<string, { connectionId: string, label: string, activeTab: string }>>({})
const sessionRefs = ref<Record<string, any>>({})

// 标签页管理
const sessionTabs = ref<string[]>([])

// 添加新会话
const addNewSession = (connectionId: string, label: string) => {
    // 检查是否已存在相同连接的会话
    const existingSession = Object.entries(sessions.value).find(
        ([_, info]) => info.connectionId === connectionId
    )

    if (existingSession) {
        // 切换到已存在的会话
        const [existingName] = existingSession
        activeSessionName.value = existingName
        return
    }

    // 创建新会话
    const baseName = label || `会话-${connectionId}`

    // 确保名称唯一
    let counter = 1
    let uniqueSessionName = baseName
    while (sessionTabs.value.includes(uniqueSessionName)) {
        uniqueSessionName = `${baseName}(${counter})`
        counter++
    }

    // 记录会话信息
    sessions.value[uniqueSessionName] = {
        connectionId,
        label,
        activeTab: 'terminal' // 默认打开终端标签
    }

    // 添加到会话列表并激活
    sessionTabs.value.push(uniqueSessionName)
    activeSessionName.value = uniqueSessionName
}

// 处理会话关闭
const sessionCloseHandler = (name: string) => {
    const index = sessionTabs.value.findIndex(v => name === v)
    if (index === -1) {
        console.warn(`尝试关闭不存在的会话: ${name}`)
        return
    }

    try {
        // 清理引用
        if (sessionRefs.value[name]) {
            // 调用会话的清理方法
            if (sessionRefs.value[name].safeDispose) {
                sessionRefs.value[name].safeDispose()
            }
            delete sessionRefs.value[name]
        }

        // 从列表中移除
        sessionTabs.value.splice(index, 1)

        // 删除会话信息
        if (sessions.value[name]) {
            delete sessions.value[name]
        }

        // 更新当前选中的会话
        if (activeSessionName.value === name) {
            activeSessionName.value = sessionTabs.value[Math.max(0, index - 1)] || ''
        }

    } catch (error) {
        console.error(`关闭会话 ${name} 时出错:`, error)
    }
}

// 获取当前会话的激活标签
const getSessionActiveTab = (sessionName: string) => {
    return sessions.value[sessionName]?.activeTab || 'terminal'
}

// 更新会话的激活标签
const updateSessionActiveTab = (sessionName: string, tabName: string) => {
    if (sessions.value[sessionName]) {
        sessions.value[sessionName].activeTab = tabName
    }
}

const initTreeData = async () => {
    try {
        const res: any = await getGroup()

        // 把res放置到treeData中
        treeData.value = res.data.map((item: any) => ({
            key: item.id,
            label: item.name,
            type: 'group', // 添加类型标识
            prefix: () =>
                h(NIcon, null, {
                    default: () => h(Folder)
                }),
            children: item.connections && item.connections.length > 0 ?
                item.connections.map((connection: any) => ({
                    key: connection.id,
                    label: connection.name,
                    type: 'connection', // 添加类型标识
                    disabled: false, // 确保连接节点可点击
                    prefix: () =>
                        h(NIcon, null, {
                            default: () => h(TerminalOutline)
                        })
                })) : []
        }))
        connectionCreateModalRef.value.initGroupOptions(res.data)
    } catch (error) {
        console.error('初始化树数据失败:', error)
        message.error('加载SSH连接数据失败，请稍后重试')
    }
}

const dropdownSelectHandler = (key: string) => {
    dropdownShow.value = false

    // 确保selectedNode存在
    if (!selectedNode.value) {
        console.error('选中节点为空')
        return
    }

    switch (key) {
        case 'deleteConnection':
            if (selectedNode.value.type === 'connection' && !selectedNode.value.children) {
                dialog.warning({
                    title: '确认删除连接',
                    content: `确定要删除连接「${selectedNode.value.label}」吗？`,
                    positiveText: '确定',
                    negativeText: '取消',
                    onPositiveClick: async () => {
                        try {
                            const response = await deleteConnection(selectedNode.value!.key as string)
                            console.log(response)
                            if (response.code === 200) {
                                message.success(response.message || '连接删除成功')
                                // 重新加载分组数据
                                await initTreeData()
                            } else {
                                message.error(response.message || '连接删除失败')
                            }
                        } catch (error) {
                            console.error('删除连接失败:', error)
                            message.error('连接删除失败，请稍后重试')
                        }
                    }
                })
            }
            break

        case 'deleteGroup':
            if (selectedNode.value.type === 'group' || selectedNode.value.children) {
                dialog.warning({
                    title: '确认删除分组',
                    content: `确定要删除分组「${selectedNode.value.label}」吗？`,
                    positiveText: '确定',
                    negativeText: '取消',
                    onPositiveClick: async () => {
                        try {
                            const response = await deleteGroup(selectedNode.value!.key as string)
                            console.log(response)
                            if (response.code === 200) {
                                message.success(response.message || '分组删除成功')
                                // 重新加载分组数据
                                await initTreeData()
                            } else {
                                message.error(response.message || '分组删除失败')
                            }
                        } catch (error) {
                            console.error('删除分组失败:', error)
                            message.error('分组删除失败，请稍后重试')
                        }
                    }
                })
            }
            break

        case 'newConnectionInGroup':
            if (selectedNode.value.type === 'group' && selectedNode.value.key && connectionCreateModalRef.value) {
                // 在特定分组中创建连接
                connectionCreateModalRef.value.show(selectedNode.value.key as string)
            }
            break

        default:
            console.log('未处理的菜单项:', key)
    }
}

const dropdownClickoutsideHandler = () => {
    dropdownShow.value = false
}

onMounted(async () => {
    await initTreeData()
})
</script>

<template>
    <div class="ssh-div">
        <div class="content-div">
            <n-split direction="horizontal" :default-size="0.15" :max="0.4" :min="0.15">
                <!-- 左侧：连接列表 -->
                <template #1>
                    <div class="left-panel">
                        <div class="panel-header">
                            <h3>SSH连接</h3>
                            <div class="toolbar">
                                <n-dropdown :options="sessionDropdownOptions" @select="sessionDropdownSelectHandler">
                                    <n-button size="small" type="primary" text>
                                        <template #icon>
                                            <NIcon>
                                                <FlowConnection />
                                            </NIcon>
                                        </template>
                                    </n-button>
                                </n-dropdown>
                            </div>
                        </div>
                        <div class="tree-container">
                            <n-tree 
                                block-line 
                                expand-on-click 
                                :data="treeData" 
                                :node-props="nodeProps" 
                                :on-update:expanded-keys="updatePrefixWithExpaned" 
                                :selectable="false" 
                            />
                            <n-dropdown 
                                trigger="manual" 
                                placement="bottom-start" 
                                :show="dropdownShow"
                                :options="dropdownOptions as any" 
                                :x="dropdownX" 
                                :y="dropdownY"
                                @select="dropdownSelectHandler" 
                                @clickoutside="dropdownClickoutsideHandler" 
                            />
                        </div>
                    </div>
                </template>
                
                <!-- 右侧：会话面板 -->
                <template #2>
                    <div class="right-panel">
                        <n-empty 
                            v-if="sessionTabs.length === 0" 
                            description="暂无打开的会话"
                            class="empty-state"
                        >
                            <template #extra>
                                <n-button size="small" type="primary" @click="connectionCreateModalRef.show()">
                                    新建连接
                                </n-button>
                            </template>
                        </n-empty>
                        
                        <n-tabs 
                            v-else 
                            v-model:value="activeSessionName" 
                            type="card" 
                            closable 
                            size="small"
                            class="session-tabs"
                            @close="sessionCloseHandler"
                        >
                            <n-tab-pane 
                                v-for="sessionName in sessionTabs" 
                                :key="sessionName" 
                                :name="sessionName"
                                display-directive="show"
                            >
                                <template #tab>
                                    <span>{{ sessions[sessionName]?.label || sessionName }}</span>
                                </template>
                                
                                <!-- 会话内部标签页 -->
                                <div class="session-content">
                                    <n-tabs 
                                        :value="getSessionActiveTab(sessionName)"
                                        type="line" 
                                        animated
                                        size="small"
                                        @update:value="(tabName) => updateSessionActiveTab(sessionName, tabName)"
                                    >
                                        <!-- 终端标签页 -->
                                        <n-tab-pane name="terminal" tab="终端" display-directive="show">
                                            <TerminalComponent 
                                                :ref="(el) => { if (el) sessionRefs[sessionName] = el }" 
                                                :name="sessionName"
                                                :connection-id="sessions[sessionName]?.connectionId"
                                                :visible="activeSessionName === sessionName && sessions[sessionName]?.activeTab === 'terminal'" 
                                            />
                                        </n-tab-pane>
                                        
                                        <!-- 文件管理标签页 -->
                                        <n-tab-pane name="files" tab="文件管理" display-directive="show">
                                            <FileManager 
                                                :connection-id="sessions[sessionName]?.connectionId || ''" 
                                                :visible="activeSessionName === sessionName && sessions[sessionName]?.activeTab === 'files'" 
                                            />
                                        </n-tab-pane>
                                        
                                        <!-- 参数面板标签页 -->
                                        <n-tab-pane name="monitor" tab="资源监控" display-directive="show">
                                            <ResourceMonitor 
                                                :connection-id="sessions[sessionName]?.connectionId" 
                                                :key="`${sessionName}-monitor`"
                                                :visible="activeSessionName === sessionName && sessions[sessionName]?.activeTab === 'monitor'"
                                            />
                                        </n-tab-pane>
                                    </n-tabs>
                                </div>
                            </n-tab-pane>
                        </n-tabs>
                    </div>
                </template>
            </n-split>
        </div>
        
        <GroupCreateModal ref="groupCreateModalRef" @success="initTreeData" />
        <ConnectionCreateModal ref="connectionCreateModalRef" @success="initTreeData" />
    </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>