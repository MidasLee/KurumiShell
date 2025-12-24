package per.midas.kurumishell.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import per.midas.kurumishell.dto.markdown.*
import per.midas.kurumishell.dto.response.ApiResponse
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.service.MarkdownService
import per.midas.kurumishell.service.UserService

@RestController
@RequestMapping("/api/markdown")
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
class MarkdownController(
    private val markdownService: MarkdownService,
    private val userService: UserService
) {
    
    // 笔记相关接口
    @GetMapping("/notes/user/{userId}")
    fun getNoteList(@PathVariable userId: String, @RequestParam(required = false) keyword: String?, 
                   @RequestParam(required = false) folderId: String?, 
                   @RequestParam(required = false) sortBy: String = "updatedAt",
                   @RequestParam(required = false) sortOrder: String = "desc",
                   @RequestParam(required = false, defaultValue = "1") page: Int,
                   @RequestParam(required = false, defaultValue = "10") pageSize: Int): ResponseEntity<ApiResponse<NoteListResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val response = markdownService.getNoteList(user, keyword, folderId, sortBy, sortOrder, page, pageSize)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取笔记列表成功",
                data = response
            )
        )
    }

    @GetMapping("/notes/{id}/user/{userId}")
    fun getNoteDetail(@PathVariable id: String, @PathVariable userId: String): ResponseEntity<ApiResponse<NoteResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val response = markdownService.getNoteDetail(user, id)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取笔记详情成功",
                data = response
            )
        )
    }

    @PostMapping("/notes/user/{userId}")
    fun createNote(@PathVariable userId: String, @RequestBody request: CreateNoteRequest): ResponseEntity<ApiResponse<NoteResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val response = markdownService.createNote(user, request)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "创建笔记成功",
                data = response
            )
        )
    }

    @PutMapping("/notes/{id}/user/{userId}")
    fun updateNote(@PathVariable id: String, @PathVariable userId: String, @RequestBody request: UpdateNoteRequest): ResponseEntity<ApiResponse<NoteResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val response = markdownService.updateNote(user, id, request)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "更新笔记成功",
                data = response
            )
        )
    }

    @DeleteMapping("/notes/{id}/user/{userId}")
    fun deleteNote(@PathVariable id: String, @PathVariable userId: String): ResponseEntity<ApiResponse<Void>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        markdownService.deleteNote(user, id)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "删除笔记成功",
                data = null
            )
        )
    }

    @DeleteMapping("/notes/batch/user/{userId}")
    fun batchDeleteNotes(@RequestBody ids: List<String>, @PathVariable userId: String): ResponseEntity<ApiResponse<Void>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        markdownService.batchDeleteNotes(user, ids)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "批量删除笔记成功",
                data = null
            )
        )
    }

    // 文件夹相关接口
    @GetMapping("/folders/user/{userId}")
    fun getFolderTree(@PathVariable userId: String): ResponseEntity<ApiResponse<List<FolderResponse>>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        
        // 检查用户是否有文件夹，如果没有则创建默认文件夹
        if (markdownService.countByUser(user) == 0L) {
            val defaultFolderRequest = CreateFolderRequest(
                name = "${user.username}的默认文件夹",
                parentId = null
            )
            markdownService.createFolder(user, defaultFolderRequest)
        }
        
        val response = markdownService.getFolderTree(user)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取文件夹列表成功",
                data = response
            )
        )
    }

    @PostMapping("/folders/user/{userId}")
    fun createFolder(@PathVariable userId: String, @RequestBody request: CreateFolderRequest): ResponseEntity<ApiResponse<FolderResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val response = markdownService.createFolder(user, request)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "创建文件夹成功",
                data = response
            )
        )
    }

    @PutMapping("/folders/{id}/user/{userId}")
    fun updateFolder(@PathVariable id: String, @PathVariable userId: String, @RequestBody request: UpdateFolderRequest): ResponseEntity<ApiResponse<FolderResponse>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val response = markdownService.updateFolder(user, id, request)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "更新文件夹成功",
                data = response
            )
        )
    }

    @DeleteMapping("/folders/{id}/user/{userId}")
    fun deleteFolder(@PathVariable id: String, @PathVariable userId: String): ResponseEntity<ApiResponse<Void>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        markdownService.deleteFolder(user, id)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "删除文件夹成功",
                data = null
            )
        )
    }

    // 标签相关接口
    @GetMapping("/tags/user/{userId}")
    fun getAllTags(@PathVariable userId: String): ResponseEntity<ApiResponse<List<String>>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val response = markdownService.getAllTags(user)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取标签列表成功",
                data = response
            )
        )
    }

    @GetMapping("/tags/stats/user/{userId}")
    fun getTagStats(@PathVariable userId: String): ResponseEntity<ApiResponse<Map<String, Long>>> {
        val user = userService.findById(userId) ?: return ResponseEntity.notFound().build()
        val response = markdownService.getTagStats(user)
        return ResponseEntity.ok(
            ApiResponse(
                code = 200,
                message = "获取标签统计信息成功",
                data = response
            )
        )
    }
}
