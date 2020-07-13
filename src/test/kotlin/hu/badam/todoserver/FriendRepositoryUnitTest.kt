package hu.badam.todoserver

import hu.badam.todoserver.model.User
import hu.badam.todoserver.model.UserFriends
import hu.badam.todoserver.repository.FriendRepository
import hu.badam.todoserver.repository.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import javax.transaction.Transactional

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureDataJpa
@Transactional
class FriendRepositoryUnitTest {
    @Autowired
    private lateinit var em: TestEntityManager
    @Autowired
    private lateinit var friendRepository: FriendRepository

    @Test
    fun testContextLoads() {}

    @Test
    fun findByUser_Email_simpleUser_success() {
        // given
        val userFriendsA = User.of(email = "a@test.com").let {
            em.persist(it)
            em.persist(UserFriends.of(user = it))
        }

        // when
        val userFriends = friendRepository.findByUser_Email(userFriendsA.user.email)

        // then
        assertThat(userFriends).isEqualTo(userFriendsA)
    }

    @Test
    fun findByUser_Email_simpleUserWithFriends_success() {
        // given
        val userFriendsA = User.of(email = "a@test.com").let {
            em.persist(it)
            em.persist(UserFriends.of(user = it))
        }

        val userFriendsB = User.of(email = "b@test.com").let {
            em.persist(it)
            em.persist(UserFriends.of(user = it, friends = mutableSetOf(userFriendsA.user)))
        }

        // when
        val userFriends = friendRepository.findByUser_Email(userFriendsB.user.email)

        // then
        assertThat(userFriends).isEqualTo(userFriendsB)
    }

    @Test
    fun findByUser_Email_simpleUserWithFriendRequest_success() {
        // given
        val userFriendsA = User.of(email = "a@test.com").let {
            em.persist(it)
            em.persist(UserFriends.of(user = it))
        }

        val userFriendsB = User.of(email = "b@test.com").let {
            em.persist(it)
            em.persist(UserFriends.of(user = it, friendRequests = mutableSetOf(userFriendsA.user)))
        }

        // when
        val userFriends = friendRepository.findByUser_Email(userFriendsB.user.email)

        // then
        assertThat(userFriends).isEqualTo(userFriendsB)
    }

}