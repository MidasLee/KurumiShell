import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import type { Folder } from '../dto/Folder.ts'
import type { FolderResponse } from '../dto/FolderResponse.ts'
import type { CreateFolderRequest } from '../dto/CreateFolderRequest.ts'
import type { UpdateFolderRequest } from '../dto/UpdateFolderRequest.ts'
import useUserStore from '@/store/modules/useUserStore.ts'

const mode = import.meta.env

// 获取文件夹列表
export const getFolderList = () => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<FolderResponse[]>>(mode.VITE_APP_BASE_URL + `/api/markdown/folders/user/${userInfo.id}`, {
    method: 'GET',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
  })
}

// 创建文件夹
export const createFolder = (data: CreateFolderRequest) => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<Folder>>(mode.VITE_APP_BASE_URL + `/api/markdown/folders/user/${userInfo.id}`, {
    method: 'POST',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
    data,
  })
}

// 更新文件夹
export const updateFolder = (id: string, data: UpdateFolderRequest) => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<Folder>>(mode.VITE_APP_BASE_URL + `/api/markdown/folders/${id}/user/${userInfo.id}`, {
    method: 'PUT',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
    data,
  })
}

// 删除文件夹
export const deleteFolder = (id: string) => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<void>>(mode.VITE_APP_BASE_URL + `/api/markdown/folders/${id}/user/${userInfo.id}`, {
    method: 'DELETE',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
  })
}


