export interface CreateNoteRequest {
  title: string
  content: string
  tags: string[]
  folderId?: string
}