export type FileInfoDTO = {
    name: string
    path: string
    type: 'file' | 'directory'
    size: number
    permissions: string
    modifiedTime: string
    owner: string
    group: string
}

export type FileOperationRequest = {
    connectionId: string
    sourcePath: string
    targetPath?: string
    content?: string
    file?: File
}