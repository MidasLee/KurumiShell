package per.midas.kurumishell.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import per.midas.kurumishell.entity.SSHGroup
import per.midas.kurumishell.entity.User

interface SSHGroupRepository : JpaRepository<SSHGroup, String> {

    // 根据用户查询所有分组
    fun findByUser(user: User): List<SSHGroup>

    // 根据ID和用户查询分组（带连接信息）
    @Query("SELECT g FROM SSHGroup g LEFT JOIN FETCH g.connections WHERE g.id = :id AND g.user = :user")
    fun findByIdAndUserWithConnections(id: String, user: User): SSHGroup?

    // 检查用户下是否存在同名分组
    fun existsByNameAndUser(name: String, user: User): Boolean

    fun existsByIdAndUser(id: String, user: User): Boolean

    // 根据用户和分组ID查询
    fun findByIdAndUser(id: String, user: User): SSHGroup?

    fun countByUser(user: User): Long

}