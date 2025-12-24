package per.midas.kurumishell.service

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import per.midas.kurumishell.dto.request.LoginRequest
import per.midas.kurumishell.dto.request.RegisterRequest
import per.midas.kurumishell.dto.response.JwtResponse
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.exception.NotFoundException
import per.midas.kurumishell.repository.RoleRepository
import per.midas.kurumishell.repository.UserRepository
import per.midas.kurumishell.security.JwtUtils
import per.midas.kurumishell.security.UserDetailsImpl

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils
) {
    fun register(request: RegisterRequest): User {
        require(request.username.isNotBlank()) { "用户名不能为空" }
        require(request.password.length >= 8) { "密码至少8位" }

        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("用户名已存在")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("邮箱已被使用")
        }
        val userRole = roleRepository.findByName("ROLE_USER").orElseThrow()
        return userRepository.save(
            User(
                id = request.id,
                username = request.username,
                email = request.email,
                password = passwordEncoder.encode(request.password),
                roles = mutableSetOf(userRole)
            )
        )
    }

    fun authenticate(request: LoginRequest): JwtResponse {
        // 先检查用户是否存在和是否被禁用
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { BadCredentialsException("无效的用户名或密码") }

        if (!user.enabled) {
            throw LockedException("用户已被禁用")
        }

        // 进行认证
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        SecurityContextHolder.getContext().authentication = authentication
        val userDetails = authentication.principal as UserDetailsImpl

        return JwtResponse(
            token = jwtUtils.generateJwtToken(authentication),
            id = userDetails.id,
            username = userDetails.username,
            email = userDetails.email,
            roles = userDetails.authorities.map { it.authority }
        )
    }

    fun refreshToken(authentication: Authentication): JwtResponse {
        val userDetails = authentication.principal as UserDetailsImpl

        // 获取最新用户数据，确保不是从缓存中读取的旧数据
        val currentUser = userRepository.findById(userDetails.id)
            .orElseThrow {
                // 用户不存在，可能是被删除了
                BadCredentialsException("用户不存在或已被删除")
            }

        // 检查用户是否被禁用
        if (!currentUser.enabled) {
            throw LockedException("用户已被禁用，无法刷新令牌")
        }

        // 可以添加其他检查，例如：
        // 1. 检查账户是否过期
        // 2. 检查凭据是否过期
        // 3. 检查账户是否被锁定
        // 4. 其他业务相关的状态检查

        // 示例：检查账户是否过期（如果实体中有此字段）
        // if (currentUser.accountExpired) {
        //     throw AccountExpiredException("用户账户已过期")
        // }

        // 示例：检查凭据是否过期（如果实体中有此字段）
        // if (currentUser.credentialsExpired) {
        //     throw CredentialsExpiredException("用户凭据已过期")
        // }

        // 所有检查通过后，生成新令牌
        return JwtResponse(
            token = jwtUtils.generateJwtToken(authentication),
            id = userDetails.id,
            username = userDetails.username,
            email = userDetails.email,
            roles = userDetails.authorities.map { it.authority }
        )
    }

    fun findByUsername(username: String): User {
        return userRepository.findByUsername(username)
            .orElseThrow { NotFoundException("用户不存在") }
    }

    fun findById(id: String): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("用户不存在") }
    }

    fun disableUserById(userId: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("用户不存在") }
        user.enabled = false
        return userRepository.save(user)
    }

    fun enableUserById(userId: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("用户不存在") }
        user.enabled = true
        return userRepository.save(user)
    }
}