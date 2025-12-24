package per.midas.kurumishell.dto.request

data class UserUpdateRequest(
    val username: String?,
    val email: String?,
    val password: String?
)