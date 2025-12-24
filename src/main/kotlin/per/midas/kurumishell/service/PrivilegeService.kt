package per.midas.kurumishell.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import per.midas.kurumishell.dto.request.PrivilegeCreateRequest
import per.midas.kurumishell.dto.request.PrivilegeUpdateRequest
import per.midas.kurumishell.entity.Privilege
import per.midas.kurumishell.entity.Role
import per.midas.kurumishell.exception.NotFoundException
import per.midas.kurumishell.repository.PrivilegeRepository
import per.midas.kurumishell.repository.RoleRepository

@Service
@Transactional
class PrivilegeService(
    private val privilegeRepository: PrivilegeRepository,
    private val roleRepository: RoleRepository
) {

    // 创建权限
    fun createPrivilege(request: PrivilegeCreateRequest): Privilege {
        require(!privilegeRepository.existsByName(request.name)) { "权限名称已存在" }

        val privilege = Privilege(
            name = request.name,
            description = request.description
        )
        return privilegeRepository.save(privilege)
    }

    // 更新权限
    fun updatePrivilege(privilegeId: Long, request: PrivilegeUpdateRequest): Privilege {
        val privilege = findById(privilegeId)
        request.name?.takeIf { it.isNotBlank() }?.let {
            require(!privilegeRepository.existsByNameAndIdNot(it, privilegeId)) { "权限名称已存在" }
            privilege.name = it
        }
        request.description?.let { privilege.description = it }
        return privilegeRepository.save(privilege)
    }

    // 根据ID获取权限
    @Transactional(readOnly = true)
    fun findById(id: Long): Privilege {
        return privilegeRepository.findById(id)
            .orElseThrow { NotFoundException("权限不存在") }
    }

    // 根据名称获取权限
    @Transactional(readOnly = true)
    fun findByName(name: String): Privilege {
        return privilegeRepository.findByName(name)
            .orElseThrow { NotFoundException("权限不存在") }
    }

    // 获取所有权限
    @Transactional(readOnly = true)
    fun findAll(): List<Privilege> {
        return privilegeRepository.findAll()
    }

    // 获取所有权限（分页）
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<Privilege> {
        return privilegeRepository.findAll(pageable)
    }

    // 搜索权限
    @Transactional(readOnly = true)
    fun search(keyword: String, pageable: Pageable): Page<Privilege> {
        return privilegeRepository.search(keyword, pageable)
    }

    // 删除权限
    fun deletePrivilege(id: Long) {
        val privilege = findById(id)
        // 删除前解除所有角色关联
        privilege.roles.forEach { it.removePrivilege(privilege) }
        privilegeRepository.delete(privilege)
    }

    // 检查权限是否存在
    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return privilegeRepository.existsById(id)
    }

    // 批量获取权限
    @Transactional(readOnly = true)
    fun findAllByIds(ids: Set<Long>): Set<Privilege> {
        return privilegeRepository.findAllById(ids).toSet()
    }

    // 获取拥有该权限的角色
    @Transactional(readOnly = true)
    fun findRolesByPrivilegeId(privilegeId: Long, pageable: Pageable): Page<Role> {
        return roleRepository.findByPrivilegeName(findById(privilegeId).name, pageable)
    }
}