package per.midas.kurumishell.dto.markdown

data class CreateNoteRequest(
    val title: String,
    val content: String,
    val tags: List<String>,
    val folderId: String? = null
)
