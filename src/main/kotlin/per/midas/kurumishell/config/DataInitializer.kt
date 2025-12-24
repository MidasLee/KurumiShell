package per.midas.kurumishell.config

import jakarta.transaction.Transactional
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import per.midas.kurumishell.entity.Privilege
import per.midas.kurumishell.entity.Role
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.repository.PrivilegeRepository
import per.midas.kurumishell.repository.RoleRepository
import per.midas.kurumishell.repository.UserRepository

@Component
@Transactional
class DataInitializer(
    private val privilegeRepository: PrivilegeRepository,
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val adminProperties: AdminProperties
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        initializePrivileges()
        initializeRoles()
        initializeAdminUser()
    }

    private fun initializePrivileges() {
        val privileges = listOf(
            Privilege(name = "PRIVILEGE_READ", description = "读取权限"),
            Privilege(name = "PRIVILEGE_WRITE", description = "写入权限"),
            Privilege(name = "PRIVILEGE_DELETE", description = "删除权限"),
            Privilege(name = "PRIVILEGE_USER_MANAGEMENT", description = "用户管理权限"),
            Privilege(name = "PRIVILEGE_ROLE_MANAGEMENT", description = "角色管理权限")
        )

        privileges.forEach { privilege ->
            if (!privilegeRepository.existsByName(privilege.name)) {
                privilegeRepository.save(privilege)
            }
        }
    }

    private fun initializeRoles() {
        val adminPrivileges = privilegeRepository.findAll().toSet()

        val roles = listOf(
            Role(
                name = "ROLE_ADMIN",
                description = "管理员角色",
                privileges = adminPrivileges.toMutableSet() // 确保使用MutableSet
            ),
            Role(
                name = "ROLE_USER",
                description = "普通用户角色",
                privileges = mutableSetOf(
                    privilegeRepository.findByName("PRIVILEGE_READ").orElseThrow(),
                    privilegeRepository.findByName("PRIVILEGE_WRITE").orElseThrow()
                )
            )
        )

        roles.forEach { role ->
            if (!roleRepository.existsByName(role.name)) {
                roleRepository.save(role)
            }
        }
    }

    private fun initializeAdminUser() {
        val adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow()
        val userRole = roleRepository.findByName("ROLE_USER").orElseThrow()

        val adminUser = User(
            id = adminProperties.id,
            username = adminProperties.username,
            email = adminProperties.email,
            password = passwordEncoder.encode(adminProperties.password),
            roles = mutableSetOf(adminRole, userRole),
            enabled = true
        )

        if (!userRepository.existsByUsername(adminUser.username)) {
            userRepository.save(adminUser)
        }
    }
}