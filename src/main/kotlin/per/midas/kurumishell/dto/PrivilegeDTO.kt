package per.midas.kurumishell.dto

import per.midas.kurumishell.entity.Privilege

data class PrivilegeDTO(
    val id: Long,
    val name: String,
    val description: String?
){
    companion object {
        fun fromEntity(privilege: Privilege): PrivilegeDTO {
            return PrivilegeDTO(
                id = privilege.id,
                name = privilege.name,
                description = privilege.description
            )
        }
    }
}