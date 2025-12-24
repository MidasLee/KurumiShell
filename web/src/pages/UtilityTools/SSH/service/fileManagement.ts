import { request } from '@/network/axios.ts'
import type { ApiResponse } from '@/dto/ApiResponse.d.ts'
import type { FileInfoDTO } from '@/pages/UtilityTools/SSH/dto/FileInfo.ts'
import useUserStore from '@/store/modules/useUserStore.ts'

const mode = import.meta.env

/**
 * @description: 获取文件列表
 * @param {string} connectionId SSH连接ID
 * @param {string} path 目录路径
 * @return {*} 文件列表
 */
export const getFileList = async (connectionId: string, path: string = '/') => {
    const userStore = useUserStore()
    return request<ApiResponse<FileInfoDTO[]>>(mode.VITE_APP_BASE_URL + `/api/ssh-files/${connectionId}/user/${userStore.getUserInfo().id}/list`, {
        method: 'POST',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        },
        data: { path },
        timeout: 30000 // 增加超时时间到30秒
    })
}

/**
 * @description: 读取文件内容
 * @param {string} connectionId SSH连接ID
 * @param {string} filePath 文件路径
 * @return {*} 文件内容
 */
export const readFile = async (connectionId: string, filePath: string) => {
    const userStore = useUserStore()
    return request<ApiResponse<string>>(mode.VITE_APP_BASE_URL + `/api/ssh-files/${connectionId}/user/${userStore.getUserInfo().id}/read`, {
        method: 'POST',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        },
        data: { path: filePath },
        timeout: 30000 // 增加超时时间到30秒
    })
}

/**
 * @description: 写入文件内容
 * @param {string} connectionId SSH连接ID
 * @param {string} filePath 文件路径
 * @param {string} content 文件内容
 * @return {*} 操作结果
 */
export const writeFile = async (connectionId: string, filePath: string, content: string) => {
    const userStore = useUserStore()
    return request<ApiResponse<any>>(mode.VITE_APP_BASE_URL + `/api/ssh-files/${connectionId}/user/${userStore.getUserInfo().id}/write`, {
        method: 'POST',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        },
        data: { path: filePath, content },
        timeout: 30000 // 增加超时时间到30秒
    })
}

/**
 * @description: 创建目录
 * @param {string} connectionId SSH连接ID
 * @param {string} dirPath 目录路径
 * @return {*} 操作结果
 */
export const createDirectory = async (connectionId: string, dirPath: string) => {
    const userStore = useUserStore()
    return request<ApiResponse<any>>(mode.VITE_APP_BASE_URL + `/api/ssh-files/${connectionId}/user/${userStore.getUserInfo().id}/mkdir`, {
        method: 'POST',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        },
        data: { path: dirPath },
        timeout: 30000 // 增加超时时间到30秒
    })
}

/**
 * @description: 删除文件或目录
 * @param {string} connectionId SSH连接ID
 * @param {string} path 文件或目录路径
 * @return {*} 操作结果
 */
export const deleteFile = async (connectionId: string, path: string) => {
    const userStore = useUserStore()
    return request<ApiResponse<any>>(mode.VITE_APP_BASE_URL + `/api/ssh-files/${connectionId}/user/${userStore.getUserInfo().id}/delete`, {
        method: 'POST',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        },
        data: { path },
        timeout: 30000 // 增加超时时间到30秒
    })
}

/**
 * @description: 重命名文件或目录
 * @param {string} connectionId SSH连接ID
 * @param {string} sourcePath 源路径
 * @param {string} targetPath 目标路径
 * @return {*} 操作结果
 */
export const renameFile = async (connectionId: string, sourcePath: string, targetPath: string) => {
    const userStore = useUserStore()
    return request<ApiResponse<any>>(mode.VITE_APP_BASE_URL + `/api/ssh-files/${connectionId}/user/${userStore.getUserInfo().id}/rename`, {
        method: 'POST',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        },
        data: { sourcePath, targetPath },
        timeout: 30000 // 增加超时时间到30秒
    })
}

/**
 * @description: 上传文件
 * @param {string} connectionId SSH连接ID
 * @param {string} remotePath 远程路径
 * @param {File} file 文件对象
 * @param {Function} onProgress 上传进度回调函数
 * @return {*} 操作结果
 */
export const uploadFile = async (connectionId: string, remotePath: string, file: File, onProgress?: (percentage: number) => void) => {
    const userStore = useUserStore()
    const formData = new FormData()
    formData.append('file', file)
    
    // 获取文件的相对路径（如果是文件夹上传，file.webkitRelativePath会包含文件夹结构）
    const relativePath = (file as any).webkitRelativePath
    
    if (relativePath) {
        // 文件夹上传情况：使用相对路径构建完整的远程路径
        const fullRemotePath = remotePath.endsWith('/') ? `${remotePath}${relativePath}` : `${remotePath}/${relativePath}`
        formData.append('remotePath', fullRemotePath)
    } else {
        // 单个文件上传情况：保持原有逻辑
        const fullRemotePath = remotePath.endsWith('/') ? `${remotePath}${file.name}` : `${remotePath}/${file.name}`
        formData.append('remotePath', fullRemotePath)
    }
    
    return request<ApiResponse<any>>(mode.VITE_APP_BASE_URL + `/api/ssh-files/${connectionId}/user/${userStore.getUserInfo().id}/upload`, {
        method: 'POST',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
        },
        data: formData,
        timeout: 60000, // 上传文件需要更长时间，设置为60秒
        onUploadProgress: onProgress ? (progressEvent) => {
            const percentage = progressEvent.total ? Math.round((progressEvent.loaded * 100) / progressEvent.total) : 0
            onProgress(percentage)
        } : undefined
    })
}

/**
 * @description: 下载文件
 * @param {string} connectionId SSH连接ID
 * @param {string} filePath 文件路径
 * @return {*} 文件Blob
 */
export const downloadFile = async (connectionId: string, filePath: string) => {
    const userStore = useUserStore()
    return request<Blob>(mode.VITE_APP_BASE_URL + `/api/ssh-files/${connectionId}/user/${userStore.getUserInfo().id}/download`, {
        method: 'POST',
        headers: {
            'Authorization': userStore.getUserInfo().type + ' ' + userStore.getUserInfo().token,
            'Content-Type': 'application/json',
        },
        data: { path: filePath },
        responseType: 'blob',
        timeout: 60000 // 下载文件需要更长时间，设置为60秒
    })
}