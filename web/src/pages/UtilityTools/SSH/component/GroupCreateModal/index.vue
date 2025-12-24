<script setup lang="ts">
import { ref } from 'vue'
import { NModal, NCard, NInput, NIcon, NButton, NForm, NFormItem, createDiscreteApi } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { Close } from '@vicons/carbon'
import { SSHGroupDTO } from '@/pages/UtilityTools/SSH/dto/SSHGroup.ts'
import { addGroup } from '@/pages/UtilityTools/SSH/service/addGroup.ts'

const { message } = createDiscreteApi(
    ['message']
)

const emit = defineEmits(['success'])

const visible = ref(false)

const form = ref<SSHGroupDTO>({
    name: '',
    description: ''
})

const rules = ref({
    name: [
        { required: true, message: '请输入分组名称', trigger: 'blur' }
    ],
    description: [
        { required: true, message: '请输入分组描述', trigger: 'blur' }
    ]
})
const formRef = ref<FormInst | null>(null)

const submitHandler = () => {
    formRef.value?.validate(async (errors) => {
        if (!errors) {
            try {
                await addGroup(form.value)
                message.success('分组创建成功')
                form.value = {
                    name: '',
                    description: ''
                }
                visible.value = false
                emit('success')
            } catch (error: any) {
                console.error('分组创建失败:', error)
                message.error(error.response.data.message)
            }
        }
    })
}

// 暴露方法
defineExpose({
    show: () => visible.value = true
})
</script>

<template>
    <n-modal v-model:show="visible" style="width: 400px">
        <n-card style="width: 600px" title="新建分组" :bordered="false" size="huge" role="dialog" aria-modal="true">
            <template #header-extra>
                <n-button text @click="visible = false" style="font-size: 24px">
                    <n-icon>
                        <Close />
                    </n-icon>
                </n-button>
            </template>
            <n-form :model="form" :rules="rules" ref="formRef">
                <n-form-item label="分组名称" path="name">
                    <n-input v-model:value="form.name" />
                </n-form-item>
                <n-form-item label="分组描述" path="description">
                    <n-input v-model:value="form.description" type="textarea" :autosize="{
                        minRows: 3,
                        maxRows: 5,
                    }" />
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
