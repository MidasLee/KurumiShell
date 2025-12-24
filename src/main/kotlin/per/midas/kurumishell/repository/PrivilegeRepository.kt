package per.midas.kurumishell.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import per.midas.kurumishell.entity.Privilege
import java.util.Optional

@Repository
interface PrivilegeRepository : JpaRepository<Privilege, Long> {

    // 按权限名查找（用于权限校验）
    fun findByName(name: String): Optional<Privilege>

    // 检查权限是否存在
    fun existsByName(name: String): Boolean

    // 模糊搜索
    @Query("SELECT p FROM Privilege p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    fun search(keyword: String): List<Privilege>

    // 添加分页支持
    @Query("SELECT p FROM Privilege p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    fun search(keyword: String, pageable: Pageable): Page<Privilege>

    // 添加批量查询
    fun findByNameIn(names: Collection<String>): List<Privilege>

    fun existsByNameAndIdNot(name: String, id: Long): Boolean

}