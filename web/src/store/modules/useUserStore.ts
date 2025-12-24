import { defineStore } from 'pinia'
import { ref } from 'vue'
import { createDiscreteApi } from 'naive-ui'
import type { JwtResponse } from '@/dto/JwtResponse.d.ts'

const { message } = createDiscreteApi(
    ['message']
)

const useUserStore = defineStore(
    'user',
    () => {
        const userInfo = ref<JwtResponse>()
        const setUserInfo = (value: JwtResponse) => {
            userInfo.value = value
        }
        const getUserInfo = (): JwtResponse | undefined => {
            if (userInfo.value) {
                return userInfo.value
            } else {
                message.error('无法获取登录状态信息，请先登录')
                window.location.href = '/#/Login'
                return undefined
            }
        }
        const removeUserInfo = () => {
            userInfo.value = undefined
        }
        return {
            userInfo,
            setUserInfo,
            getUserInfo,
            removeUserInfo
        }
    },
    {
        persist: true,
    }
)

export default useUserStore