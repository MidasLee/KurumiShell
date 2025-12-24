package per.midas.kurumishell.dto.response

import java.time.Instant

data class TokenInfoResponse(
    val issuedAt: Instant,       // token签发时间
    val expiresAt: Instant,      // token过期时间
    val expiresInSeconds: Long,  // 剩余有效秒数
    val isExpired: Boolean       // 是否已过期
)