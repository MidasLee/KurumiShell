package per.midas.kurumishell.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import per.midas.kurumishell.config.AdminProperties
import per.midas.kurumishell.dto.UserDTO
import per.midas.kurumishell.dto.request.LoginRequest
import per.midas.kurumishell.dto.request.RegisterRequest
import per.midas.kurumishell.dto.response.ApiResponse
import per.midas.kurumishell.dto.response.JwtResponse
import per.midas.kurumishell.dto.response.TokenInfoResponse
import per.midas.kurumishell.dto.response.UserInfoResponse
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.exception.NotFoundException
import per.midas.kurumishell.security.JwtUtils
import per.midas.kurumishell.security.UserDetailsImpl
import per.midas.kurumishell.service.AuthService

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtUtils: JwtUtils,
    private val adminProperties: AdminProperties
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<UserDTO>> {
        return try {
            val user = authService.register(request)
            val userResponse = UserDTO.fromEntity(user)
            ResponseEntity.ok(ApiResponse(200, "用户注册成功", userResponse))
        } catch (e: IllegalArgumentException) {
            when (e.message) {
                "用户名不能为空" -> ResponseEntity.status(400).body(ApiResponse(400, "用户名不能为空", null))
                "密码至少8位" -> ResponseEntity.status(400).body(ApiResponse(400, "密码至少8位", null))
                "用户名已存在" -> ResponseEntity.status(409).body(ApiResponse(409, "用户名已存在", null))
                "邮箱已被使用" -> ResponseEntity.status(409).body(ApiResponse(409, "邮箱已被使用", null))
                else -> ResponseEntity.status(400).body(ApiResponse(400, "注册请求无效", null))
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(ApiResponse(500, "服务器内部错误", null))
        }
    }

    @PostMapping("/login")
    fun authenticate(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<JwtResponse>> {
        return try {
            val jwtResponse = authService.authenticate(request)
            ResponseEntity.ok(ApiResponse(200, "Login successful", jwtResponse))
        } catch (e: BadCredentialsException) {
            ResponseEntity.status(401).body(ApiResponse(401, "无效的用户名或密码", null))
        } catch (e: LockedException) {
            ResponseEntity.status(403).body(ApiResponse(403, "用户已被锁定", null))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(ApiResponse(500, "其他服务器错误", null))
        }
    }

    // 新增：刷新令牌端点
    @PostMapping("/refresh-token")
    @PreAuthorize("isAuthenticated()")
    fun refreshToken(): ResponseEntity<ApiResponse<JwtResponse>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val jwtResponse = authService.refreshToken(authentication)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "令牌刷新成功",
                data = jwtResponse
            )
        )
    }

    // 获取当前用户信息：
    // 依赖Spring Security的SecurityContext
    // 需要已认证的会话
    // 更安全，推荐在应用内部使用
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUser(): ResponseEntity<ApiResponse<UserInfoResponse>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userDetails = authentication.principal as UserDetailsImpl

        // 分离角色和权限
        val authorities = userDetails.authorities.map { it.authority }
        val roles = authorities.filter { it.startsWith("ROLE_") }
        val privileges = authorities.filter { it.startsWith("PRIVILEGE_") }

        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取用户信息成功",
                data = UserInfoResponse(
                    id = userDetails.id,
                    username = userDetails.username,
                    email = userDetails.email,
                    roles = roles,
                    privileges = privileges
                )
            )
        )
    }

    // 根据token获取用户信息（不依赖SecurityContext）：
    // 直接从token解析用户信息
    // 不依赖SecurityContext
    // 适合外部服务调用或微服务间通信
    @GetMapping("/userinfo")
    fun getUserInfo(@RequestHeader("Authorization") token: String): ResponseEntity<ApiResponse<UserInfoResponse>> {
        val jwtToken = token.replace("Bearer ", "")
        if (!jwtUtils.validateJwtToken(jwtToken)) {
            throw AccessDeniedException("无效的token")
        }

        val username = jwtUtils.getUserNameFromJwtToken(jwtToken)
        val user = authService.findByUsername(username)

        // 分离角色和权限
        val roles = user.roles.map { it.name }  // 角色列表（如 ["ROLE_ADMIN"]）
        val privileges = user.roles
            .flatMap { it.privileges }         // 获取所有权限
            .map { it.name }                   // 提取权限名称（如 ["PRIVILEGE_READ"]）
            .distinct()                        // 去重（可选）

        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取用户信息成功",
                data = UserInfoResponse(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    roles = roles,
                    privileges = privileges
                )
            )
        )
    }

    @GetMapping("/token-info")
    fun getTokenInfo(@RequestHeader("Authorization") authHeader: String): ResponseEntity<ApiResponse<TokenInfoResponse>> {
        val token = authHeader.replace("Bearer ", "")
        if (!jwtUtils.validateJwtToken(token)) {
            throw AccessDeniedException("无效的token")
        }

        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "Token信息获取成功",
                data = jwtUtils.getTokenInfo(token)
            )
        )
    }

    @PostMapping("/disable-user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun disableUserById(@PathVariable userId: String): ResponseEntity<ApiResponse<*>> {
        return try {
            if (userId == adminProperties.id) {
                ResponseEntity.status(403).body(ApiResponse.error(403, "无法禁用初始管理员"))
            } else {
                val user = authService.disableUserById(userId)
                ResponseEntity.ok(ApiResponse(200, "用户已禁用", UserDTO.fromEntity(user)))
            }
        } catch (e: NotFoundException) {
            ResponseEntity.status(404).body(ApiResponse.error(404, "用户不存在"))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(ApiResponse.error(500, "服务器内部错误"))
        }
    }

    @PostMapping("/enable-user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun enableUserById(@PathVariable userId: String): ResponseEntity<ApiResponse<UserDTO>> {
        return try {
            val user = authService.enableUserById(userId)
            ResponseEntity.ok(ApiResponse(200, "用户已启用", UserDTO.fromEntity(user)))
        } catch (e: NotFoundException) {
            ResponseEntity.status(404).body(ApiResponse(404, "用户不存在", null))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(ApiResponse(500, "服务器内部错误", null))
        }
    }

}