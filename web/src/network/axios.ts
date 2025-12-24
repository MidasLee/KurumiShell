import axios, { AxiosRequestConfig } from 'axios'
import { createDiscreteApi } from 'naive-ui'
import useUserStore from '@/store/modules/useUserStore.ts'

const { message } = createDiscreteApi(
  ['message']
)

axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8'

const axiosInstance = axios.create({
  timeout: 10000,
})

axiosInstance.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  },
)

axiosInstance.interceptors.response.use(
  response => {
    if (response?.status === 200 || response?.status === 201) {
      return Promise.resolve(response.data)
    } else {
      return Promise.reject(response)
    }
  },
  error => {
    if (error?.message?.includes?.('timeout')) {
      message.error('请求超时')
    } else if (error.code === 'ECONNABORTED') {
      message.error('请求超时')
    } else if (error.response) {
      // 服务器有响应，但状态码不是 2xx
      message.error(error.response.data.message || '服务器错误')
      if (error.response.status === 401) {
        const userStore = useUserStore()
        userStore.removeUserInfo()
        window.location.href = '/#/Login'
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      message.error('无法连接到服务器，请检查网络或后端服务是否正常')
    } else {
      // 其他错误
      message.error('请求失败: ' + error.message)
    }
    return Promise.reject(error)
  },
)

const request = <ResponseType = unknown>(
  url: string,
  options?: AxiosRequestConfig<unknown> & {
    onUploadProgress?: (progressEvent: ProgressEvent) => void
  },
): Promise<ResponseType> => {
  return new Promise((resolve, reject) => {
    axiosInstance({
      url,
      ...options,
      onUploadProgress: options?.onUploadProgress,
    })
      .then((res: any) => {
        // 拦截器已经将响应转换为response.data
        resolve(res as ResponseType)
      })
      .catch(err => reject(err))
  })
}
export { axiosInstance, request }