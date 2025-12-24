package per.midas.kurumishell.dto.response
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun <T> success(data: T? = null) = ApiResponse(
            code = 200,
            message = "success",
            data = data
        )

        fun error(code: Int, message: String) = ApiResponse<Void>(
            code = code,
            message = message
        )
    }
}