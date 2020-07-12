package hu.badam.todoserver.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.stereotype.Component
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class JwtAuthorizationFilter(
        authenticationManager: AuthenticationManager,
        private val authService: TokenAuthenticationService,
        private val jwtTokenContent: JwtTokenContent
) : BasicAuthenticationFilter(authenticationManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authentication = authService.getAuthentication(jwtTokenContent, request)

        if (authentication != null) SecurityContextHolder.getContext().authentication = authentication
        else SecurityContextHolder.clearContext()

        filterChain.doFilter(request, response)
    }
}