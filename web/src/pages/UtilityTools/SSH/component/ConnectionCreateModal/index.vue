<script setup lang="ts">
import { ref } from 'vue'
import { NModal, NCard, NInput, NInputNumber, NSelect, NButton, NIcon, NForm, NFormItem, createDiscreteApi } from 'naive-ui'
import type { FormInst, FormItemRule } from 'naive-ui'
import { Close } from '@vicons/ionicons5'
import { SSHConnectionDTO } from '@/pages/UtilityTools/SSH/dto/SSHConnection.ts'
import { addConnection } from '@/pages/UtilityTools/SSH/service/addConnection.ts'

const { message } = createDiscreteApi(['message'])

const emit = defineEmits(['success'])

const visible = ref(false)
const formRef = ref<FormInst>()
const form = ref<SSHConnectionDTO>({
    name: '',
    groupId: '', // 初始为空，会在设置groupOptions时更新
    host: '',
    port: 22,
    username: '',
    password: '',
    privateKey: '',
    keyPassphrase: ''
})

const rules = ref<Record<string, FormItemRule[]>>({
    name: [{ required: true, message: '请输入连接名称', trigger: 'blur' }],
    groupId: [{ required: true, message: '请输入分组', trigger: 'blur' }],
    host: [{ required: true, message: '请输入主机', trigger: 'blur' }],
    port: [{ type: 'number', required: true, message: '请输入端口', trigger: ['blur', 'change'] }],
    username: [
        {
            validator: (rule: any, value: string) => {
                // 如果用户名和私钥都为空，则报错
                if (!value && !form.value.privateKey) {
                    return new Error('用户名和私钥必须至少输入一个')
                }
                // 如果输入了用户名但密码为空，则报错
                if (value && !form.value.password) {
                    return new Error('请输入密码')
                }
                return true
            },
            trigger: 'blur'
        }
    ],
    password: [
        {
            validator: (rule: any, value: string) => {
                // 如果输入了用户名但密码为空，则报错
                if (form.value.username && !value) {
                    return new Error('请输入密码')
                }
                return true
            },
            trigger: 'blur'
        }
    ],
    privateKey: [
        {
            validator: (rule: any, value: string) => {
                // 如果用户名和私钥都为空，则报错
                if (!value && !form.value.username) {
                    return new Error('用户名和私钥必须至少输入一个')
                }
                // 如果输入了私钥但私钥密码为空，则报错
                if (value && !form.value.keyPassphrase) {
                    return new Error('请输入私钥密码')
                }
                return true
            },
            trigger: 'blur'
        }
    ],
    keyPassphrase: [
        {
            validator: (rule: any, value: string) => {
                // 如果输入了私钥但私钥密码为空，则报错
                if (form.value.privateKey && !value) {
                    return new Error('请输入私钥密码')
                }
                return true
            },
            trigger: 'blur'
        }
    ]
})

const groupOptions = ref<{ label: string, value: string }[]>([])

const submitHandler = async () => {
    formRef.value?.validate(async (errors) => {
        if (!errors) {
            try {
                await addConnection(form.value)
                message.success('连接创建成功')
                visible.value = false
                // 重置表单
                form.value = {
                    name: '',
                    groupId: groupOptions.value.length > 0 ? groupOptions.value[0].value : '',
                    host: '',
                    port: 22,
                    username: '',
                    password: '',
                    privateKey: '',
                    keyPassphrase: ''
                }
                formRef.value?.restoreValidation()
                // 触发父组件更新列表
                emit('success')
            } catch (error: any) {
                console.error('创建SSH连接失败:', error)
                message.error(error.response?.data?.message || '创建失败')
            }
        }
    })
}

// 暴露方法
defineExpose({
    show: (selectedNodeKey: string) => {
        if (selectedNodeKey) {
            form.value.groupId = selectedNodeKey
        } else {
            form.value.groupId = groupOptions.value[0].value
        }
        visible.value = true
    },
    initGroupOptions: (options: any) => {
        groupOptions.value = options.map((item: any) => ({
            label: item.name,
            value: item.id
        }))
        // 如果有选项，设置第一个选项为默认值
        if (groupOptions.value.length > 0 && !form.value.groupId) {
            form.value.groupId = groupOptions.value[0].value
        }
    }
})
</script>

<template>
    <n-modal v-model:show="visible" style="width: 400px">
        <n-card style="width: 600px" title="新建连接" :bordered="false" size="huge" role="dialog" aria-modal="true">
            <template #header-extra>
                <n-button text @click="visible = false" style="font-size: 24px">
                    <n-icon>
                        <Close />
                    </n-icon>
                </n-button>
            </template>
            <n-form :model="form" :rules="rules" ref="formRef">
                <n-form-item label="连接名称" path="name">
                    <n-input v-model:value="form.name" />
                </n-form-item>
                <n-form-item label="分组" path="groupId">
                    <n-select v-model:value="form.groupId" :options="groupOptions" />
                </n-form-item>
                <n-form-item label="主机" path="host">
                    <n-input v-model:value="form.host" />
                </n-form-item>
                <n-form-item label="端口" path="port">
                    <n-input-number v-model:value="form.port" />
                </n-form-item>
                <n-form-item label="用户名" path="username">
                    <n-input v-model:value="form.username" />
                </n-form-item>
                <n-form-item label="密码" path="password">
                    <n-input v-model:value="form.password" type="password" />
                </n-form-item>
                <n-form-item label="私钥" path="privateKey">
                    <n-input v-model:value="form.privateKey" />
                </n-form-item>
                <n-form-item label="私钥密码" path="keyPassphrase">
                    <n-input v-model:value="form.keyPassphrase" />
                </n-form-item>
            </n-form>
            <template #footer>
                <n-button type="primary" @click="submitHandler">确定</n-button>
            </template>
        </n-card>
    </n-modal>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>