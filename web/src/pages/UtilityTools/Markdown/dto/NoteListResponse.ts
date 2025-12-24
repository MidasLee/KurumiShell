import type { NoteResponse } from './NoteResponse.ts'

export interface NoteListResponse {
  data: NoteResponse[]
  total: number
  page: number
  pageSize: number
}