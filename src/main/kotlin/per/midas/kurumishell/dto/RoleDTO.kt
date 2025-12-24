package per.midas.kurumishell.dto

import per.midas.kurumishell.entity.Role

data class RoleDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val privileges: Set<PrivilegeDTO>
) {
    companion object {
        fun fromEntity(role: Role): RoleDTO {
            return RoleDTO(
                id = role.id,
                name = role.name,
                description = role.description,
                privileges = role.privileges.map { PrivilegeDTO.fromEntity(it) }.toSet()
            )
        }
    }
}