import { request } from '@/network/axios.ts'
import type { SearchImagesDTO } from '@/pages/UtilityTools/Docker/dto/SearchImagesDTO.ts'
import type { ImageInfoDTO } from '@/pages/UtilityTools/Docker/dto/ImageInfoDTO.ts'

const mode = import.meta.env

/**
 * @description: 搜索Docker镜像
 * @param {Object} params 搜索参数
 * @return {*} 搜索结果
 */
export const searchImages = async (params: {
  search: string
  site?: string
  platform?: string
  sort?: string
  page?: number
  page_size?: number
}): Promise<SearchImagesDTO> => {
  const { search, site, platform, sort, page = 1, page_size = 10 } = params
  return request<SearchImagesDTO>(mode.VITE_APP_DUDUBIRD_URL + '/api/dudubird/search_images', {
    method: 'GET',
    params: {
      search,
      site,
      platform,
      sort,
      page,
      page_size
    }
  })
}

/**
 * @description: 获取镜像详情
 * @param {Object} params 镜像参数
 * @return {*} 镜像详情
 */
export const getImageInfo = async (params: {
  image_name: string
}): Promise<ImageInfoDTO> => {
  const { image_name } = params
  return request<ImageInfoDTO>(mode.VITE_APP_DUDUBIRD_URL + '/api/dudubird/image_info', {
    method: 'GET',
    params: {
      image_name
    }
  })
}