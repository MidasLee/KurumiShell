package per.midas.kurumishell.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import per.midas.kurumishell.dto.response.TokenInfoResponse
import java.time.Duration
import java.time.Instant
import java.util.*

@Component
class JwtUtils {
    @Value("\${app.jwtSecret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwtExpirationMs}")
    private val jwtExpirationMs: Int = 0

    private val key by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray(Charsets.UTF_8))
    }

    fun generateJwtToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDetailsImpl
        return Jwts.builder()
            .subject(userPrincipal.username)
            .issuedAt(Date())
            .expiration(Date(Date().time + jwtExpirationMs))
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    fun validateJwtToken(authToken: String): Boolean {
        try {
            println("Validating token: ${authToken.take(10)}...")
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(authToken)
            return true
        }catch (e: Exception){
            throw e
        }
    }

    fun getUserNameFromJwtToken(token: String): String {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }

    fun getTokenInfo(token: String): TokenInfoResponse {
        val claims: Claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        val issuedAt = claims.issuedAt.toInstant()
        val expiration = claims.expiration.toInstant()
        val currentTime = Instant.now()

        return TokenInfoResponse(
            issuedAt = issuedAt,
            expiresAt = expiration,
            expiresInSeconds = Duration.between(currentTime, expiration).seconds,
            isExpired = currentTime.isAfter(expiration)
        )
    }
}