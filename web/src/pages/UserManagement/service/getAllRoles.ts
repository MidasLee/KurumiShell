import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import useUserStore from '@/store/modules/useUserStore.ts'
import type { RoleDTO } from '@/dto/RoleDTO.d.ts'

const mode = import.meta.env

export async function getAllRoles() {
    const userStore = useUserStore()
    return request<ApiResponse<RoleDTO[]>>(mode.VITE_APP_BASE_URL + '/api/roles', {
        method: 'GET',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        },
    })
}
