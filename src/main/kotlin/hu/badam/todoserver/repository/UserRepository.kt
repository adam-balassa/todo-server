package hu.badam.todoserver.repository

import hu.badam.todoserver.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findFirstByEmail(email: String): User?
    fun findByEmailIn(email: Set<String>): Set<User>
}