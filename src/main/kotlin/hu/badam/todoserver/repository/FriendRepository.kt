package hu.badam.todoserver.repository

import hu.badam.todoserver.model.User
import hu.badam.todoserver.model.UserFriends
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRepository : JpaRepository<UserFriends, Long> {
    fun findByUser_Email(email: String): UserFriends?
}