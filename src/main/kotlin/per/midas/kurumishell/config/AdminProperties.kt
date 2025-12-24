package per.midas.kurumishell.config

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.admin")
data class AdminProperties(
    var id: String = "",
    var username: String = "",
    var password: String = "",
    var email: String = ""
) {
    @PostConstruct
    fun validate() {
        require(id.isNotBlank() && username.isNotBlank() && password.isNotBlank() && email.isNotBlank()) {
            "Admin properties must be configured in application.yml"
        }
    }
}