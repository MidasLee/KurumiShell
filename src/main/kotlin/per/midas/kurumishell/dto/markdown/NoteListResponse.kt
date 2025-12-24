package per.midas.kurumishell.dto.markdown

data class NoteListResponse(
    val data: List<NoteResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int
)
