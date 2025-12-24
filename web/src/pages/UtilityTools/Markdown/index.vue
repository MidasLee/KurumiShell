<script setup lang="ts">
import { ref, reactive, computed, onMounted, h, nextTick, watch } from 'vue'
import { NTree, NInput, NButton, NDropdown, NIcon, NEmpty, createDiscreteApi, NSplit, NScrollbar } from 'naive-ui'
import FolderCreateModal from './component/FolderCreateModal/index.vue'
import NoteCreateModal from './component/NoteCreateModal/index.vue'
import type { TreeOption } from 'naive-ui'
import {
  AddOutline,
  Folder,
  FolderOpenOutline,
  DocumentTextOutline
} from '@vicons/ionicons5'
import { getNoteList, updateNote, deleteNote } from './service/noteService.ts'
import { getFolderList, deleteFolder } from './service/folderService.ts'
import { getAllTags } from './service/tagService.ts'

// 定义类型
interface Note {
  id: string
  title: string
  content: string
  folderId: string | null
  tags: string[]
  createdAt: string
  updatedAt: string
}

interface Folder {
  id: string
  name: string
  parentId: string | null
  children?: Folder[]
}

interface Tag {
  id: string
  name: string
  color: string
}

// 创建离散API
const { message, dialog } = createDiscreteApi(['message', 'dialog'])

// 响应式数据
const notes = ref<Note[]>([])
const folders = ref<Folder[]>([])
const nestedFolders = ref<any[]>([])
const folderTreeData = ref<TreeOption[]>([])
const tags = ref<Tag[]>([])
const loading = ref(false)
const currentNote = ref<Note | null>(null)
const currentFolderId = ref('')
const searchKeyword = ref('')

// 右键菜单相关
const dropdownShow = ref(false)
const dropdownX = ref(0)
const dropdownY = ref(0)
const dropdownOptions = ref<any[]>([])
const selectedNode = ref<TreeOption | null>(null)

// 组件引用
const folderCreateModalRef = ref()
const noteCreateModalRef = ref()
const vMdEditorHeight = ref('300px')
const editorVisible = ref(false)

// 会话下拉菜单选项
const sessionDropdownOptions = [
  {
    label: '新建笔记',
    key: 'newNote',
    icon: () => h(NIcon, null, { default: () => h(DocumentTextOutline) })
  },
  {
    label: '新建文件夹',
    key: 'newFolder',
    icon: () => h(NIcon, null, { default: () => h(FolderOpenOutline) })
  },
]

// 会话下拉菜单选择处理函数
const sessionDropdownSelectHandler = (key: string) => {
  if (key === 'newNote') {
    handleOpenCreateNoteModal()
  } else if (key === 'newFolder') {
    handleOpenCreateFolderModal()
  }
}

// 表单数据
const noteFormData = reactive<{ title: string; content: string; folderId: string | null; tags: string[] }>({
  title: '',
  content: '',
  folderId: null,
  tags: []
})

