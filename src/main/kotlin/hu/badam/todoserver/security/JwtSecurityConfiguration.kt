package hu.badam.todoserver.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.access.AccessDeniedHandler
import java.util.*
import javax.sql.DataSource


@EnableWebSecurity
class JwtSecurityConfiguration : WebSecurityConfigurerAdapter() {

    @Value("\${jwt.secret}") private lateinit var jwtSecret: String
    @Value("\${jwt.issuer}") private lateinit var jwtIssuer: String
    @Value("\${jwt.type}") private lateinit var jwtType: String
    @Value("\${jwt.audience}") private lateinit var jwtAudience: String

    @Autowired
    private lateinit var dataSource: DataSource

    @Autowired
    private lateinit var authService: TokenAuthenticationService

    override fun configure(http: HttpSecurity) {
        val jwtTokenContent = JwtTokenContent(jwtAudience, jwtIssuer, jwtSecret, jwtType)

        http.csrf().disable()
                .addFilter(JwtAuthenticationFilter(authenticationManager(), authService, jwtTokenContent))
                .addFilter(JwtAuthorizationFilter(authenticationManager(), authService, jwtTokenContent))
                .authorizeRequests { authorizeRequests ->
                    authorizeRequests
                            .antMatchers("/api/login", "/api/register").permitAll()
                            .anyRequest().hasAuthority("USER")
                }
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
                .jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery("SELECT email as username, password, true as enabled from users where email = ?")
                .authoritiesByUsernameQuery("""
                    |SELECT u.email as username, a.authority 
                    |FROM user_authorities a, users u WHERE u.email = ? 
                    |AND u.id = a.user_id""".trimMargin())
    }


    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}