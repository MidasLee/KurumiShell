package per.midas.kurumishell.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import per.midas.kurumishell.repository.UserRepository

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("用户不存在") }

        val authorities = mutableSetOf<SimpleGrantedAuthority>().apply {
            // 添加权限
            user.roles.flatMapTo(this) { role ->
                role.privileges.map { SimpleGrantedAuthority(it.name) }
            }
            // 添加角色（格式必须为 ROLE_XXX）
            user.roles.forEach { add(SimpleGrantedAuthority(it.name)) }
        }

        return UserDetailsImpl(
            user.id,
            user.username,
            user.email,
            user.password,
            authorities
        )
    }
}