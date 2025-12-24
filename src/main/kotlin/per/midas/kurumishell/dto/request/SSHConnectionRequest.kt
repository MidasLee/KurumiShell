package per.midas.kurumishell.dto.request

data class SSHConnectionRequest(
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val password: String? = null,
    val privateKey: String? = null,
    val keyPassphrase: String? = null,
    val groupId: String? = null  // 接收 groupId 字段
)