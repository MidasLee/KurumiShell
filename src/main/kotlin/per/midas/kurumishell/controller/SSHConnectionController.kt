package per.midas.kurumishell.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import per.midas.kurumishell.dto.response.ApiResponse
import per.midas.kurumishell.dto.response.SSHConnectionResponse
import per.midas.kurumishell.dto.request.SSHConnectionRequest
import per.midas.kurumishell.entity.SSHConnection
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.service.SSHConnectionService
import per.midas.kurumishell.service.UserService

@RestController
@RequestMapping("/api/ssh-connections")
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
class SSHConnectionController(
    private val connectionService: SSHConnectionService,
    private val userService: UserService
) {
    // 获取用户的所有连接
    @GetMapping("/user/{userId}")
    fun getAllConnectionsByUser(@PathVariable userId: String): ResponseEntity<ApiResponse<List<SSHConnectionResponse>>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val connections = connectionService.findAllByUser(user)
        val connectionResponses = connections.map { SSHConnectionResponse.fromEntity(it) }
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取用户连接列表成功",
                data = connectionResponses
            )
        )
    }

    // 获取用户特定分组下的连接
    @GetMapping("/user/{userId}/group/{groupId}")
    fun getConnectionsByUserAndGroup(
        @PathVariable userId: String,
        @PathVariable groupId: String
    ): ResponseEntity<ApiResponse<List<SSHConnectionResponse>>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val connections = connectionService.findByUserAndGroupId(user, groupId)
        val connectionResponses = connections.map { SSHConnectionResponse.fromEntity(it) }
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取用户特定分组下的连接成功",
                data = connectionResponses
            )
        )
    }

    // 获取用户的特定连接
    @GetMapping("/{id}/user/{userId}")
    fun getConnectionByIdAndUser(
        @PathVariable id: String,
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<SSHConnectionResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        return connectionService.findByIdAndUser(id, user)
            ?.let {
                ResponseEntity.ok(
                    ApiResponse(
                        code = 200,
                        message = "获取连接详情成功",
                        data = SSHConnectionResponse.fromEntity(it)
                    )
                )
            }
            ?: ResponseEntity.notFound().build()
    }

    // 创建连接
    @PostMapping("/user/{userId}")
    fun createConnection(
        @PathVariable userId: String,
        @RequestBody request: SSHConnectionRequest  // 改为接收 DTO
    ): ResponseEntity<ApiResponse<SSHConnectionResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        
        return try {
            val createdConnection = connectionService.createConnection(request, user)
            ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(
                    code = 201,
                    message = "连接创建成功",
                    data = SSHConnectionResponse.fromEntity(createdConnection)
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "创建连接失败",
                    data = null
                )
            )
        }
    }

    // 更新连接
    @PutMapping("/{id}/user/{userId}")
    fun updateConnection(
        @PathVariable id: String,
        @PathVariable userId: String,
        @RequestBody connection: SSHConnection
    ): ResponseEntity<ApiResponse<SSHConnectionResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        return try {
            val updatedConnection = connectionService.updateConnection(id, connection, user)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "连接更新成功",
                    data = SSHConnectionResponse.fromEntity(updatedConnection)
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "更新连接失败",
                    data = null
                )
            )
        }
    }

    // 删除连接
    @DeleteMapping("/{id}/user/{userId}")
    fun deleteConnection(
        @PathVariable id: String,
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<Void>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        return try {
            connectionService.deleteConnection(id, user)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "连接删除成功",
                    data = null
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "删除连接失败",
                    data = null
                )
            )
        }
    }

    // 批量删除用户的所有连接
    @DeleteMapping("/user/{userId}")
    fun deleteAllConnectionsByUser(@PathVariable userId: String): ResponseEntity<ApiResponse<Void>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        connectionService.deleteAllByUser(user)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "批量删除连接成功",
                data = null
            )
        )
    }

    // 新增：获取用户的连接数量
    @GetMapping("/user/{userId}/count")
    fun getConnectionCountByUser(@PathVariable userId: String): ResponseEntity<ApiResponse<Long>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val count = connectionService.countByUser(user)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取连接数量成功",
                data = count
            )
        )
    }

    // 新增：移动连接到其他分组
    @PatchMapping("/{id}/user/{userId}/move-to-group/{newGroupId}")
    fun moveConnectionToGroup(
        @PathVariable id: String,
        @PathVariable userId: String,
        @PathVariable newGroupId: String
    ): ResponseEntity<ApiResponse<SSHConnectionResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        return try {
            val movedConnection = connectionService.moveConnectionToGroup(id, newGroupId, user)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "连接移动成功",
                    data = SSHConnectionResponse.fromEntity(movedConnection)
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "移动连接失败",
                    data = null
                )
            )
        }
    }

    // 新增：将连接从分组中移除（设置为无分组）
    @PatchMapping("/{id}/user/{userId}/remove-from-group")
    fun removeConnectionFromGroup(
        @PathVariable id: String,
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<SSHConnectionResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        return try {
            val updatedConnection = connectionService.moveConnectionToGroup(id, null, user)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "连接从分组移除成功",
                    data = SSHConnectionResponse.fromEntity(updatedConnection)
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "移除连接失败",
                    data = null
                )
            )
        }
    }
}