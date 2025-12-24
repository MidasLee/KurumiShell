package per.midas.kurumishell.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import per.midas.kurumishell.dto.RoleDTO
import per.midas.kurumishell.dto.request.RoleCreateRequest
import per.midas.kurumishell.dto.request.RoleUpdateRequest
import per.midas.kurumishell.dto.response.ApiResponse
import per.midas.kurumishell.entity.Role
import per.midas.kurumishell.service.RoleService

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ROLE_ADMIN')") // 类级别权限控制
class RoleController(private val roleService: RoleService) {

    @GetMapping
    fun getAllRoles(): ResponseEntity<ApiResponse<List<RoleDTO>>> {
        val roles = roleService.findAll().map { RoleDTO.fromEntity(it) }
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "角色列表获取成功",
                data = roles
            )
        )
    }

    @GetMapping("/{id}")
    fun getRoleById(@PathVariable id: Long): ResponseEntity<ApiResponse<Role>> {
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "角色详情获取成功",
                data = roleService.findById(id)
            )
        )
    }

    @PostMapping
    fun createRole(@Valid @RequestBody request: RoleCreateRequest): ResponseEntity<ApiResponse<Role>> {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                code = 201,
                message = "角色创建成功",
                data = roleService.createRole(request)
            )
        )
    }

    @PutMapping("/{id}")
    fun updateRole(
        @PathVariable id: Long,
        @Valid @RequestBody request: RoleUpdateRequest
    ): ResponseEntity<ApiResponse<Role>> {
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "角色更新成功",
                data = roleService.updateRole(id, request)
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long): ResponseEntity<ApiResponse<Void>> {
        roleService.deleteRole(id)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "角色删除成功",
                data = null
            )
        )
    }

    @GetMapping("/search")
    fun searchRoles(
        @RequestParam name: String?,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<Role>>> {
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "角色搜索成功",
                data = roleService.searchByName(name ?: "", pageable)
            )
        )
    }
}
