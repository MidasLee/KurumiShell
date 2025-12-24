<script setup lang="ts">
import { ref } from 'vue'
import { NModal, NCard, NInput, NIcon, NButton, NForm, NFormItem, NSelect, createDiscreteApi } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { Close } from '@vicons/carbon'
import { CreateFolderRequest } from '../../dto/CreateFolderRequest.ts'
import { createFolder } from '../../service/folderService.ts'

const { message } = createDiscreteApi(['message'])

const emit = defineEmits(['success'])

const visible = ref(false)
const folderOptions = ref<{ label: string; value: string }[]>([])

const form = ref<CreateFolderRequest>({
  name: '',
  parentId: undefined
})

const rules = ref({
  name: [
    { required: true, message: '请输入文件夹名称', trigger: 'blur' }
  ]
})
const formRef = ref<FormInst | null>(null)

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
}

const submitHandler = () => {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      try {
        const response = await createFolder(form.value)
        if (response.code === 200) {
          message.success(response.message || '文件夹创建成功')
          form.value = {
            name: '',
            parentId: undefined
          }
          visible.value = false
          emit('success')
        } else {
          message.error(response.message || '文件夹创建失败')
        }
      } catch (error: any) {
        console.error('文件夹创建失败:', error)
        message.error(error.response?.data?.message || '文件夹创建失败')
      }
    }
  })
}

// 暴露方法
defineExpose({
  show: (parentId: string | undefined = undefined) => {
    form.value.parentId = parentId
    visible.value = true
  },
  initFolderOptions
})
</script>

<template>
  <n-modal v-model:show="visible" style="width: 400px">
    <n-card style="width: 600px" title="新建文件夹" :bordered="false" size="huge" role="dialog" aria-modal="true">
      <template #header-extra>
        <n-button text @click="visible = false" style="font-size: 24px">
          <n-icon>
            <Close />
          </n-icon>
        </n-button>
      </template>
      <n-form :model="form" :rules="rules" ref="formRef">
        <n-form-item label="文件夹名称" path="name">
        <n-input v-model:value="form.name" placeholder="请输入文件夹名称" />
      </n-form-item>
      <n-form-item label="父级文件夹">
        <n-select v-model:value="form.parentId" :options="folderOptions" placeholder="选择父级文件夹" />
      </n-form-item>
      </n-form>
      <template #footer>
        <n-button type="primary" @click="submitHandler">确定</n-button>
      </template>
    </n-card>
  </n-modal>
</template>

<style lang="scss" scoped>
</style>