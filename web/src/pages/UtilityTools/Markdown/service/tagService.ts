import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import useUserStore from '@/store/modules/useUserStore.ts'

const mode = import.meta.env

// 获取所有标签
export const getAllTags = () => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<string[]>>(mode.VITE_APP_BASE_URL + `/api/markdown/tags/user/${userInfo.id}`, {
    method: 'GET',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
  })
}

// 获取标签统计
export const getTagStats = () => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<Map<string, number>>>(mode.VITE_APP_BASE_URL + `/api/markdown/tags/stats/user/${userInfo.id}`, {
    method: 'GET',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
  })
}
