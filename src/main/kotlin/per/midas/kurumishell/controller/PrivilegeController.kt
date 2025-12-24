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
import per.midas.kurumishell.dto.request.PrivilegeCreateRequest
import per.midas.kurumishell.dto.request.PrivilegeUpdateRequest
import per.midas.kurumishell.dto.response.ApiResponse
import per.midas.kurumishell.entity.Privilege
import per.midas.kurumishell.entity.Role
import per.midas.kurumishell.service.PrivilegeService

@RestController
@RequestMapping("/api/privileges")
@PreAuthorize("hasRole('ROLE_ADMIN')")
class PrivilegeController(private val privilegeService: PrivilegeService) {

    @GetMapping
    fun getAllPrivileges(pageable: Pageable): ResponseEntity<ApiResponse<Page<Privilege>>> {
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "权限列表获取成功",
                data = privilegeService.findAll(pageable)
            )
        )
    }

    @GetMapping("/{id}")
    fun getPrivilegeById(@PathVariable id: Long): ResponseEntity<ApiResponse<Privilege>> {
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "权限详情获取成功",
                data = privilegeService.findById(id)
            )
        )
    }

    @PostMapping
    fun createPrivilege(@Valid @RequestBody request: PrivilegeCreateRequest): ResponseEntity<ApiResponse<Privilege>> {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                code = 201,
                message = "权限创建成功",
                data = privilegeService.createPrivilege(request)
            )
        )
    }

    @PutMapping("/{id}")
    fun updatePrivilege(
        @PathVariable id: Long,
        @Valid @RequestBody request: PrivilegeUpdateRequest
    ): ResponseEntity<ApiResponse<Privilege>> {
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "权限更新成功",
                data = privilegeService.updatePrivilege(id, request)
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deletePrivilege(@PathVariable id: Long): ResponseEntity<ApiResponse<Void>> {
        privilegeService.deletePrivilege(id)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "权限删除成功",
                data = null
            )
        )
    }

    @GetMapping("/search")
    fun searchPrivileges(
        @RequestParam keyword: String,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<Privilege>>> {
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "权限搜索成功",
                data = privilegeService.search(keyword, pageable)
            )
        )
    }

    @GetMapping("/{id}/roles")
    fun getRolesByPrivilege(
        @PathVariable id: Long,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<Role>>> {
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "拥有该权限的角色列表获取成功",
                data = privilegeService.findRolesByPrivilegeId(id, pageable)
            )
        )
    }
}