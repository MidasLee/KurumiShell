export type XuanyuanImageTagsDTO = {
  results: Array<{
    name: string
    full_size: number
    v2: boolean
    digest: string
    architecture: string
    os: string
    status: string
    last_pushed: string
    last_pulled: string
    tag_status: string
  }>
  total: number
  page: number
  limit: number
  totalPages: number
  hasMore: boolean
}
