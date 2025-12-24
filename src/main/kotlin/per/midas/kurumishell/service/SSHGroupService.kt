package per.midas.kurumishell.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import per.midas.kurumishell.entity.SSHGroup
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.repository.SSHConnectionRepository
import per.midas.kurumishell.repository.SSHGroupRepository
import per.midas.kurumishell.repository.UserRepository

@Service
class SSHGroupService(
    private val groupRepository: SSHGroupRepository,
    private val connectionRepository: SSHConnectionRepository,
    private val userRepository: UserRepository
) {
    // 获取用户的所有分组
    @Transactional(readOnly = true)
    fun findAllByUser(user: User): List<SSHGroup> =
        groupRepository.findByUser(user)

    // 获取用户的特定分组（带连接）
    @Transactional(readOnly = true)
    fun findByIdAndUserWithConnections(id: String, user: User): SSHGroup? =
        groupRepository.findByIdAndUserWithConnections(id, user)

    // 创建分组（用户隔离）
    @Transactional
    fun createGroup(group: SSHGroup, user: User): SSHGroup {
        if (groupRepository.existsByNameAndUser(group.name, user)) {
            throw IllegalArgumentException("'${group.name}'分组已经存在，请勿重复创建")
        }

        group.user = user
        return groupRepository.save(group)
    }

    // 更新分组
    @Transactional
    fun updateGroup(id: String, updateData: SSHGroup, user: User): SSHGroup {
        val existing = groupRepository.findByIdAndUser(id, user)
            ?: throw IllegalArgumentException("Group not found or not accessible")

        // 名称变更时验证唯一性
        if (updateData.name != existing.name &&
            groupRepository.existsByNameAndUser(updateData.name, user)) {
            throw IllegalArgumentException("New group name '${updateData.name}'分组已经存在，请勿重复创建")
        }

        return existing.copy(
            name = updateData.name,
            description = updateData.description
        ).let { groupRepository.save(it) }
    }

    // 删除分组
    @Transactional
    fun deleteGroup(id: String, user: User) {
        // 验证分组归属并获取分组对象（确保关联的连接被正确加载和级联删除）
        val group = groupRepository.findByIdAndUser(id, user)
            ?: throw IllegalArgumentException("Group not found or not accessible")
        
        // 直接删除分组，通过级联删除（CascadeType.REMOVE）自动删除所有关联的连接
        groupRepository.delete(group)
    }

    // 检查分组是否存在且属于用户
    @Transactional(readOnly = true)
    fun existsByIdAndUser(id: String, user: User): Boolean =
        groupRepository.findByIdAndUser(id, user) != null

    // 新增：获取用户的分组数量
    @Transactional(readOnly = true)
    fun countByUser(user: User): Long = groupRepository.countByUser(user)
}