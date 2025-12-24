import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import useUserStore from '@/store/modules/useUserStore.ts'

const mode = import.meta.env

/**
 * @description: 获取用户的所有SSH连接
 * @return {*} 连接列表
 */
export const getConnections = async () => {
    const userStore = useUserStore()
    return request<ApiResponse<any>>(mode.VITE_APP_BASE_URL + `/api/ssh-connections/user/${userStore.getUserInfo().id}`, {
        method: 'GET',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        }
    })
}
