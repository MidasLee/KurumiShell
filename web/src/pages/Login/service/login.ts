import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import type { JwtResponse } from '@/dto/JwtResponse.d.ts'

const mode = import.meta.env

export async function login(username: string, password: string) {
  return request<ApiResponse<JwtResponse>>(mode.VITE_APP_BASE_URL + '/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: {
      username,
      password,
    },
  })
}