package per.midas.kurumishell.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import per.midas.kurumishell.entity.User
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, String> {

    // 通过用户名查找用户（用于登录）
    @EntityGraph(attributePaths = ["roles"])  // 预加载 roles
    fun findByUsername(username: String): Optional<User>

    // 通过邮箱查找用户（用于注册校验）
    fun findByEmail(email: String): Optional<User>

    // 检查用户名是否存在（性能优化版）
    fun existsByUsername(username: String): Boolean

    // 检查邮箱是否存在（性能优化版）
    fun existsByEmail(email: String): Boolean

    fun existsByUsernameAndIdNot(username: String, id: String): Boolean

    fun existsByEmailAndIdNot(email: String, id: String): Boolean

    // 复杂查询示例：查找拥有指定角色的用户
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    fun findByRoleName(roleName: String): List<User>

    // 使用EntityGraph解决N+1查询问题
    @EntityGraph(attributePaths = ["roles.privileges"])
    override fun findAll(): List<User>

    // 添加分页查询支持
    @EntityGraph(attributePaths = ["roles.privileges"])
    override fun findAll(pageable: Pageable): Page<User>

    // 添加对 enabled 状态的查询
    fun findByUsernameAndEnabledTrue(username: String): Optional<User>
    fun findByEmailAndEnabledTrue(email: String): Optional<User>

    // 添加按角色查询的分页版本
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    fun findByRoleName(roleName: String, pageable: Pageable): Page<User>

    // 添加按多个条件查询的示例
    @Query("SELECT u FROM User u WHERE " +
            "(:username IS NULL OR u.username LIKE %:username%) AND " +
            "(:email IS NULL OR u.email LIKE %:email%) AND " +
            "(:enabled IS NULL OR u.enabled = :enabled)")
    fun searchUsers(
        @Param("username") username: String?,
        @Param("email") email: String?,
        @Param("enabled") enabled: Boolean?,
        pageable: Pageable
    ): Page<User>
}