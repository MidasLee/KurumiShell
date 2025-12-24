package per.midas.kurumishell.dto.markdown

data class CreateFolderRequest(
    val name: String,
    val parentId: String? = null
)
