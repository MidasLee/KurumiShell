package per.midas.kurumishell.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import per.midas.kurumishell.entity.MarkdownFolder
import per.midas.kurumishell.entity.User

interface MarkdownFolderRepository : JpaRepository<MarkdownFolder, String> {
    
    // 根据用户查询所有文件夹
    fun findByUser(user: User): List<MarkdownFolder>
    
    // 根据用户查询文件夹数量
    fun countByUser(user: User): Long
    
    // 根据用户查询所有根文件夹（没有父文件夹的文件夹）
    fun findByParentIsNullAndUser(user: User): List<MarkdownFolder>
    
    // 根据父文件夹ID和用户查询子文件夹
    fun findByParentIdAndUser(parentId: String, user: User): List<MarkdownFolder>
    
    // 根据ID和用户查询文件夹
    fun findByIdAndUser(id: String, user: User): MarkdownFolder?
    
    // 查询用户的文件夹树结构
    @Query("""
        SELECT f FROM MarkdownFolder f 
        LEFT JOIN FETCH f.children
        LEFT JOIN FETCH f.parent
        WHERE f.parent IS NULL AND f.user = :user
    """)
    fun findRootFoldersWithChildrenByUser(user: User): List<MarkdownFolder>
}
