package per.midas.kurumishell.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {

    // ========== 拦截器配置 ==========
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(logInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/auth/**")
    }

    @Bean
    fun logInterceptor() = LogInterceptor()

    // ========== 消息转换器 ==========
    @Bean
    fun jsonMessageConverter(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.serializationInclusion(JsonInclude.Include.NON_NULL)
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            builder.dateFormat(StdDateFormat()) // ISO8601日期格式
        }
    }
}