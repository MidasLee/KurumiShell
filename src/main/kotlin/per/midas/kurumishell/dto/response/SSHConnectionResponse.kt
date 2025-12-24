package per.midas.kurumishell.dto.response

import per.midas.kurumishell.entity.SSHConnection

data class SSHConnectionResponse(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val privateKey: String?
) {
    companion object{
        fun fromEntity(sshConnection:SSHConnection): SSHConnectionResponse {
            return SSHConnectionResponse(
                id = sshConnection.id,
                name = sshConnection.name,
                host = sshConnection.host,
                port = sshConnection.port,
                username = sshConnection.username,
                privateKey = sshConnection.privateKey
            )
        }
        fun fromEntityMutableList(sshConnectionMutableList: MutableList<SSHConnection>): List<SSHConnectionResponse> {
            return sshConnectionMutableList.map { fromEntity(it) }
        }
    }

}