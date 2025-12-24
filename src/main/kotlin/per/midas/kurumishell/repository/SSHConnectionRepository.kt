package per.midas.kurumishell.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import per.midas.kurumishell.entity.SSHConnection
import per.midas.kurumishell.entity.SSHGroup
import per.midas.kurumishell.entity.User

interface SSHConnectionRepository : JpaRepository<SSHConnection, String> {

    // 根据用户查询所有连接
    fun findByUser(user: User): List<SSHConnection>

    // 根据用户和分组查询连接
    fun findByUserAndGroupId(user: User, groupId: String): List<SSHConnection>

    // 根据连接ID和用户查询
    fun findByIdAndUser(id: String, user: User): SSHConnection?

    // 检查用户的分组内是否存在同名连接
    fun existsByNameAndUserAndGroupId(name: String, user: User, groupId: String?): Boolean

    // 删除用户的所有连接
    @Modifying
    @Transactional
    fun deleteByUser(user: User)

    fun countByUser(user: User): Long

    fun findByUserAndGroup(user: User, group: SSHGroup?): List<SSHConnection>
}