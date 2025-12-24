import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import type { UserDTO } from '@/dto/UserDTO.d.ts'

const mode = import.meta.env

export async function register(id: string, email: string, username: string, password: string) {
    return request<ApiResponse<UserDTO>>(mode.VITE_APP_BASE_URL + '/api/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: {
            id,
            email,
            username,
            password
        },
    })
}