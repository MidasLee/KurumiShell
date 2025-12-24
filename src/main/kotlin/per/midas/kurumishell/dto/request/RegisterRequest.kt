package per.midas.kurumishell.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "用户编号不能为空")
    val id: String,

    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 4, max = 20, message = "用户名长度4-20个字符")
    val username: String,

    @field:NotBlank(message = "密码不能为空")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$",
        message = "密码必须包含字母和数字，且至少8位"
    )
    val password: String,

    @field:NotBlank(message = "邮箱不能为空")
    @field:Email(message = "邮箱格式不正确")
    val email: String
)