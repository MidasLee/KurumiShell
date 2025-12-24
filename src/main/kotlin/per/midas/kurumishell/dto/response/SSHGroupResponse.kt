package per.midas.kurumishell.dto.response

import per.midas.kurumishell.entity.SSHGroup
import java.time.Instant

data class SSHGroupResponse(
    val id: String,
    val name: String,
    val description: String?,
    val connections: List<SSHConnectionResponse>,
    val createdAt: Instant,
    val updatedAt: Instant?,
) {
    companion object {
        fun fromEntity(sshGroup: SSHGroup): SSHGroupResponse {
            return SSHGroupResponse(
                id = sshGroup.id,
                name = sshGroup.name,
                description = sshGroup.description,
                connections = SSHConnectionResponse.fromEntityMutableList(sshGroup.connections),
                createdAt = sshGroup.createdAt,
                updatedAt = sshGroup.updatedAt,
            )
        }

        fun fromEntityList(sshGroupList: List<SSHGroup>): List<SSHGroupResponse> {
            return sshGroupList.map { fromEntity(it) }
        }
    }
}