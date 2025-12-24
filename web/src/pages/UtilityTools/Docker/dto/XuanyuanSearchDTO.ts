export type XuanyuanSearchDTO = {
  results: XuanyuanImageResultDTO[]
  total: number
  page: number
  limit: number
  totalPages: number
  hasMore: boolean
}

export type XuanyuanImageResultDTO = {
  id: string
  name: string
  slug: string
  short_description: string
  badge: string
  star_count: number
  pull_count: string
  raw_pull_count: number
  score: number
  created_at: string
  updated_at: string
  architectures: Array<{
    name: string
    label: string
  }>
  operating_systems: Array<{
    name: string
    label: string
  }>
  publisher: {
    name: string
    id: string
  }
  logo_url: {
    large: string
    small: string
  }
  archived: boolean
  media_types: string[]
  content_types: string[]
}
