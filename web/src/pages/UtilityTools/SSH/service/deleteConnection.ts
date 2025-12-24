import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import useUserStore from '@/store/modules/useUserStore.ts'

const mode = import.meta.env

/**
 * @description: 删除连接会话
 * @param {string} connectionId 连接ID
 * @return {*} 删除结果
 */
export const deleteConnection = async (connectionId: string) => {
    const userStore = useUserStore()
    return request<ApiResponse<any>>(mode.VITE_APP_BASE_URL + `/api/ssh-connections/${connectionId}/user/${userStore.getUserInfo().id}`, {
        method: 'DELETE',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        }
    })
}