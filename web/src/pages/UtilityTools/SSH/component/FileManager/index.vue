<script setup lang="ts">
import { ref, watch, h } from 'vue'
import {
    NProgress,
    NDataTable,
    NButton,
    NIcon,
    NModal,
    NInput,
    NText,
    NUpload,
    NUploadDragger,
    NEmpty,
    NSpace,
    NCode,
    createDiscreteApi
} from 'naive-ui'
import type { DataTableColumns, UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import {
    FolderOpenOutline,
    DocumentTextOutline,
    CloudUploadOutline,
    CloudDownloadOutline,
    CreateOutline,
    TrashOutline,
    ArrowBackOutline,
    RefreshOutline
} from '@vicons/ionicons5'
import type { FileInfoDTO } from '@/pages/UtilityTools/SSH/dto/FileInfo.ts'
import {
    getFileList,
    readFile,
    writeFile,
    createDirectory,
    deleteFile,
    renameFile,
    uploadFile,
    downloadFile
} from '@/pages/UtilityTools/SSH/service/fileManagement.ts'

const { message, dialog } = createDiscreteApi(['message', 'dialog'])

interface Props {
    connectionId: string
    visible: boolean
}

const props = defineProps<Props>()

const fileList = ref<FileInfoDTO[]>([])
const loading = ref(false)
const currentPath = ref('/')
const pathHistory = ref<string[]>(['/'])

// 文件编辑器相关
const editorVisible = ref(false)
const editingFile = ref<FileInfoDTO | null>(null)
const fileContent = ref('')
const saving = ref(false)

// 文件操作相关
const renameModalVisible = ref(false)
const renameTarget = ref<FileInfoDTO | null>(null)
const newName = ref('')

const createDirModalVisible = ref(false)
const newDirName = ref('')

const uploadFileModalVisible = ref(false)
const uploadFolderModalVisible = ref(false)
const renderIcon = (icon: any) => {
    return () => h(NIcon, null, { default: () => h(icon) })
}

const columns: DataTableColumns<FileInfoDTO> = [
    {
        title: '名称',
        key: 'name',
        render: (row) => {
            const icon = row.type === 'directory' ? FolderOpenOutline : DocumentTextOutline
            return h(NSpace, { align: 'center' }, {
                default: () => [
                    h(NIcon, { size: 18 }, { default: () => h(icon) }),
                    h(NText, {
                        style: {
                            cursor: row.type === 'directory' ? 'pointer' : 'default',
                            color: row.type === 'directory' ? '#1890ff' : 'inherit'
                        },
                        onClick: () => row.type === 'directory' ? enterDirectory(row) : undefined
                    }, { default: () => row.name })
                ]
            })
        }
    },
    {
        title: '大小',
        key: 'size',
        width: 80,
        render: (row) => {
            if (row.type === 'directory') return '-'
            const size = row.size
            if (size < 1024) return `${size} B`
            if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
            return `${(size / (1024 * 1024)).toFixed(1)} MB`
        }
    },
    {
        title: '权限',
        key: 'permissions',
        width: 80,
        render: (row) => h(NText, { type: 'info' }, { default: () => row.permissions })
    },
    {
        title: '修改时间',
        key: 'modifiedTime',
        render: (row) => {
            const date = new Date(row.modifiedTime)
            return date.toLocaleString()
        }
    },
    {
        title: '操作',
        key: 'actions',
        width: 350,
        render: (row) => {
            const actions: any[] = []

            actions.push(
                h(NButton, {
                    size: 'small',
                    onClick: () => openRenameModal(row)
                }, {
                    default: () => '重命名',
                    icon: renderIcon(CreateOutline)
                }),
                h(NButton, {
                    size: 'small',
                    type: 'error',
                    onClick: () => deleteFileAction(row)
                }, {
                    default: () => '删除',
                    icon: renderIcon(TrashOutline)
                })
            )

            if (row.type === 'file') {
                actions.push(
                    // h(NButton, {
                    //     size: 'small',
                    //     type: 'primary',
                    //     onClick: () => editFile(row)
                    // }, {
                    //     default: () => '编辑',
                    //     icon: renderIcon(CreateOutline)
                    // }),
                    h(NButton, {
                        size: 'small',
                        type: 'info',
                        onClick: () => downloadFileAction(row)
                    }, {
                        default: () => '下载',
                        icon: renderIcon(CloudDownloadOutline)
                    })
                )
            }

            return h(NSpace, { size: 'small' }, { default: () => actions })
        }
    }
]

const loadFileList = async () => {
    if (!props.connectionId) return

    loading.value = true
    try {
        const response = await getFileList(props.connectionId, currentPath.value)
        if (response.code === 200) {
            fileList.value = response.data || []
            // 添加父目录链接（如果不是根目录）
            if (currentPath.value !== '/') {
                fileList.value.unshift({
                    name: '..',
                    path: getParentPath(currentPath.value),
                    type: 'directory',
                    size: 0,
                    permissions: 'drwxr-xr-x',
                    modifiedTime: new Date().toISOString(),
                    owner: '',
                    group: ''
                } as FileInfoDTO)
            }
        } else {
            message.error(response.message || '获取文件列表失败')
        }
    } catch (error) {
        console.error('加载文件列表失败:', error)
        message.error('获取文件列表失败')
    } finally {
        loading.value = false
    }
}

const getParentPath = (path: string): string => {
    if (path === '/') return '/'
    const parts = path.split('/').filter(Boolean)
    parts.pop()
    return parts.length ? '/' + parts.join('/') : '/'
}

const enterDirectory = (dir: FileInfoDTO) => {
    if (dir.name === '..') {
        // 返回上级目录
        const parentPath = getParentPath(currentPath.value)
        currentPath.value = parentPath
        pathHistory.value.push(parentPath)
    } else {
        // 进入子目录
        const newPath = currentPath.value === '/' ? `/${dir.name}` : `${currentPath.value}/${dir.name}`
        currentPath.value = newPath
        pathHistory.value.push(newPath)
    }
    loadFileList()
}

const goBack = () => {
    if (pathHistory.value.length > 1) {
        pathHistory.value.pop()
        currentPath.value = pathHistory.value[pathHistory.value.length - 1]
        loadFileList()
    }
}

const editFile = async (file: FileInfoDTO) => {
    if (file.type !== 'file') return

    loading.value = true
    try {
        const response = await readFile(props.connectionId, file.path)
        if (response.code === 200) {
            editingFile.value = file
            fileContent.value = response.data || ''
            editorVisible.value = true
        } else {
            message.error(response.message || '读取文件失败')
        }
    } catch (error) {
        console.error('读取文件失败:', error)
        message.error('读取文件失败')
    } finally {
        loading.value = false
    }
}

const saveFile = async () => {
    if (!editingFile.value) return

    saving.value = true
    try {
        const response = await writeFile(props.connectionId, editingFile.value.path, fileContent.value)
        if (response.code === 200) {
            message.success('文件保存成功')
            editorVisible.value = false
            loadFileList()
        } else {
            message.error(response.message || '保存文件失败')
        }
    } catch (error) {
        console.error('保存文件失败:', error)
        message.error('保存文件失败')
    } finally {
        saving.value = false
    }
}

const openRenameModal = (file: FileInfoDTO) => {
    renameTarget.value = file
    newName.value = file.name
    renameModalVisible.value = true
}

const renameFileAction = async () => {
    if (!renameTarget.value || !newName.value.trim()) return

    try {
        const newPath = renameTarget.value.path.replace(/[^/]*$/, newName.value)
        const response = await renameFile(props.connectionId, renameTarget.value.path, newPath)
        if (response.code === 200) {
            message.success('重命名成功')
            renameModalVisible.value = false
            loadFileList()
        } else {
            message.error(response.message || '重命名失败')
        }
    } catch (error) {
        console.error('重命名失败:', error)
        message.error('重命名失败')
    }
}

const createDirectoryAction = async () => {
    if (!newDirName.value.trim()) return

    try {
        const dirPath = currentPath.value === '/' ? `/${newDirName.value}` : `${currentPath.value}/${newDirName.value}`
        const response = await createDirectory(props.connectionId, dirPath)
        if (response.code === 200) {
            message.success('目录创建成功')
            createDirModalVisible.value = false
            newDirName.value = ''
            loadFileList()
        } else {
            message.error(response.message || '创建目录失败')
        }
    } catch (error) {
        console.error('创建目录失败:', error)
        message.error('创建目录失败')
    }
}

const deleteFileAction = (file: FileInfoDTO) => {
    dialog.warning({
        title: '确认删除',
        content: `确定要删除 ${file.type === 'directory' ? '目录' : '文件'}「${file.name}」吗？`,
        positiveText: '确定',
        negativeText: '取消',
        onPositiveClick: async () => {
            try {
                const response = await deleteFile(props.connectionId, file.path)
                if (response.code === 200) {
                    message.success('删除成功')
                    loadFileList()
                } else {
                    message.error(response.message || '删除失败')
                }
            } catch (error) {
                console.error('删除失败:', error)
                message.error('删除失败')
            }
        }
    })
}

const downloadFileAction = async (file: FileInfoDTO) => {
    if (file.type !== 'file') return

    try {
        const blob = await downloadFile(props.connectionId, file.path)
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = file.name
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        message.success('文件下载开始')
    } catch (error) {
        console.error('下载文件失败:', error)
        message.error('下载文件失败')
    }
}

// 上传状态管理
const isUploading = ref(false)
const uploadProgress = ref(0)
const selectedFiles = ref<UploadFileInfo[]>([])
const uploadedFilesCount = ref(0)

// 处理文件选择变更
const handleFileChange = (options: { fileList: UploadFileInfo[] }) => {
    // 存储选择的文件列表
    selectedFiles.value = options.fileList
    // 重置上传进度和计数器
    uploadProgress.value = 0
    uploadedFilesCount.value = 0
}

// 处理上传按钮点击
const handleUploadClick = async () => {
    if (selectedFiles.value.length === 0 || isUploading.value) {
        return
    }
    
    // 设置上传状态
    isUploading.value = true
    uploadProgress.value = 0
    uploadedFilesCount.value = 0
    
    try {
        // 顺序上传文件，避免并发请求过多
        for (let i = 0; i < selectedFiles.value.length; i++) {
            const fileInfo = selectedFiles.value[i]
            const file = fileInfo.file
            
            if (!file) continue
            
            // 计算当前文件的进度权重
            const fileProgressWeight = 100 / selectedFiles.value.length
            
            await uploadFile(
                props.connectionId, 
                currentPath.value, 
                file as File, 
                (percentage) => {
                    // 更新整体进度
                    const currentFileProgress = (percentage / 100) * fileProgressWeight
                    const completedFilesProgress = (uploadedFilesCount.value) * fileProgressWeight
                    uploadProgress.value = Math.round(completedFilesProgress + currentFileProgress)
                }
            )
            
            uploadedFilesCount.value++
        }
        
        // 所有文件上传完成
            message.success(`成功上传 ${selectedFiles.value.length} 个文件/文件夹`)
            // 上传完成后刷新文件列表
            loadFileList()
            // 关闭对应的上传模态框
            if (uploadFileModalVisible.value) {
                uploadFileModalVisible.value = false
            } else if (uploadFolderModalVisible.value) {
                uploadFolderModalVisible.value = false
            }
    } catch (error) {
        console.error('文件上传失败:', error)
        message.error(error instanceof Error ? error.message : '文件上传失败')
        // 上传失败时也关闭对应的模态框
        if (uploadFileModalVisible.value) {
            uploadFileModalVisible.value = false
        } else if (uploadFolderModalVisible.value) {
            uploadFolderModalVisible.value = false
        }
    } finally {
        // 重置上传状态
        isUploading.value = false
        uploadProgress.value = 0
        uploadedFilesCount.value = 0
        selectedFiles.value = []
    }
}

// 处理文件上传，由于我们使用了自定义的上传逻辑，这里只需要阻止默认行为
const handleUpload = (options: UploadCustomRequestOptions) => {
    // 不执行任何操作，因为我们在handleUploadClick中统一处理上传
    return false
}

watch(() => props.connectionId, (newVal) => {
    if (newVal && props.visible) {
        currentPath.value = '/'
        pathHistory.value = ['/']
        loadFileList()
    }
}, { immediate: true })

watch(() => props.visible, (newVal) => {
    if (newVal && props.connectionId) {
        loadFileList()
    }
})

defineExpose({
    reload: loadFileList
})
</script>

<template>
    <div class="file-manager" v-if="visible">
        <!-- 顶部工具栏 -->
        <div class="toolbar">
            <n-space>
                <n-button @click="goBack" :disabled="currentPath === '/'" :render-icon="renderIcon(ArrowBackOutline)">
                    返回上级
                </n-button>
                <n-button @click="loadFileList" :render-icon="renderIcon(RefreshOutline)">
                    刷新
                </n-button>
                <n-button @click="createDirModalVisible = true" type="primary" :render-icon="renderIcon(CreateOutline)">
                    新建目录
                </n-button>
                <n-button @click="uploadFileModalVisible = true" type="info" :render-icon="renderIcon(CloudUploadOutline)">
                    上传文件
                </n-button>
                <n-button @click="uploadFolderModalVisible = true" type="info" :render-icon="renderIcon(FolderOpenOutline)">
                    上传文件夹
                </n-button>
            </n-space>
            <div class="current-path">
                <n-text type="info">当前路径: {{ currentPath }}</n-text>
            </div>
        </div>

        <!-- 文件列表 -->
        <div class="file-list">
            <n-data-table :columns="columns" :data="fileList" :loading="loading" :bordered="false" size="small"
                :flex-height="true" style="height: 100%" :virtual-scroll="true" />
            <n-empty v-if="!loading && fileList.length === 0" description="目录为空">
                <template #extra>
                    <n-button size="small" @click="createDirModalVisible = true">
                        创建目录
                    </n-button>
                </template>
            </n-empty>
        </div>

        <!-- 文件编辑器模态框 -->
        <n-modal v-model:show="editorVisible" :mask-closable="false" :close-on-esc="false" preset="dialog" title="编辑文件"
            :show-icon="false" positive-text="保存" negative-text="取消" @positive-click="saveFile"
            @negative-click="editorVisible = false" :loading="saving">
            <div style="height: 400px;">
                <n-code :code="fileContent" language="text" show-line-numbers :word-wrap="true" />
            </div>
        </n-modal>

        <!-- 重命名模态框 -->
        <n-modal v-model:show="renameModalVisible" :mask-closable="false" preset="dialog" title="重命名" :show-icon="false"
            positive-text="确定" negative-text="取消" @positive-click="renameFileAction"
            @negative-click="renameModalVisible = false">
            <n-input v-model:value="newName" placeholder="请输入新名称" />
        </n-modal>

        <!-- 创建目录模态框 -->
        <n-modal v-model:show="createDirModalVisible" :mask-closable="false" preset="dialog" title="创建目录"
            :show-icon="false" positive-text="确定" negative-text="取消" @positive-click="createDirectoryAction"
            @negative-click="createDirModalVisible = false">
            <n-input v-model:value="newDirName" placeholder="请输入目录名称" />
        </n-modal>

        <!-- 上传文件模态框 -->
        <n-modal v-model:show="uploadFileModalVisible" :mask-closable="false" preset="dialog" title="上传文件"
            :show-icon="false"
            positive-text="上传"
            negative-text="取消"
            :positive-button-props="{ loading: isUploading, disabled: isUploading }"
            :negative-button-props="{ disabled: isUploading }"
            @positive-click="handleUploadClick"
            @negative-click="uploadFileModalVisible = false">
            <n-upload :multiple="true" :custom-request="handleUpload" accept="*/*" :disabled="isUploading" @change="handleFileChange">
                <n-upload-dragger>
                    <div style="margin-bottom: 12px">
                        <n-icon size="48" :depth="3">
                            <CloudUploadOutline />
                        </n-icon>
                    </div>
                    <n-text style="font-size: 16px">点击或拖拽文件到此处上传</n-text>
                    <p depth="3" style="margin: 8px 0 0 0">
                        支持单个或多个文件上传
                    </p>
                </n-upload-dragger>
            </n-upload>
            
            <!-- 上传进度显示 -->
            <n-space vertical style="margin-top: 16px; width: 100%" v-if="isUploading">
                <n-text>正在上传...</n-text>
                <n-progress :percentage="uploadProgress" :show-indicator="true" />
            </n-space>
        </n-modal>
        
        <!-- 上传文件夹模态框 -->
        <n-modal v-model:show="uploadFolderModalVisible" :mask-closable="false" preset="dialog" title="上传文件夹"
            :show-icon="false"
            positive-text="上传"
            negative-text="取消"
            :positive-button-props="{ loading: isUploading, disabled: isUploading }"
            :negative-button-props="{ disabled: isUploading }"
            @positive-click="handleUploadClick"
            @negative-click="uploadFolderModalVisible = false">
            <n-upload :directory="true" directory-dnd :multiple="true" :custom-request="handleUpload" accept="*/*" :disabled="isUploading" @change="handleFileChange">
                <n-upload-dragger>
                    <div style="margin-bottom: 12px">
                        <n-icon size="48" :depth="3">
                            <CloudUploadOutline />
                        </n-icon>
                    </div>
                    <n-text style="font-size: 16px">点击或拖拽文件夹到此处上传</n-text>
                    <p depth="3" style="margin: 8px 0 0 0">
                        支持单个文件夹上传
                    </p>
                </n-upload-dragger>
            </n-upload>
            
            <!-- 上传进度显示 -->
            <n-space vertical style="margin-top: 16px; width: 100%" v-if="isUploading">
                <n-text>正在上传...</n-text>
                <n-progress :percentage="uploadProgress" :show-indicator="true" />
            </n-space>
        </n-modal>
    </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>