// 计算当前文件夹下的笔记
const filteredNotes = computed(() => {
  let result = notes.value

  // 按文件夹筛选
  if (currentFolderId.value) {
    result = result.filter(note => note.folderId === currentFolderId.value)
  }

  // 按搜索关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(note =>
      note.title.toLowerCase().includes(keyword) ||
      note.content.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 目录树节点属性配置
const nodeProps = ({ option }: { option: TreeOption }) => {
  return {
    onClick() {
      // 根据节点类型处理点击事件
      if (option.type === 'folder') {
        handleFolderSelect(option.key as string)
      } else if (option.type === 'note') {
        // 找到对应的笔记并打开
        const note = notes.value.find(n => n.id === option.key)
        if (note) {
          handleOpenEditNoteModal(note)
        }
      }
    },
    onContextmenu(e: MouseEvent): void {
      e.stopPropagation() // 阻止事件冒泡
      selectedNode.value = option

      // 根据节点类型设置右键菜单选项
      if (option.type === 'folder') {
        dropdownOptions.value = [
          {
            label: '新建文件夹',
            key: 'newSubFolder'
          },
          {
            label: '新建笔记',
            key: 'newNoteInFolder'
          },
          {
            label: '删除文件夹',
            key: 'deleteFolder'
          }
        ]
      } else if (option.type === 'note') {
        dropdownOptions.value = [
          {
            label: '编辑笔记',
            key: 'editNote'
          },
          {
            label: '删除笔记',
            key: 'deleteNote'
          },
          {
            label: '导出笔记',
            key: 'exportNote'
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

// 加载笔记列表
const loadNotes = async () => {
  loading.value = true
  try {
    const response = await getNoteList()
    // NoteListResponse包含嵌套的data字段，所以需要访问response.data.data
    notes.value = response?.data?.data?.filter((note: any) => note) || []
    // 更新目录树
    updateTreeData()
  } catch (error) {
    message.error('加载笔记失败')
    console.error('Failed to load notes:', error)
  } finally {
    loading.value = false
  }
}

// 更新目录树节点展开/折叠状态时的图标
const updatePrefixWithExpaned = (
  _keys: Array<string | number>,
  _option: Array<TreeOption | null>,
  meta: {
    node: TreeOption | null
    action: 'expand' | 'collapse' | 'filter'
  }
) => {
  if (!meta.node) return
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

// 构建目录树数据 - 同时支持嵌套结构和扁平结构
const buildTree = (folderData: any[] | undefined, noteData: any[] | undefined, parentId: string | null = null): TreeOption[] => {
  if (!folderData) return []

  // 检查数据是否是嵌套结构（是否包含children字段）
  const isNestedStructure = folderData.some(folder => folder && folder.children && Array.isArray(folder.children))

  if (isNestedStructure) {
    // 递归处理单个文件夹及其子文件夹
    const processFolder = (folder: any): TreeOption => {
      return {
        key: folder.id,
        label: folder.name,
        type: 'folder',
        prefix: () => h(NIcon, null, { default: () => h(Folder) }),
        children: [
          // 递归处理子文件夹
          ...(folder.children && folder.children.length > 0 ? folder.children.map(processFolder) : []),
          // 添加该文件夹下的笔记
          ...noteData?.filter(note => note && note.folderId === folder.id).map((note: any) => ({
            key: note.id,
            label: note.title,
            type: 'note',
            prefix: () => h(NIcon, null, { default: () => h(DocumentTextOutline) })
          })) || []
        ]
      }
    }

    // 如果是根调用（parentId为null），构建所有顶级文件夹和根目录笔记
    if (parentId === null) {
      // 构建所有顶级文件夹节点
      const folderNodes = folderData
        .filter(folder => folder && (folder.parentId === null || folder.parentId === ''))
        .map(processFolder)

      // 找到没有parentId的笔记（根目录下的笔记）
      const rootNotes = noteData
        ? noteData.filter(note => note && (note.folderId === null || note.folderId === ''))
        : []

      // 根目录下的笔记节点
      const noteNodes = rootNotes.map((note: any) => ({
        key: note.id,
        label: note.title,
        type: 'note',
        prefix: () => h(NIcon, null, { default: () => h(DocumentTextOutline) })
      }))

      // 合并文件夹和根目录笔记
      return [...folderNodes, ...noteNodes]
    } else {
      // 递归调用时，返回与当前parentId匹配的文件夹节点
      // 在嵌套结构中，我们需要通过检查每个文件夹的id来找到匹配的文件夹
      const matchingFolder = folderData.find(folder => folder && folder.id === parentId)
      if (matchingFolder && matchingFolder.children) {
        return matchingFolder.children.map(processFolder)
      }
      return []
    }
  } else {
    // 使用扁平结构处理（按parentId关联）
    // 查找当前parentId下的所有文件夹
    const childFolders = folderData.filter(folder => folder && folder.parentId === parentId)

    // 构建文件夹节点
    const folderNodes = childFolders.map(folder => ({
      key: folder.id,
      label: folder.name,
      type: 'folder',
      prefix: () => h(NIcon, null, { default: () => h(Folder) }),
      children: [
        // 递归构建子文件夹
        ...buildTree(folderData, noteData, folder.id),
        // 添加该文件夹下的笔记
        ...noteData?.filter(note => note && note.folderId === folder.id).map((note: any) => ({
          key: note.id,
          label: note.title,
          type: 'note',
          prefix: () => h(NIcon, null, { default: () => h(DocumentTextOutline) })
        })) || []
      ]
    }))

    // 如果是根目录（parentId为null），添加根目录下的笔记
    if (parentId === null) {
      // 找到没有parentId的笔记（根目录下的笔记）
      const rootNotes = noteData
        ? noteData.filter(note => note && (note.folderId === null || note.folderId === ''))
        : []

      // 根目录下的笔记节点
      const noteNodes = rootNotes.map((note: any) => ({
        key: note.id,
        label: note.title,
        type: 'note',
        prefix: () => h(NIcon, null, { default: () => h(DocumentTextOutline) })
      }))

      // 合并文件夹和根目录笔记
      return [...folderNodes, ...noteNodes]
    }

    // 如果不是根目录，只返回子文件夹（笔记已经在父级处理了）
    return folderNodes
  }
}

// 更新目录树
const updateTreeData = () => {
  folderTreeData.value = buildTree(nestedFolders.value, notes.value)
}

// 将嵌套的文件夹结构转换为扁平数组
const flattenFolders = (folderData: any[]): any[] => {
  return folderData.flatMap(folder => {
    const folderItem = {
      id: folder.id,
      name: folder.name,
      parentId: folder.parentId
    }
    // 如果有子文件夹，递归处理并合并到结果中
    if (folder.children && folder.children.length > 0) {
      return [folderItem, ...flattenFolders(folder.children)]
    }
    return [folderItem]
  })
}

// 加载文件夹列表
const loadFolders = async () => {
  try {
    const response = await getFolderList()

    // 从response.data获取实际的文件夹数据，因为axios拦截器已经处理了响应
    const folderData = response?.data || []

    // 存储原始的嵌套文件夹结构
    nestedFolders.value = folderData

    // 将嵌套的文件夹结构转换为扁平数组
    const flatFolders = flattenFolders(folderData)

    // 更新folders数组（用于筛选等其他功能）
    folders.value = flatFolders

    // 初始化笔记创建模态框的文件夹选项
    if (noteCreateModalRef.value) {
      noteCreateModalRef.value.initFolderOptions(folderData)
    }

    // 初始化文件夹创建模态框的文件夹选项
    if (folderCreateModalRef.value) {
      folderCreateModalRef.value.initFolderOptions(folderData)
    }

    // 更新目录树
    updateTreeData()
  } catch (error) {
    message.error('加载文件夹失败')
    console.error('Failed to load folders:', error)
  }
}

// 加载标签列表
const loadTags = async () => {
  try {
    const response = await getAllTags()
    // 从response.data获取实际的标签数据，因为axios拦截器已经处理了响应
    tags.value = (response?.data || [])
      .filter((tagName: string) => tagName)
      .map((tagName: string) => ({
        id: tagName,
        name: tagName,
        color: '' // 默认颜色，实际使用时可能需要从服务端获取
      }))
  } catch (error) {
    message.error('加载标签失败')
    console.error('Failed to load tags:', error)
  }
}

// 选择文件夹
const handleFolderSelect = (folderId: string) => {
  currentFolderId.value = folderId
}

// 打开创建文件夹模态框
const handleOpenCreateFolderModal = () => {
  if (folderCreateModalRef.value) {
    folderCreateModalRef.value.show(undefined)
  }
}

// 打开创建笔记模态框
const handleOpenCreateNoteModal = () => {
  noteCreateModalRef.value?.show()
}

// 打开编辑笔记（直接在右侧面板编辑）
const handleOpenEditNoteModal = (note: Note) => {
  currentNote.value = note
  noteFormData.title = note.title
  noteFormData.content = note.content
  noteFormData.folderId = note.folderId
  noteFormData.tags = [...note.tags]
}

// 关闭当前笔记
const handleCloseNote = () => {
  currentNote.value = null
  resetNoteForm()
}

// 删除笔记
const handleDeleteNote = (noteId: string) => {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除这篇笔记吗？此操作不可恢复。',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteNote(noteId)

        notes.value = notes.value.filter(note => note.id !== noteId)
        if (currentNote.value?.id === noteId) {
          currentNote.value = null
        }
        // 更新目录树
        updateTreeData()
        message.success('笔记删除成功')
      } catch (error) {
        message.error('删除笔记失败')
        console.error('Failed to delete note:', error)
      }
    }
  })
}

// 导出笔记
const handleExportNote = (note: Note) => {
  try {
    const markdownContent = note.content
    const blob = new Blob([markdownContent], { type: 'text/markdown;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${note.title}.md`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    message.success('笔记导出成功')
  } catch (error) {
    message.error('导出笔记失败')
    console.error('Failed to export note:', error)
  }
}

// 下拉菜单选择处理函数
const handleDropdownSelect = (key: string, option: any) => {
  const note = option.note as Note
  switch (key) {
    case 'edit':
      handleOpenEditNoteModal(note)
      break
    case 'delete':
      handleDeleteNote(note.id)
      break
    case 'export':
      handleExportNote(note)
      break
  }
}

// 右键菜单选择处理函数
const dropdownSelectHandler = (key: string) => {
  if (!selectedNode.value) return

  if (selectedNode.value.type === 'folder') {
    const folderId = selectedNode.value.key as string
    switch (key) {
      case 'newSubFolder':
        // 打开新建文件夹模态框，并传递当前文件夹ID作为父级ID
        folderCreateModalRef.value.show(folderId)
        dropdownShow.value = false
        break
      case 'newNoteInFolder':
        // 打开新建笔记模态框，并传递当前文件夹ID作为父级ID
        noteCreateModalRef.value.showInFolder(folderId)
        dropdownShow.value = false
        break
      case 'deleteFolder':
        dialog.warning({
          title: '确认删除',
          content: '确定要删除这个文件夹吗？此操作不可恢复。',
          positiveText: '删除',
          negativeText: '取消',
          onPositiveClick: async () => {
            try {
              const response = await deleteFolder(selectedNode.value!.key as string)
              if (response.code === 200) {
                message.success(response.message || '文件夹删除成功')
                // 重新加载文件夹列表
                await loadFolders()
                // 关闭右键菜单
                dropdownShow.value = false
              } else {
                message.error(response.message || '文件夹删除失败')
              }
            } catch (error) {
              message.error('删除文件夹失败，请稍后重试')
              console.error('Failed to delete folder:', error)
            }
          }
        })
        break
    }
  } else if (selectedNode.value.type === 'note') {
    const noteId = selectedNode.value.key as string
    const note = notes.value.find(n => n.id === noteId)
    if (!note) return

    switch (key) {
      case 'editNote':
        handleOpenEditNoteModal(note)
        dropdownShow.value = false
        break
      case 'deleteNote':
        handleDeleteNote(noteId)
        dropdownShow.value = false
        break
      case 'exportNote':
        handleExportNote(note)
        dropdownShow.value = false
        break
    }
  }
}

// 点击外部关闭右键菜单
const dropdownClickoutsideHandler = () => {
  dropdownShow.value = false
}

// 处理笔记创建成功
const handleNoteCreated = (newNote: Note) => {
  notes.value.unshift(newNote)
  updateTreeData()
}

// 打开笔记详情
const handleNoteClick = (note: Note) => {
  currentNote.value = note
  noteFormData.title = note.title
  noteFormData.content = note.content
  noteFormData.folderId = note.folderId
  noteFormData.tags = [...note.tags]
}

// 保存编辑后的笔记
const handleEditNote = async () => {
  if (!currentNote.value) return

  try {
    // 调用更新笔记的API
    await updateNote(currentNote.value.id, {
      title: noteFormData.title,
      content: noteFormData.content,
      folderId: noteFormData.folderId || undefined,
      tags: noteFormData.tags
    })

    // 更新本地笔记列表
    const index = notes.value.findIndex(note => note.id === currentNote.value!.id)
    if (index !== -1) {
      notes.value[index] = {
        ...currentNote.value!,
        title: noteFormData.title,
        content: noteFormData.content,
        folderId: noteFormData.folderId,
        tags: [...noteFormData.tags]
      }
    }

    // 更新当前选中的笔记
    currentNote.value = {
      ...currentNote.value,
      title: noteFormData.title,
      content: noteFormData.content,
      folderId: noteFormData.folderId,
      tags: [...noteFormData.tags]
    }

    // 更新目录树
    updateTreeData()
    message.success('笔记保存成功')
  } catch (error) {
    message.error('保存笔记失败')
    console.error('Failed to update note:', error)
  }
}

// 重置表单
const resetNoteForm = () => {
  noteFormData.title = ''
  noteFormData.content = ''
  noteFormData.folderId = currentFolderId.value
  noteFormData.tags = []
}

// 组件挂载时加载数据
onMounted(async () => {
  await Promise.all([loadFolders(), loadNotes(), loadTags()])
  updateTreeData()
})

// 监听currentNote变化，当有值时（session-content渲染时）执行代码
watch(() => currentNote.value, (newNote) => {
  if (newNote) {
    nextTick(() => {
      const vMdEditorContainer = document.querySelector('.editor-body') as HTMLElement | null
      if (vMdEditorContainer) {
        const computedHeight = window.getComputedStyle(vMdEditorContainer).height
        vMdEditorHeight.value = computedHeight
      }
    })
  }
})
</script>

<template>
  <div class="markdown-div">
    <div class="content-div">
      <n-split direction="horizontal" :default-size="0.15" :max="0.4" :min="0.15">
        <!-- 左侧：笔记列表 -->
        <template #1>
          <div class="left-panel">
            <div class="panel-header">
              <h3>Markdown笔记</h3>
              <div class="toolbar">
                <n-dropdown :options="sessionDropdownOptions" @select="sessionDropdownSelectHandler">
                  <n-button size="small" type="primary" text>
                    <template #icon>
                      <NIcon>
                        <AddOutline />
                      </NIcon>
                    </template>
                  </n-button>
                </n-dropdown>
              </div>
            </div>
            <div class="tree-container">
              <!-- 目录树 -->
              <div class="folder-tree">
                <n-tree block-line expand-on-click :data="folderTreeData" :node-props="nodeProps"
                  :on-update:expanded-keys="updatePrefixWithExpaned" :selectable="false" />
              </div>
              <!-- 右键菜单 -->
              <n-dropdown trigger="manual" v-model:show="dropdownShow" :options="dropdownOptions" :x="dropdownX"
                :y="dropdownY" @select="dropdownSelectHandler" @clickoutside="dropdownClickoutsideHandler"
                placement="bottom-start" />
            </div>
          </div>
        </template>

        <!-- 右侧：编辑器面板 -->
        <template #2>
          <div class="right-panel">
            <n-empty v-if="!currentNote" description="请选择或创建一篇笔记" class="empty-state">
              <template #extra>
                <n-button size="small" type="primary" @click="handleOpenCreateNoteModal">
                  创建新笔记
                </n-button>
              </template>
            </n-empty>

            <div v-else class="session-content">
              <!-- 编辑器 -->
              <div class="editor-container">
                <div class="editor-header">
                  <n-input v-model:value="noteFormData.title" placeholder="笔记标题" class="note-title-input" />
                  <div class="editor-actions">
                    <n-button secondary size="small" @click="handleCloseNote">
                      关闭
                    </n-button>
                    <n-button type="primary" size="small" @click="handleEditNote">
                      保存
                    </n-button>
                    <n-button secondary size="small" @click="editorVisible = !editorVisible">
                      {{ editorVisible ? '预览' : '编辑' }}
                    </n-button>
                  </div>
                </div>
                <div class="editor-body">
                  <VueMarkdownEditor v-if="editorVisible" v-model="noteFormData.content" @save="handleEditNote" :height="vMdEditorHeight" />
                  <n-scrollbar v-else :style="{ height: vMdEditorHeight }">
                    <v-md-preview :text="noteFormData.content"></v-md-preview>
                  </n-scrollbar>
                </div>
              </div>
            </div>
          </div>
        </template>
      </n-split>
    </div>

    <!-- 创建文件夹模态框 -->
    <FolderCreateModal ref="folderCreateModalRef" @success="loadFolders" />
    <!-- 创建笔记模态框 -->
    <NoteCreateModal ref="noteCreateModalRef" :currentFolderId="currentFolderId" @success="handleNoteCreated" />


  </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>
