package per.midas.kurumishell.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class DatabaseConfig : ApplicationListener<ApplicationReadyEvent> {
    
    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)
    
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val env = event.applicationContext.environment
        val activeProfiles = env.activeProfiles
        
        val databaseType = when {
            activeProfiles.contains("sqlite") -> "SQLite"
            activeProfiles.contains("mysql") -> "MySQL"
            else -> {
                val driverClass = env.getProperty("spring.datasource.driver-class-name", "")
                when {
                    driverClass.contains("sqlite") -> "SQLite"
                    driverClass.contains("mysql") -> "MySQL"
                    else -> "Unknown"
                }
            }
        }
        
        logger.info("=====================================")
        logger.info("KurumiShell 已启动")
        logger.info("当前使用数据库类型: $databaseType")
        logger.info("当前激活的profile(s): ${activeProfiles.joinToString(", ")}")
        logger.info("=====================================")
    }
}