export interface FolderResponse {
  id: string
  name: string
  createdAt: string
  parentId: string | null
  children?: FolderResponse[]
}