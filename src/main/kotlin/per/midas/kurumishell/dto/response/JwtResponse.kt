package per.midas.kurumishell.dto.response

data class JwtResponse(
    val token: String,
    val type: String = "Bearer",
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String>
)