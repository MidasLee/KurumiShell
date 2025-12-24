package per.midas.kurumishell.dto.request

data class SSHFileOperationRequest(
    val path: String? = null,
    val content: String? = null,
    val sourcePath: String? = null,
    val targetPath: String? = null,
    val remotePath: String? = null
)