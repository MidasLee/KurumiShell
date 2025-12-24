import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import useUserStore from '@/store/modules/useUserStore.ts'

const mode = import.meta.env

export async function disableUser(userId: string) {
    const userStore = useUserStore()
    return request<ApiResponse<any>>(mode.VITE_APP_BASE_URL + '/api/auth/disable-user/' + userId, {
        method: 'POST',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        }
    })
}