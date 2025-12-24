export interface NoteResponse {
  id: string
  title: string
  content: string
  tags: string[]
  createdAt: string
  updatedAt: string
  folderId: string | null
  folderName?: string
}