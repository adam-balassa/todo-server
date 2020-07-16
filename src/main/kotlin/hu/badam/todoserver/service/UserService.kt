package hu.badam.todoserver.service

import hu.badam.todoserver.model.User
import hu.badam.todoserver.model.UserAuthority
import hu.badam.todoserver.model.UserFriends
import hu.badam.todoserver.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Service
class UserService (private val userRepository: UserRepository) {

    @PersistenceContext
    private lateinit var em: EntityManager

    @Transactional
    fun registerUser(user: User) {
        require(userRepository.findFirstByEmail(user.email) == null) { "User has already registered" }

        val userAuthority = UserAuthority().apply {
            authority = UserAuthority.Authority.USER
            this.user = user
        }

        val userFriends = UserFriends().apply {
            this.user = user
            this.friends = mutableSetOf()
            this.friendRequests = mutableSetOf()
        }

        userRepository.save(user)
        em.persist(userAuthority)
        em.persist(userFriends)
    }
}