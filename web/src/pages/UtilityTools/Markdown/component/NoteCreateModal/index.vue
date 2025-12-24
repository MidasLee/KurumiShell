<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue'
import { NModal, NCard, NInput, NIcon, NButton, NForm, NFormItem, NSelect, createDiscreteApi } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { Close } from '@vicons/carbon'
import { createNote } from '@/pages/UtilityTools/Markdown/service/noteService.ts'
import type { Note } from '@/pages/UtilityTools/Markdown/dto/Note.ts'

const { message } = createDiscreteApi(['message'])

const emit = defineEmits(['success'])

const props = defineProps<{
  currentFolderId: string | null
}>()

const visible = ref(false)
const folderId = ref<string | null>(null)
const vMdEditorHeight = ref('300px')
const folderOptions = ref<{ label: string; value: string }[]>([])

const form = reactive<{ title: string; content: string }>({
  title: '',
  content: ''
})

const formRef = ref<FormInst | null>(null)

const show = () => {
  // 如果有当前文件夹ID，则使用它，否则使用第一个文件夹选项
  folderId.value = props.currentFolderId || (folderOptions.value.length > 0 ? folderOptions.value[0].value : null)
  form.title = ''
  form.content = ''
  visible.value = true
  // 模态框打开后执行高度计算
  nextTick(() => {
    const vMdEditorContainer = document.querySelector('.v-md-editor-container') as HTMLElement | null;
    if (vMdEditorContainer) {
      const computedHeight = window.getComputedStyle(vMdEditorContainer).height
      const height = computedHeight.replace('px', '') as unknown as number - 54 + 'px'
      vMdEditorHeight.value = height
    }
    const vMdEditorToolbarItemFullscreen = document.querySelector('.v-md-editor__toolbar-item-fullscreen') as HTMLElement | null
    if (vMdEditorToolbarItemFullscreen) {
      vMdEditorToolbarItemFullscreen.remove()
    }
    const vMdEditorToolbarItemSave = document.querySelector('.v-md-editor__toolbar-item-save') as HTMLElement | null
    if (vMdEditorToolbarItemSave) {
      vMdEditorToolbarItemSave.remove()
    }
  });
}

const showInFolder = (inFolderId: string) => {
  folderId.value = inFolderId
  form.title = ''
  form.content = ''
  visible.value = true
  // 模态框打开后执行高度计算
  nextTick(() => {
    const vMdEditorContainer = document.querySelector('.v-md-editor-container') as HTMLElement | null;
    if (vMdEditorContainer) {
      const computedHeight = window.getComputedStyle(vMdEditorContainer).height
      const height = computedHeight.replace('px', '') as unknown as number - 54 + 'px'
      vMdEditorHeight.value = height
    }
  });
}

// 递归处理嵌套文件夹，生成带路径的选项
const processFolderOptions = (folders: any[], parentPath: string = ''): { label: string; value: string }[] => {
  return folders.flatMap(folder => {
    // 构建当前文件夹的完整路径
    const currentPath = parentPath ? `${parentPath}/${folder.name}` : folder.name
    
    // 当前文件夹选项
    const currentOption = {
      label: currentPath,
      value: folder.id
    }
    
    // 如果有子文件夹，递归处理并合并到结果中
    if (folder.children && folder.children.length > 0) {
      return [currentOption, ...processFolderOptions(folder.children, currentPath)]
    }
    
    return [currentOption]
  })
}

// 初始化文件夹选项
const initFolderOptions = (options: any) => {
  folderOptions.value = processFolderOptions(options)
  // 如果有选项且没有设置folderId，则设置第一个选项为默认值
  if (folderOptions.value.length > 0 && !folderId.value) {
    folderId.value = folderOptions.value[0].value
  }
}

const submitHandler = async () => {
  try {
    const newNote = {
      title: form.title || '未命名笔记',
      content: form.content,
      folderId: folderId.value || undefined,
      tags: []
    }

    const response = await createNote(newNote)

    if (response.data) {
      visible.value = false
      emit('success', response.data)
      message.success('笔记创建成功')
    }
  } catch (error) {
    message.error('创建笔记失败')
    console.error('Failed to create note:', error)
  }
}



// 暴露方法
defineExpose({
  show,
  showInFolder,
  initFolderOptions
})
</script>

<template>
  <n-modal v-model:show="visible" class="note-create-modal" preset="card" title="创建新笔记" footer-style="display: flex; justify-content: flex-start;gap: 12px"
    style="width: 80vw; height: 80vh;">
    <n-form ref="formRef">
      <n-form-item label="标题">
        <n-input v-model:value="form.title" placeholder="输入笔记标题" />
      </n-form-item>
      <n-form-item label="文件夹">
        <n-select v-model:value="folderId" :options="folderOptions" placeholder="选择文件夹" />
      </n-form-item>
      <n-form-item class="v-md-editor-container" label="内容" style="height: 100%">
        <VueMarkdownEditor v-model="form.content" :height="vMdEditorHeight" />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-button @click="visible = false">取消</n-button>
      <n-button type="primary" @click="submitHandler">创建</n-button>
    </template>
  </n-modal>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>