<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NImage, NCard, NTabs, NTabPane, NForm, NFormItemRow, NInput, NButton, createDiscreteApi } from 'naive-ui'
import type { FormInst, FormItemRule } from 'naive-ui'
import { login } from '@/pages/Login/service/login.ts'
import { register } from '@/pages/Login/service/register.ts'
import type { LoginDTO } from '@/pages/Login/dto/LoginDTO.ts'
import type { RegisterDTO } from '@/pages/Login/dto/RegisterDTO.ts'
import useUserStore from '@/store/modules/useUserStore.ts'
import kurumiShellImg from '@/assets/images/kurumi-shell.png'
import kurumiImg from '@/assets/images/kurumi.png'

const router = useRouter()

const { message } = createDiscreteApi(
    ['message']
)

const activeTab = ref<'signin' | 'signup'>('signin')

const loginFormRef = ref<FormInst | null>(null)

const registerFormRef = ref<FormInst | null>(null)

const loginModel = ref<LoginDTO>({
    username: '',
    password: ''
})

const registerModel = ref<RegisterDTO>({
    id: '',
    username: '',
    email: '',
    password: '',
    reenteredPassword: ''
})

const loginRules = {
    username: [
        {
            required: true,
            trigger: ['blur', 'input'],
            message: '用户名不能为空'
        }
    ],
    password: [
        {
            required: true,
            trigger: ['blur', 'input'],
            message: '密码不能为空'
        }
    ]
}

const registerRules = {
    id: [
        {
            required: true,
            trigger: ['blur', 'input'],
            message: '工号不能为空'
        }
    ],
    username: [
        {
            required: true,
            trigger: ['blur', 'input'],
            message: '用户名不能为空'
        }
    ],
    email: [
        {
            required: true,
            trigger: ['blur', 'input'],
            message: '邮箱不能为空'
        }
    ],
    password: [
        {
            required: true,
            trigger: ['blur', 'input'],
            message: '密码不能为空'
        }
    ],
    reenteredPassword: [
        {
            required: true,
            message: '请再次输入密码',
            trigger: ['input', 'blur']
        },
        {
            validator: (
                rule: FormItemRule,
                value: string
            ): boolean => {
                return (
                    !!registerModel.value.password
                    && registerModel.value.password.startsWith(value)
                    && registerModel.value.password.length >= value.length
                )
            },
            message: '两次密码输入不一致',
            trigger: 'input'
        },
        {
            validator: (rule: FormItemRule, value: string): boolean => {
                return value === registerModel.value.password
            },
            message: '两次密码输入不一致',
            trigger: ['blur', 'password-input']
        }
    ]
}

const loginButtonClickHandler = async (e: MouseEvent) => {
    e.preventDefault()
    loginFormRef.value?.validate(async (errors) => {
        if (!errors) {
            const loginRes = await login(loginModel.value.username, loginModel.value.password)
            if (loginRes) {
                const userStore = useUserStore()
                userStore.setUserInfo(loginRes.data)
                message.success('登录成功')
                router.push('/Home')
            }
        }
    })
}

const registerButtonClickHandler = async (e: MouseEvent) => {
    e.preventDefault()
    await registerFormRef.value?.validate(async (errors) => {
        if (!errors) {
            const registerRes = await register(
                registerModel.value.id,
                registerModel.value.email,
                registerModel.value.username,
                registerModel.value.password
            )
            if (registerRes) {
                message.success('注册成功，请登录')
                activeTab.value = 'signin'
                registerFormRef.value?.restoreValidation()
            }
        }
    })
}

const handleEnterKey = (e: KeyboardEvent) => {
    if (activeTab.value === 'signin') {
        loginButtonClickHandler(e as unknown as MouseEvent)
    } else if (activeTab.value === 'signup') {
        registerButtonClickHandler(e as unknown as MouseEvent)
    }
}

</script>

<template>
    <div class="login-div">
        <div class="left-div">
            <n-image preview-disabled :src="kurumiShellImg" />
        </div>
        <div class="right-div">
            <div class="right-content-div">
                <div class="login-form-div">
                    <div class="title-div">
                        <span class="title-span">
                            <n-image preview-disabled :src="kurumiImg" />
                            KurumiShell
                        </span>
                        <span class="description-span">
                            Shell一体化解决方案平台
                        </span>
                    </div>
                    <div class="form-div">
                        <n-card>
                            <n-tabs v-model:value="activeTab" class="card-tabs" size="large" animated
                                justify-content="space-evenly" pane-wrapper-style="margin: 0 -4px"
                                pane-style="padding-left: 4px padding-right: 4px box-sizing: border-box">
                                <n-tab-pane name="signin" tab="登录">
                                    <n-form ref="loginFormRef" :model="loginModel" :rules="loginRules"
                                        @keyup.enter="handleEnterKey">
                                        <n-form-item-row path="username" label="用户名">
                                            <n-input v-model:value="loginModel.username" placeholder="请输入用户名" />
                                        </n-form-item-row>
                                        <n-form-item-row path="password" label="密码">
                                            <n-input v-model:value="loginModel.password" type="password"
                                                placeholder="请输入密码" />
                                        </n-form-item-row>
                                    </n-form>
                                    <n-button type="primary" block secondary strong @click="loginButtonClickHandler">
                                        登录
                                    </n-button>
                                </n-tab-pane>
                                <n-tab-pane name="signup" tab="注册">
                                    <n-form ref="registerFormRef" :model="registerModel" :rules="registerRules"
                                        @keyup.enter="handleEnterKey">
                                        <n-form-item-row path="id" label="工号">
                                            <n-input v-model:value="registerModel.id" placeholder="请输入工号" />
                                        </n-form-item-row>
                                        <n-form-item-row path="email" label="邮箱">
                                            <n-input v-model:value="registerModel.email" placeholder="请输入邮箱" />
                                        </n-form-item-row>
                                        <n-form-item-row path="username" label="用户名">
                                            <n-input v-model:value="registerModel.username"
                                                placeholder="请输入用户名，用户名长度4-20个字符" />
                                        </n-form-item-row>
                                        <n-form-item-row path="password" label="密码">
                                            <n-input v-model:value="registerModel.password" type="password"
                                                placeholder="请输入密码，密码必须包含字母和数字，且至少8位" />
                                        </n-form-item-row>
                                        <n-form-item-row path="reenteredPassword" label="重复密码">
                                            <n-input v-model:value="registerModel.reenteredPassword" type="password"
                                                placeholder="请重复输入密码" />
                                        </n-form-item-row>
                                    </n-form>
                                    <n-button type="primary" block secondary strong @click="registerButtonClickHandler">
                                        注册
                                    </n-button>
                                </n-tab-pane>
                            </n-tabs>
                        </n-card>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>
