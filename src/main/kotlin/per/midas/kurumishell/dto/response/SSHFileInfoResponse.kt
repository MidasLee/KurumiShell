package per.midas.kurumishell.dto.response

import java.time.Instant

data class SSHFileInfoResponse(
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val permissions: String,
    val owner: String,
    val group: String,
    val modifiedTime: Instant,
    val canRead: Boolean,
    val canWrite: Boolean,
    val canExecute: Boolean
) {
    companion object {
        fun fromFileInfo(fileInfo: Map<String, Any>): SSHFileInfoResponse {
            return SSHFileInfoResponse(
                name = fileInfo["name"] as String,
                path = fileInfo["path"] as String,
                size = (fileInfo["size"] as? Number)?.toLong() ?: 0,
                isDirectory = fileInfo["isDirectory"] as? Boolean ?: false,
                permissions = fileInfo["permissions"] as? String ?: "",
                owner = fileInfo["owner"] as? String ?: "",
                group = fileInfo["group"] as? String ?: "",
                modifiedTime = fileInfo["modifiedTime"] as? Instant ?: Instant.now(),
                canRead = fileInfo["canRead"] as? Boolean ?: false,
                canWrite = fileInfo["canWrite"] as? Boolean ?: false,
                canExecute = fileInfo["canExecute"] as? Boolean ?: false
            )
        }

        fun fromFileInfoList(fileInfoList: List<Map<String, Any>>): List<SSHFileInfoResponse> {
            return fileInfoList.map { fromFileInfo(it) }
        }
    }
    
    // 转换为前端友好的格式
    fun toFrontendFormat(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "path" to path,
            "type" to if (isDirectory) "directory" else "file",
            "size" to size,
            "permissions" to permissions,
            "modifiedTime" to modifiedTime.toString(),
            "owner" to owner,
            "group" to group
        )
    }
}