export interface NoteSearchParams {
  keyword?: string
  folderId?: string
  tags?: string[]
  sortBy?: 'createdAt' | 'updatedAt' | 'title'
  sortOrder?: 'asc' | 'desc'
  page?: number
  pageSize?: number
}