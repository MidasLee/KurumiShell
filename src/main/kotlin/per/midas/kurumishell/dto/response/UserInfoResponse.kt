package per.midas.kurumishell.dto.response

data class UserInfoResponse(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String>,
    val privileges: List<String>
)