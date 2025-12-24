package per.midas.kurumishell.dto.markdown

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class NoteResponse(
    val id: String,
    val title: String,
    val content: String,
    val tags: List<String>,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime,
    val folderId: String?,
    val folderName: String? = null
)
