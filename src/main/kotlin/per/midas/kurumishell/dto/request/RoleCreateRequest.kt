package per.midas.kurumishell.dto.request

import jakarta.validation.constraints.NotBlank

data class RoleCreateRequest(
    @field:NotBlank(message = "角色名称不能为空")
    val name: String,

    val description: String? = null
)