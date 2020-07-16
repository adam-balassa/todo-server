package hu.badam.todoserver.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TokenAuthenticationService {
    private val expirationTime: Date by lazy { Calendar.getInstance().apply { add(Calendar.MINUTE, 20) }.time }

    fun generateToken(tokenContent: JwtTokenContent, user: User): String {
        val (jwtAudience, jwtIssuer, jwtSecret, jwtType) = tokenContent
        val secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

        return Jwts.builder().apply {
            signWith(secretKey, SignatureAlgorithm.HS512)
            setHeaderParam("type", jwtType)
            setHeaderParam("authority", user.authority())
            setIssuer(jwtIssuer)
            setAudience(jwtAudience)
            setSubject(user.username)
            setExpiration(expirationTime)
        }.compact()
    }

    fun getAuthentication(tokenContent: JwtTokenContent, request: HttpServletRequest): Authentication? {
        val jwtSecret = tokenContent.jwtSecret
        val token = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (token != null && token.startsWith("Bearer ")) {
            val claims = token.replace("Bearer ", "")
            return try {
                val claimsJws = Jwts.parser().setSigningKey(jwtSecret.toByteArray()).parseClaimsJws(claims)
                val username = claimsJws.body.subject
                val authority = claimsJws.header["authority"] as String

                if ("" == username || username == null) null
                else UsernamePasswordAuthenticationToken(username, null, listOf(SimpleGrantedAuthority(authority)))
            }
            catch (exception: JwtException) { null }
        }
        return null
    }

    fun addAuthentication(res: HttpServletResponse, tokenContent: JwtTokenContent, user: User) {
        val token = generateToken(tokenContent, user)
        res.setHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    }

    fun UserDetails.authority (): String = authorities.toList()[0].authority

}

data class JwtTokenContent(
        val jwtAudience: String,
        val jwtIssuer: String,
        val jwtSecret: String,
        val jwtType: String
)