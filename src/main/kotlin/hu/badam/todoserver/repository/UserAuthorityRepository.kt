package hu.badam.todoserver.repository

import hu.badam.todoserver.model.UserAuthority
import org.springframework.data.jpa.repository.JpaRepository

interface UserAuthorityRepository: JpaRepository<UserAuthority, Long> {
    fun findByUser_Email(email: String): UserAuthority?
}