package per.midas.kurumishell.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.cors")
data class CorsProperties(
    var allowedOrigins: List<String> = emptyList()  // 默认空列表
)