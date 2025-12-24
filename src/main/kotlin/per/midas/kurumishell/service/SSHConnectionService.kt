package per.midas.kurumishell.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import per.midas.kurumishell.entity.SSHConnection
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.dto.request.SSHConnectionRequest
import per.midas.kurumishell.repository.SSHConnectionRepository
import per.midas.kurumishell.repository.SSHGroupRepository
import java.util.UUID

@Service
class SSHConnectionService(
    private val connectionRepository: SSHConnectionRepository,
    private val groupRepository: SSHGroupRepository
) {
    // 获取用户的所有连接
    @Transactional(readOnly = true)
    fun findAllByUser(user: User): List<SSHConnection> =
        connectionRepository.findByUser(user)

    // 获取用户特定分组下的连接
    @Transactional(readOnly = true)
    fun findByUserAndGroupId(user: User, groupId: String): List<SSHConnection> =
        connectionRepository.findByUserAndGroupId(user, groupId)

    // 获取用户的特定连接
    @Transactional(readOnly = true)
    fun findByIdAndUser(id: String, user: User): SSHConnection? =
        connectionRepository.findByIdAndUser(id, user)

    // 创建连接
    @Transactional
    fun createConnection(request: SSHConnectionRequest, user: User): SSHConnection {
        println("=== 创建连接调试 ===")
        println("接收到的 groupId: ${request.groupId}")

        // 处理分组
        val group = request.groupId?.let { groupId ->
            println("正在查找分组: $groupId")
            val foundGroup = groupRepository.findByIdAndUser(groupId, user)
            if (foundGroup == null) {
                throw IllegalArgumentException("分组不存在或无权访问")
            }
            println("找到分组: ${foundGroup.id} - ${foundGroup.name}")
            foundGroup
        }

        println("最终使用的分组: ${group?.id}")

        // 创建连接对象
        val newConnection = SSHConnection(
            name = request.name,
            host = request.host,
            port = request.port,
            username = request.username,
            password = request.password,
            privateKey = request.privateKey,
            keyPassphrase = request.keyPassphrase,
            group = group,
            user = user
        )

        // 验证名称唯一性
        validateConnectionUnique(request.name, user, group?.id)

        println("保存前确认 - 分组ID: ${newConnection.group?.id}")
        val savedConnection = connectionRepository.save(newConnection)
        println("保存后分组ID: ${savedConnection.group?.id}")
        
        return savedConnection
    }

    // 更新连接
    @Transactional
    fun updateConnection(id: String, updateData: SSHConnection, user: User): SSHConnection {
        val existing = connectionRepository.findByIdAndUser(id, user)
            ?: throw IllegalArgumentException("Connection not found or not accessible")

        // 验证分组归属（如果有变更）
        updateData.group?.id?.takeIf { it != existing.group?.id }?.let { groupId ->
            if (!groupRepository.existsByIdAndUser(groupId, user)) {
                throw IllegalArgumentException("Group not found or not accessible")
            }
        }

        // 验证名称唯一性（如果名称或分组有变更）
        if (updateData.name != existing.name || updateData.group?.id != existing.group?.id) {
            validateConnectionUnique(updateData.name, user, updateData.group?.id)
        }

        return existing.copy(
            name = updateData.name,
            host = updateData.host,
            port = updateData.port,
            username = updateData.username,
            password = updateData.password,
            privateKey = updateData.privateKey,
            keyPassphrase = updateData.keyPassphrase,
            group = updateData.group
        ).let { connectionRepository.save(it) }
    }

    // 删除连接
    @Transactional
    fun deleteConnection(id: String, user: User) {
        val connection = connectionRepository.findByIdAndUser(id, user)
            ?: throw IllegalArgumentException("Connection not found or not accessible")
        connectionRepository.delete(connection)
    }

    // 批量删除用户的所有连接
    @Transactional
    fun deleteAllByUser(user: User) {
        connectionRepository.deleteByUser(user)
    }

    // 验证连接名称唯一性
    private fun validateConnectionUnique(name: String, user: User, groupId: String?) {
        if (connectionRepository.existsByNameAndUserAndGroupId(name, user, groupId)) {
            throw IllegalArgumentException("Connection name must be unique within group for this user")
        }
    }

    // 获取用户的连接数量
    @Transactional(readOnly = true)
    fun countByUser(user: User): Long = connectionRepository.countByUser(user)

    // 移动连接到其他分组
    @Transactional
    fun moveConnectionToGroup(connectionId: String, newGroupId: String?, user: User): SSHConnection {
        val connection = connectionRepository.findByIdAndUser(connectionId, user)
            ?: throw IllegalArgumentException("Connection not found or not accessible")

        val newGroup = newGroupId?.let {
            groupRepository.findByIdAndUser(it, user)
                ?: throw IllegalArgumentException("Target group not found or not accessible")
        }

        connection.group = newGroup
        return connectionRepository.save(connection)
    }
}