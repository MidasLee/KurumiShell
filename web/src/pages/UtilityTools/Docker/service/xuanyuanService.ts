import { request } from '@/network/axios.ts'
import type { XuanyuanSearchDTO } from '@/pages/UtilityTools/Docker/dto/XuanyuanSearchDTO.ts'
import type { XuanyuanImageTagsDTO } from '@/pages/UtilityTools/Docker/dto/XuanyuanImageTagsDTO.ts'

const mode = import.meta.env

/**
 * @description: 搜索Docker镜像
 * @param {Object} params 搜索参数
 * @return {*} 搜索结果
 */
export const searchImages = async (params: {
  imageName: string
  page?: number
  pageSize?: number
}): Promise<XuanyuanSearchDTO> => {
  const { imageName, page = 1, pageSize = 10 } = params
  return request<XuanyuanSearchDTO>(mode.VITE_APP_XUANYUAN_URL + '/api/xuanyuan/v2/search', {
    method: 'GET',
    params: {
      image_name: imageName,
      page,
      page_size: pageSize
    }
  })
}

/**
 * @description: 获取镜像标签
 * @param {Object} params 镜像参数
 * @return {*} 镜像标签
 */
export const getImageTags = async (params: {
  namespace: string
  name: string
  tag?: string
}): Promise<XuanyuanImageTagsDTO> => {
  const { namespace, name, tag } = params
  return request<XuanyuanImageTagsDTO>(mode.VITE_APP_XUANYUAN_URL + '/api/xuanyuan/v2/image_tags', {
    method: 'GET',
    params: {
      namespace,
      name,
      tag
    }
  })
}