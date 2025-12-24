package per.midas.kurumishell.controller

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import per.midas.kurumishell.dto.UserDTO
import per.midas.kurumishell.dto.response.ApiResponse
import per.midas.kurumishell.service.UserService

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getAllUsers(pageable: Pageable): ResponseEntity<ApiResponse<Page<UserDTO>>> {
        val usersPage = userService.findAll(pageable).map { UserDTO.fromEntity(it) }
        return ResponseEntity.ok(
            ApiResponse.success(usersPage)
        )
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: String): ResponseEntity<ApiResponse<UserDTO>> {
        val user = userService.findById(id)
        return ResponseEntity.ok(
            ApiResponse.success(UserDTO.fromEntity(user))
        )
    }

    @PutMapping("/{id}/roles")
    fun assignUserRoles(
        @PathVariable id: String,
        @RequestBody roleIds: Set<Long>
    ): ResponseEntity<ApiResponse<UserDTO>> {
        val updatedUser = userService.assignRolesToUser(id, roleIds)
        return ResponseEntity.ok(
            ApiResponse.success(UserDTO.fromEntity(updatedUser))
        )
    }

    @PatchMapping("/{id}/status")
    fun toggleUserStatus(
        @PathVariable id: String,
        @RequestParam enabled: Boolean
    ): ResponseEntity<ApiResponse<UserDTO>> {
        val updatedUser = userService.toggleUserStatus(id, enabled)
        return ResponseEntity.ok(
            ApiResponse.success(UserDTO.fromEntity(updatedUser))
        )
    }

}