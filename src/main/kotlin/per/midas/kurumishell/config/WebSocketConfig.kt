package per.midas.kurumishell.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import per.midas.kurumishell.websocket.handler.ResourceMonitorWebSocketHandler
import per.midas.kurumishell.websocket.handler.SSHWebSocketHandler
import per.midas.kurumishell.websocket.service.SSHService

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val sshService: SSHService,
    private val corsProperties: CorsProperties,
    private val resourceMonitorWebSocketHandler: ResourceMonitorWebSocketHandler
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(SSHWebSocketHandler(sshService), "/api/ssh/terminal")
            .setAllowedOrigins(corsProperties.allowedOrigins.joinToString(","))
            
        // 添加资源监控WebSocket端点
        registry.addHandler(resourceMonitorWebSocketHandler, "/api/ssh/resources")
            .setAllowedOrigins(corsProperties.allowedOrigins.joinToString(","))
    }
}