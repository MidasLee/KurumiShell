package per.midas.kurumishell.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import per.midas.kurumishell.dto.request.RoleCreateRequest
import per.midas.kurumishell.dto.request.RoleUpdateRequest
import per.midas.kurumishell.entity.Privilege
import per.midas.kurumishell.entity.Role
import per.midas.kurumishell.exception.NotFoundException
import per.midas.kurumishell.repository.PrivilegeRepository
import per.midas.kurumishell.repository.RoleRepository
import java.util.*

@Service
@Transactional
class RoleService(
    private val roleRepository: RoleRepository,
    private val privilegeRepository: PrivilegeRepository
) {

    // 创建角色
    fun createRole(request: RoleCreateRequest): Role {
        require(!roleRepository.existsByName(request.name)) { "角色名称已存在" }

        val role = Role(
            name = request.name,
            description = request.description
        )
        return roleRepository.save(role)
    }

    // 更新角色基本信息
    fun updateRole(roleId: Long, request: RoleUpdateRequest): Role {
        val role = findById(roleId)
        request.name?.takeIf { it.isNotBlank() }?.let {
            require(!roleRepository.existsByNameAndIdNot(it, roleId)) { "角色名称已存在" }
            role.name = it
        }
        request.description?.let { role.description = it }
        return roleRepository.save(role)
    }

    // 为角色分配权限
    fun assignPrivileges(roleId: Long, privilegeIds: Set<Long>): Role {
        val role = findById(roleId)
        val privileges = privilegeRepository.findAllById(privilegeIds)
        role.privileges = privileges.toSet() as MutableSet<Privilege>
        return roleRepository.save(role)
    }

    // 根据ID获取角色（包含权限）
    @Transactional(readOnly = true)
    fun findById(id: Long): Role {
        return roleRepository.findById(id)
            .orElseThrow { NotFoundException("角色不存在") }
    }

    // 根据名称获取角色
    @Transactional(readOnly = true)
    fun findByName(name: String): Role {
        val roleOptional = roleRepository.findByName(name)
        if (roleOptional.isEmpty) {
            throw NotFoundException("角色不存在")
        } else {
            return roleOptional.get()
        }
    }

    // 获取所有角色
    @Transactional(readOnly = true)
    fun findAll(): List<Role> {
        return roleRepository.findAll()
    }

    // 获取所有角色（分页）
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<Role> {
        return roleRepository.findAll(pageable)
    }

    // 删除角色
    fun deleteRole(id: Long) {
        require(roleRepository.existsById(id)) { "角色不存在" }
        roleRepository.deleteById(id)
    }

    // 检查角色是否存在
    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return roleRepository.existsById(id)
    }

    // 添加批量权限分配（增量式）
    fun addPrivilegesToRole(roleId: Long, privilegeIds: Set<Long>): Role {
        val role = findById(roleId)
        val privileges = privilegeRepository.findAllById(privilegeIds)
        privileges.forEach { role.addPrivilege(it) } // 使用实体类中的辅助方法
        return roleRepository.save(role)
    }

    // 移除角色权限
    fun removePrivilegesFromRole(roleId: Long, privilegeIds: Set<Long>): Role {
        val role = findById(roleId)
        val privileges = privilegeRepository.findAllById(privilegeIds)
        privileges.forEach { role.removePrivilege(it) } // 使用实体类中的辅助方法
        return roleRepository.save(role)
    }

    // 根据名称搜索角色
    @Transactional(readOnly = true)
    fun searchByName(name: String, pageable: Pageable): Page<Role> {
        return roleRepository.findByNameContainingIgnoreCase(name, pageable)
    }
}