package per.midas.kurumishell.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.HandlerInterceptor

class LogInterceptor : HandlerInterceptor {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        log.info("Request: ${request.method} ${request.requestURI}")
        return true
    }
}