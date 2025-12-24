package per.midas.kurumishell.dto

import per.midas.kurumishell.entity.User

data class UserDTO(
    val id: String,
    val username: String,
    val email: String,
    val enabled: Boolean,
    val roles: Set<RoleDTO>
) {
    companion object {
        fun fromEntity(user: User): UserDTO {
            return UserDTO(
                id = user.id,
                username = user.username,
                email = user.email,
                enabled = user.enabled,
                roles = user.roles.map { RoleDTO.fromEntity(it) }.toSet()
            )
        }
    }
}