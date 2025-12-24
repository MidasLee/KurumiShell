package per.midas.kurumishell.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import per.midas.kurumishell.entity.MarkdownNote
import per.midas.kurumishell.entity.User

interface MarkdownNoteRepository : JpaRepository<MarkdownNote, String> {
    
    // 根据文件夹ID和用户查询笔记
    fun findByFolderIdAndUser(folderId: String?, user: User, pageable: Pageable): Page<MarkdownNote>
    
    // 根据ID和用户查询笔记
    fun findByIdAndUser(id: String, user: User): MarkdownNote?
    
    // 根据用户查询所有笔记
    fun findByUser(user: User, pageable: Pageable): Page<MarkdownNote>
    
    // 根据关键词搜索用户的笔记（标题、内容、标签）
    @Query("""
        SELECT DISTINCT n FROM MarkdownNote n 
        WHERE (
            LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
            OR CAST(n.content AS string) LIKE CONCAT('%', :keyword, '%')
            OR :keyword MEMBER OF n.tags
        )
        AND n.user = :user
        AND (:folderId IS NULL OR n.folder.id = :folderId)
    """)
    fun searchNotesByUser(keyword: String, folderId: String?, user: User, pageable: Pageable): Page<MarkdownNote>
    
    // 获取用户的所有标签
    @Query("SELECT DISTINCT t FROM MarkdownNote n JOIN n.tags t WHERE n.user = :user")
    fun findAllTagsByUser(user: User): List<String>
    
    // 获取用户的标签统计
    @Query("SELECT t, COUNT(n) FROM MarkdownNote n JOIN n.tags t WHERE n.user = :user GROUP BY t")
    fun countNotesByTagByUser(user: User): List<Array<Any>>
    
    // 根据用户批量删除笔记
    fun deleteByIdInAndUser(ids: List<String>, user: User)
    
    // 根据文件夹ID和用户删除笔记
    fun deleteByFolderIdAndUser(folderId: String, user: User)
}