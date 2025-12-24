package per.midas.kurumishell.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import per.midas.kurumishell.dto.response.ApiResponse

@Component
class AuthTokenFilter(
    private val jwtUtils: JwtUtils,
    private val userDetailsService: UserDetailsService,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = parseJwt(request)
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                val username = jwtUtils.getUserNameFromJwtToken(jwt)
                val userDetails = userDetailsService.loadUserByUsername(username)
                // 调试日志
                logger.info("Authenticated user: ${userDetails.username}, authorities: ${userDetails.authorities}")

                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: ExpiredJwtException) {
            // 使用 ApiResponse 返回统一格式
            sendJsonResponse(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                ApiResponse.error(401, "登录状态已过期，请重新登录")
            )
            return
        } catch (e: JwtException) {
            sendJsonResponse(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                ApiResponse.error(401, "登录状态无效: ${e.message ?: "未知错误"}")
            )
            return
        } catch (e: Exception) {
            throw e // 其他异常交给全局处理器
        }
        filterChain.doFilter(request, response)
    }

    /**
     * 发送 JSON 格式响应
     */
    private fun sendJsonResponse(
        response: HttpServletResponse,
        statusCode: Int,
        apiResponse: ApiResponse<*>
    ) {
        response.status = statusCode
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(objectMapper.writeValueAsString(apiResponse))
    }

    private fun parseJwt(request: HttpServletRequest): String? {
        val headerAuth = request.getHeader("Authorization")
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7)
        }
        return null
    }
}