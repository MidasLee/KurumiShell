package per.midas.kurumishell.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import per.midas.kurumishell.dto.request.UserUpdateRequest
import per.midas.kurumishell.entity.Role
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.exception.NotFoundException
import per.midas.kurumishell.repository.RoleRepository
import per.midas.kurumishell.repository.UserRepository

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val roleRepository: RoleRepository
) {

    // 创建用户（与AuthService的register区分，这里用于管理员创建用户）
    fun createUser(user: User): User {
        require(!userRepository.existsByUsername(user.username)) { "用户名已存在" }
        require(!userRepository.existsByEmail(user.email)) { "邮箱已被使用" }

        return userRepository.save(
            user.copy(
                password = passwordEncoder.encode(user.password)
            )
        )
    }

    // 更新用户信息
    fun updateUser(userId: String, request: UserUpdateRequest): User {
        val user = findById(userId)
        request.username?.takeIf { it.isNotBlank() }?.let {
            require(!userRepository.existsByUsernameAndIdNot(it, userId)) { "用户名已被使用" }
            user.username = it
        }
        request.email?.takeIf { it.isNotBlank() }?.let {
            require(!userRepository.existsByEmailAndIdNot(it, userId)) { "邮箱已被使用" }
            user.email = it
        }
        request.password?.takeIf { it.isNotBlank() }?.let {
            user.password = passwordEncoder.encode(it)
        }
        return userRepository.save(user)
    }

    // 根据ID获取用户
    @Transactional(readOnly = true)
    fun findById(id: String): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("用户不存在") }
    }

    // 根据用户名获取用户
    @Transactional(readOnly = true)
    fun findByUsername(username: String): User {
        val userOptional = userRepository.findByUsername(username)
        if (userOptional.isEmpty) {
            throw NotFoundException("用户不存在")
        } else {
            return userOptional.get()
        }
    }

    // 获取所有用户
    @Transactional(readOnly = true)
    fun findAll(): List<User> {
        return userRepository.findAll()
    }

    // 获取所有用户（分页）
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<User> {
        println(userRepository.findAll(pageable).size)
        return userRepository.findAll(pageable)
    }

    // 删除用户
    fun deleteUser(id: String) {
        require(userRepository.existsById(id)) { "用户不存在" }
        userRepository.deleteById(id)
    }

    // 检查用户是否存在
    @Transactional(readOnly = true)
    fun existsById(id: String): Boolean {
        return userRepository.existsById(id)
    }

    // 启用/禁用用户
    fun toggleUserStatus(id: String, enabled: Boolean): User {
        val user = findById(id)
        user.enabled = enabled
        return userRepository.save(user)
    }


    // 修改后的角色分配方法
    fun assignRolesToUser(userId: String, roleIds: Set<Long>): User {
        val user = findById(userId)
        val roles = roleRepository.findAllById(roleIds)
        require(roles.size == roleIds.size) { "部分角色不存在" }
        user.roles = roles.toSet() as MutableSet<Role>
        return userRepository.save(user)
    }

    // 修改后的增量式添加角色
    fun addRolesToUser(userId: String, roleIds: Set<Long>): User {
        val user = findById(userId)
        val existingRoles = user.roles.map { it.id }.toSet()
        val newRoleIds = roleIds - existingRoles

        if (newRoleIds.isNotEmpty()) {
            val roles = roleRepository.findAllById(newRoleIds)
            require(roles.size == newRoleIds.size) { "部分角色不存在" }
            roles.forEach { user.addRole(it) }
            return userRepository.save(user)
        }
        return user
    }

    // 修改后的移除用户角色
    fun removeRolesFromUser(userId: String, roleIds: Set<Long>): User {
        val user = findById(userId)
        roleIds.mapNotNull { roleId ->
            user.roles.find { it.id == roleId }
        }.forEach { user.removeRole(it) }
        return userRepository.save(user)
    }

    // 获取用户角色
    @Transactional(readOnly = true)
    fun getUserRoles(userId: String): Set<Role> {
        return findById(userId).roles
    }
}