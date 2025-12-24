import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import { SSHConnectionDTO } from '@/pages/UtilityTools/SSH/dto/SSHConnection.ts'
import useUserStore from '@/store/modules/useUserStore.ts'

const mode = import.meta.env

/**
 * @description: 添加SSH连接
 * @param {SSHConnectionDTO} connection 连接信息
 * @return {*} 连接信息
 */
export const addConnection = async (connection: SSHConnectionDTO) => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  // 直接发送连接数据，URL路径参数中的user_id将被后端用于关联用户
  return request<ApiResponse<any>>(mode.VITE_APP_BASE_URL + `/api/ssh-connections/user/${userInfo.id}`, {
    method: 'POST',
    headers: {
      'Authorization': userInfo.type + ' ' + userInfo.token,
      'Content-Type': 'application/json',
    },
    data: connection,
  })
}