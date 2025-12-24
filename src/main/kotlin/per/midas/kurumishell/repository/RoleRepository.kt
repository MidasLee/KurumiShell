package per.midas.kurumishell.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import per.midas.kurumishell.entity.Role
import java.util.Optional

@Repository
interface RoleRepository : JpaRepository<Role, Long> {

    // 通过角色名查找角色
    fun findByName(name: String): Optional<Role>

    // 检查角色名是否存在
    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(name: String, id: Long): Boolean

    // 复杂查询示例：查找拥有特定权限的角色
    @Query("SELECT r FROM Role r JOIN r.privileges p WHERE p.name = :privilegeName")
    fun findByPrivilegeName(privilegeName: String): List<Role>

    // 使用EntityGraph预加载权限数据
    @EntityGraph(attributePaths = ["privileges"])
    override fun findById(id: Long): Optional<Role>

    // 添加分页查询
    @EntityGraph(attributePaths = ["privileges"])
    override fun findAll(pageable: Pageable): Page<Role>

    // 添加按名称模糊查询
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Role>

    // 改进复杂查询，添加分页支持
    @Query("SELECT r FROM Role r JOIN r.privileges p WHERE p.name = :privilegeName")
    fun findByPrivilegeName(privilegeName: String, pageable: Pageable): Page<Role>
}