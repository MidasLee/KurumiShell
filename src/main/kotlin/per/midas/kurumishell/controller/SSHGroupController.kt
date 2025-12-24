package per.midas.kurumishell.controller

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import per.midas.kurumishell.dto.response.ApiResponse
import per.midas.kurumishell.dto.response.SSHGroupResponse
import per.midas.kurumishell.entity.SSHGroup
import per.midas.kurumishell.service.SSHGroupService
import per.midas.kurumishell.service.UserService

@RestController
@RequestMapping("/api/ssh-groups")
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
class SSHGroupController(
    private val groupService: SSHGroupService,
    private val userService: UserService
) {
    // 获取用户的所有分组
    @GetMapping("/user/{userId}")
    fun getAllGroupsByUser(@PathVariable userId: String): ResponseEntity<ApiResponse<List<SSHGroupResponse>>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()

        if (groupService.countByUser(user) == 0L) {
            val defaultGroup = SSHGroup(
                name =  "${user.username}的默认分组",
                description = "${user.username}的默认分组"
            )
            groupService.createGroup(defaultGroup, user)
        }

        val groups = groupService.findAllByUser(user)
        val groupResponses = SSHGroupResponse.fromEntityList(groups).sortedBy { it.createdAt }
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取用户分组列表成功",
                data = groupResponses
            )
        )
    }

    // 获取用户的特定分组（带连接）
    @GetMapping("/{id}/user/{userId}")
    fun getGroupByIdAndUser(
        @PathVariable id: String,
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<SSHGroupResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        return groupService.findByIdAndUserWithConnections(id, user)
            ?.let {
                ResponseEntity.ok(
                    ApiResponse(
                        code = 200,
                        message = "获取分组详情成功",
                        data = SSHGroupResponse.fromEntity(it)
                    )
                )
            }
            ?: ResponseEntity.notFound().build()
    }

    // 创建分组
    @PostMapping("/user/{userId}")
    fun createGroup(
        @PathVariable userId: String,
        @RequestBody group: SSHGroup
    ): ResponseEntity<ApiResponse<SSHGroupResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        return try {
            val createdGroup = groupService.createGroup(group, user)
            ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(
                    code = 201,
                    message = "分组创建成功",
                    data = SSHGroupResponse.fromEntity(createdGroup)
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "创建分组失败",
                    data = null
                )
            )
        }
    }

    // 更新分组
    @PutMapping("/{id}/user/{userId}")
    fun updateGroup(
        @PathVariable id: String,
        @PathVariable userId: String,
        @RequestBody group: SSHGroup
    ): ResponseEntity<ApiResponse<SSHGroupResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        return try {
            val updatedGroup = groupService.updateGroup(id, group, user)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "分组更新成功",
                    data = SSHGroupResponse.fromEntity(updatedGroup)
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "更新分组失败",
                    data = null
                )
            )
        }
    }

    // 删除分组
    @DeleteMapping("/{id}/user/{userId}")
    fun deleteGroup(
        @PathVariable id: String,
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<Void>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        return try {
            groupService.deleteGroup(id, user)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "分组删除成功",
                    data = null
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "删除分组失败",
                    data = null
                )
            )
        }
    }

    // 获取用户的分组数量
    @GetMapping("/user/{userId}/count")
    fun getGroupCountByUser(@PathVariable userId: String): ResponseEntity<ApiResponse<Long>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val count = groupService.countByUser(user)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取分组数量成功",
                data = count
            )
        )
    }
}