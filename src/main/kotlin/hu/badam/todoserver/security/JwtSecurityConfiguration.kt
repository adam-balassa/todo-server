package hu.badam.todoserver.security

import hu.badam.todoserver.repository.UserAuthorityRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder


@EnableWebSecurity
@Configuration
class JwtSecurityConfiguration (private val authService: TokenAuthenticationService) : WebSecurityConfigurerAdapter() {

    @Value("\${jwt.secret}") private lateinit var jwtSecret: String
    @Value("\${jwt.issuer}") private lateinit var jwtIssuer: String
    @Value("\${jwt.type}") private lateinit var jwtType: String
    @Value("\${jwt.audience}") private lateinit var jwtAudience: String

    @Bean
    fun loginManager(userAuthorityRepository: UserAuthorityRepository): UserDetailsService {
        return UserDetailsService { email ->
            val userAuth = userAuthorityRepository.findByUser_Email(email) ?:
                throw UsernameNotFoundException("No user found with email $email")
            val auth = GrantedAuthority { userAuth.authority.name }
            User(email, userAuth.user.password, mutableListOf(auth))
        }
    }

    @Autowired private lateinit var loginManager: UserDetailsService

    override fun configure(http: HttpSecurity) {
        val jwtTokenContent = JwtTokenContent(jwtAudience, jwtIssuer, jwtSecret, jwtType)
        http
                .csrf().disable()
                .addFilter(JwtAuthenticationFilter(authenticationManager(), authService, jwtTokenContent))
                .addFilter(JwtAuthorizationFilter(authenticationManager(), authService, jwtTokenContent))
                .authorizeRequests()
                    .antMatchers("/api/registration").permitAll()
                    .anyRequest().authenticated()
                    .and()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
                .userDetailsService(loginManager)
                .passwordEncoder(passwordEncoder())
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}