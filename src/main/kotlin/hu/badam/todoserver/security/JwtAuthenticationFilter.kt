package hu.badam.todoserver.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
        authenticationManager: AuthenticationManager,
        private val authService: TokenAuthenticationService,
        private val jwtTokenContent: JwtTokenContent
): UsernamePasswordAuthenticationFilter() {
    init {
        this.authenticationManager = authenticationManager
        setFilterProcessesUrl("/api/login")
    }

    override fun successfulAuthentication(r: HttpServletRequest, response: HttpServletResponse, f: FilterChain, authentication: Authentication) {
        val user: User = authentication.principal as User
        authService.addAuthentication(response, jwtTokenContent, user.username)
    }
}
