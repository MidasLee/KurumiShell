package per.midas.kurumishell.dto.markdown

data class UpdateNoteRequest(
    val title: String? = null,
    val content: String? = null,
    val tags: List<String>? = null,
    val folderId: String? = null
)
