package per.midas.kurumishell.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import per.midas.kurumishell.dto.response.ApiResponse
import per.midas.kurumishell.dto.request.SSHFileOperationRequest
import per.midas.kurumishell.dto.response.SSHFileInfoResponse
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.service.SSHFileManagementService
import per.midas.kurumishell.service.UserService

@RestController
@RequestMapping("/api/ssh-files")
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
class SSHFileManagementController(
    private val fileManagementService: SSHFileManagementService,
    private val userService: UserService
) {
    
    /**
     * 获取文件列表
     */
    @PostMapping("/{connectionId}/user/{userId}/list")
    fun getFileList(
        @PathVariable connectionId: String,
        @PathVariable userId: String,
        @RequestBody request: SSHFileOperationRequest
    ): ResponseEntity<ApiResponse<List<Map<String, Any>>>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        
        return try {
            val path = request.path ?: throw IllegalArgumentException("文件路径不能为空")
            val fileList = fileManagementService.getFileList(connectionId, user, path)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "获取文件列表成功",
                    data = fileList
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "获取文件列表失败",
                    data = null
                )
            )
        }
    }
    
    /**
     * 读取文件内容
     */
    @PostMapping("/{connectionId}/user/{userId}/read")
    fun readFile(
        @PathVariable connectionId: String,
        @PathVariable userId: String,
        @RequestBody request: SSHFileOperationRequest
    ): ResponseEntity<ApiResponse<String>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        
        return try {
            val content = fileManagementService.readFile(connectionId, user, request)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "读取文件成功",
                    data = content
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "读取文件失败",
                    data = null
                )
            )
        }
    }
    
    /**
     * 写入文件内容
     */
    @PostMapping("/{connectionId}/user/{userId}/write")
    fun writeFile(
        @PathVariable connectionId: String,
        @PathVariable userId: String,
        @RequestBody request: SSHFileOperationRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        
        return try {
            val result = fileManagementService.writeFile(connectionId, user, request)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "写入文件成功",
                    data = result
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "写入文件失败",
                    data = null
                )
            )
        }
    }
    
    /**
     * 创建目录
     */
    @PostMapping("/{connectionId}/user/{userId}/mkdir")
    fun createDirectory(
        @PathVariable connectionId: String,
        @PathVariable userId: String,
        @RequestBody request: SSHFileOperationRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        
        return try {
            val result = fileManagementService.createDirectory(connectionId, user, request)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "创建目录成功",
                    data = result
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "创建目录失败",
                    data = null
                )
            )
        }
    }
    
    /**
     * 删除文件或目录
     */
    @PostMapping("/{connectionId}/user/{userId}/delete")
    fun deleteFile(
        @PathVariable connectionId: String,
        @PathVariable userId: String,
        @RequestBody request: SSHFileOperationRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        
        return try {
            val result = fileManagementService.deleteFile(connectionId, user, request)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "删除文件成功",
                    data = result
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "删除文件失败",
                    data = null
                )
            )
        }
    }
    
    /**
     * 重命名文件或目录
     */
    @PostMapping("/{connectionId}/user/{userId}/rename")
    fun renameFile(
        @PathVariable connectionId: String,
        @PathVariable userId: String,
        @RequestBody request: SSHFileOperationRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        
        return try {
            val result = fileManagementService.renameFile(connectionId, user, request)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "重命名文件成功",
                    data = result
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "重命名文件失败",
                    data = null
                )
            )
        }
    }
    
    /**
     * 上传文件
     */
    @PostMapping("/{connectionId}/user/{userId}/upload")
    fun uploadFile(
        @PathVariable connectionId: String,
        @PathVariable userId: String,
        @RequestParam("file") file: MultipartFile,
        @RequestParam remotePath: String
    ): ResponseEntity<ApiResponse<Boolean>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        
        return try {
            val request = SSHFileOperationRequest(path = remotePath)
            val fileContent = file.bytes
            val result = fileManagementService.uploadFile(connectionId, user, request, fileContent)
            ResponseEntity.ok(
                ApiResponse(
                    code = 200,
                    message = "上传文件成功",
                    data = result
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    code = 400,
                    message = e.message ?: "上传文件失败",
                    data = null
                )
            )
        }
    }
    
    /**
     * 下载文件
     */
    @PostMapping("/{connectionId}/user/{userId}/download")
    fun downloadFile(
        @PathVariable connectionId: String,
        @PathVariable userId: String,
        @RequestBody request: SSHFileOperationRequest
    ): ResponseEntity<ByteArray> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        
        return try {
            val path = request.path ?: throw IllegalArgumentException("文件路径不能为空")
            val fileContent = fileManagementService.downloadFile(connectionId, user, request)
            
            // 设置响应头
            val headers = org.springframework.http.HttpHeaders().apply {
                contentType = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM
                contentDisposition = org.springframework.http.ContentDisposition.builder("attachment")
                    .filename(java.net.URLEncoder.encode(path.substringAfterLast("/"), "UTF-8"))
                    .build()
                contentLength = fileContent.size.toLong()
            }
            
            ResponseEntity(fileContent, headers, HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
}