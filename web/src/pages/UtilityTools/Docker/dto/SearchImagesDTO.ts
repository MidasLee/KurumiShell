export type SearchImagesDTO = {
  count: number
  results: ImageResultDTO[]
}

export type ImageResultDTO = {
  icon_path: string
  image_name: string
  platform: string
  source: string
  size: string
  collection_date: string
}
