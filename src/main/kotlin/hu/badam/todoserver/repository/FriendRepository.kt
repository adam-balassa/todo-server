package hu.badam.todoserver.repository

import hu.badam.todoserver.model.UserFriends
import hu.badam.todoserver.util.criteriaQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

interface FriendRepository : JpaRepository<UserFriends, Long> {
    fun findByUser_Email(email: String): UserFriends?
}
