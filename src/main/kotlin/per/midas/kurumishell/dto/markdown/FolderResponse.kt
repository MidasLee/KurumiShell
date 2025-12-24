package per.midas.kurumishell.dto.markdown

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class FolderResponse(
    val id: String,
    val name: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
    val parentId: String?,
    val children: List<FolderResponse>? = null
)
