import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import { SSHGroupDTO } from '@/pages/UtilityTools/SSH/dto/SSHGroup.ts'
import useUserStore from '@/store/modules/useUserStore.ts'

const mode = import.meta.env

/**
 * @description: 添加分组
 * @param {SSHGroupDTO} group 分组信息
 * @return {*} 分组信息
 */
export const addGroup = async (group: SSHGroupDTO) => {
  const userStore = useUserStore()
  return request<ApiResponse<any>>(mode.VITE_APP_BASE_URL + `/api/ssh-groups/user/${userStore.getUserInfo().id}`, {
    method: 'POST',
    headers: {
      'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
      'Content-Type': 'application/json',
    },
    data: group,
  })
}