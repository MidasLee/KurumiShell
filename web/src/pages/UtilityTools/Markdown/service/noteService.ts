import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import type { Note } from '../dto/Note.ts'
import type { NoteResponse } from '../dto/NoteResponse.ts'
import type { NoteSearchParams } from '../dto/NoteSearchParams.ts'
import type { NoteListResponse } from '../dto/NoteListResponse.ts'
import type { CreateNoteRequest } from '../dto/CreateNoteRequest.ts'
import type { UpdateNoteRequest } from '../dto/UpdateNoteRequest.ts'
import useUserStore from '@/store/modules/useUserStore.ts'

const mode = import.meta.env

// 获取笔记列表
export const getNoteList = (params?: NoteSearchParams) => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<NoteListResponse>>(mode.VITE_APP_BASE_URL + `/api/markdown/notes/user/${userInfo.id}`, {
    method: 'GET',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
    params,
  })
}

// 获取单条笔记详情
export const getNoteDetail = (id: string) => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<NoteResponse>>(mode.VITE_APP_BASE_URL + `/api/markdown/notes/${id}/user/${userInfo.id}`, {
    method: 'GET',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
  })
}

// 创建笔记
export const createNote = (data: CreateNoteRequest) => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<Note>>(mode.VITE_APP_BASE_URL + `/api/markdown/notes/user/${userInfo.id}`, {
    method: 'POST',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
    data,
  })
}

// 更新笔记
export const updateNote = (id: string, data: UpdateNoteRequest) => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<Note>>(mode.VITE_APP_BASE_URL + `/api/markdown/notes/${id}/user/${userInfo.id}`, {
    method: 'PUT',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
    data,
  })
}

// 删除笔记
export const deleteNote = (id: string) => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<void>>(mode.VITE_APP_BASE_URL + `/api/markdown/notes/${id}/user/${userInfo.id}`, {
    method: 'DELETE',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
  })
}

// 批量删除笔记
export const batchDeleteNotes = (ids: string[]) => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  return request<ApiResponse<void>>(mode.VITE_APP_BASE_URL + `/api/markdown/notes/batch/user/${userInfo.id}`, {
    method: 'DELETE',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
    data: ids,
  })
}
