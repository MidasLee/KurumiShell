package per.midas.kurumishell.dto.request

import jakarta.validation.constraints.NotBlank

data class PrivilegeCreateRequest(
    @field:NotBlank(message = "权限名称不能为空")
    val name: String,

    val description: String? = null
)