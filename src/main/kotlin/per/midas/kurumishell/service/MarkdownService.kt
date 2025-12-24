package per.midas.kurumishell.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import per.midas.kurumishell.dto.markdown.*
import per.midas.kurumishell.entity.MarkdownFolder
import per.midas.kurumishell.entity.MarkdownNote
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.repository.MarkdownFolderRepository
import per.midas.kurumishell.repository.MarkdownNoteRepository

@Service
@Transactional
class MarkdownService(
    private val noteRepository: MarkdownNoteRepository,
    private val folderRepository: MarkdownFolderRepository
) {
    
    // 笔记相关方法
    fun getNoteList(user: User, keyword: String?, folderId: String?, sortBy: String, sortOrder: String, page: Int, pageSize: Int): NoteListResponse {
        val sortDirection = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page - 1, pageSize, Sort.by(sortDirection, sortBy))
        
        val notePage = if (keyword.isNullOrBlank()) {
            if (folderId.isNullOrBlank()) {
                noteRepository.findByUser(user, pageable)
            } else {
                noteRepository.findByFolderIdAndUser(folderId, user, pageable)
            }
        } else {
            noteRepository.searchNotesByUser(keyword, folderId, user, pageable)
        }
        
        val noteResponses = notePage.content.map { noteToResponse(it) }
        
        return NoteListResponse(
            data = noteResponses,
            total = notePage.totalElements,
            page = page,
            pageSize = pageSize
        )
    }
    
    fun getNoteDetail(user: User, id: String): NoteResponse {
        val note = noteRepository.findByIdAndUser(id, user)
        if (note == null) {
            throw RuntimeException("Note not found with id: $id")
        }
        return noteToResponse(note)
    }
    
    @Transactional
    fun createNote(user: User, request: CreateNoteRequest): NoteResponse {
        val folder = request.folderId?.let { folderRepository.findByIdAndUser(it, user) }
        
        val note = MarkdownNote(
            title = request.title,
            content = request.content,
            tags = request.tags.toMutableList(),
            folder = folder,
            user = user
        )
        
        val savedNote = noteRepository.save(note)
        return noteToResponse(savedNote)
    }
    
    @Transactional
    fun updateNote(user: User, id: String, request: UpdateNoteRequest): NoteResponse {
        val note = noteRepository.findByIdAndUser(id, user)
        if (note == null) {
            throw RuntimeException("Note not found with id: $id")
        }
        
        request.title?.let { note.title = it }
        request.content?.let { note.content = it }
        request.tags?.let { note.tags = it.toMutableList() }
        request.folderId?.let { 
            note.folder = folderRepository.findByIdAndUser(it, user)
        }
        
        val updatedNote = noteRepository.save(note)
        return noteToResponse(updatedNote)
    }
    
    @Transactional
    fun deleteNote(user: User, id: String) {
        val note = noteRepository.findByIdAndUser(id, user)
        if (note == null) {
            throw RuntimeException("Note not found with id: $id")
        }
        noteRepository.deleteById(id)
    }
    
    @Transactional
    fun batchDeleteNotes(user: User, ids: List<String>) {
        val existingNotes = ids.mapNotNull { noteRepository.findByIdAndUser(it, user) }
        if (existingNotes.size != ids.size) {
            throw RuntimeException("Some notes not found")
        }
        noteRepository.deleteByIdInAndUser(ids, user)
    }
    
    // 文件夹相关方法
    fun countByUser(user: User): Long {
        return folderRepository.countByUser(user)
    }
    
    fun getFolderTree(user: User): List<FolderResponse> {
        val rootFolders = folderRepository.findRootFoldersWithChildrenByUser(user)
        return rootFolders.map { folderToResponse(it) }
    }
    
    @Transactional
    fun createFolder(user: User, request: CreateFolderRequest): FolderResponse {
        val parent = request.parentId?.let { folderRepository.findByIdAndUser(it, user) }
        
        val folder = MarkdownFolder(
            name = request.name,
            parent = parent,
            user = user
        )
        
        val savedFolder = folderRepository.save(folder)
        return folderToResponse(savedFolder)
    }
    
    @Transactional
    fun updateFolder(user: User, id: String, request: UpdateFolderRequest): FolderResponse {
        val folder = folderRepository.findByIdAndUser(id, user)
        if (folder == null) {
            throw RuntimeException("Folder not found with id: $id")
        }
        folder.name = request.name
        
        val updatedFolder = folderRepository.save(folder)
        return folderToResponse(updatedFolder)
    }
    
    @Transactional
    fun deleteFolder(user: User, id: String) {
        val folder = folderRepository.findByIdAndUser(id, user)
        if (folder == null) {
            throw RuntimeException("Folder not found with id: $id")
        }
        
        // 递归删除所有子文件夹
        folder.children.forEach { childFolder ->
            deleteFolder(user, childFolder.id)
        }
        
        // 删除该文件夹下的所有笔记
        noteRepository.deleteByFolderIdAndUser(id, user)
        
        // 删除当前文件夹
        folderRepository.delete(folder)
    }
    
    // 标签相关方法
    fun getAllTags(user: User): List<String> {
        return noteRepository.findAllTagsByUser(user)
    }
    
    fun getTagStats(user: User): Map<String, Long> {
        val tagCounts = noteRepository.countNotesByTagByUser(user)
        return tagCounts.associate { it[0] as String to it[1] as Long }
    }
    
    // 工具方法：转换Note实体到Response
    private fun noteToResponse(note: MarkdownNote): NoteResponse {
        return NoteResponse(
            id = note.id,
            title = note.title,
            content = note.content,
            tags = note.tags,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            folderId = note.folder?.id,
            folderName = note.folder?.name
        )
    }
    
    // 工具方法：转换Folder实体到Response
    private fun folderToResponse(folder: MarkdownFolder): FolderResponse {
        val childrenResponses = folder.children.map { folderToResponse(it) }
        
        return FolderResponse(
            id = folder.id,
            name = folder.name,
            createdAt = folder.createdAt,
            parentId = folder.parent?.id,
            children = childrenResponses
        )
    }
}
