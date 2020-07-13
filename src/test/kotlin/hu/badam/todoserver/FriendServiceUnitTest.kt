package hu.badam.todoserver

import hu.badam.todoserver.model.Course
import hu.badam.todoserver.model.Task
import hu.badam.todoserver.model.User
import hu.badam.todoserver.model.UserFriends
import hu.badam.todoserver.repository.FriendRepository
import hu.badam.todoserver.repository.TaskRepository
import hu.badam.todoserver.repository.UserRepository
import hu.badam.todoserver.service.TaskService
import hu.badam.todoserver.service.UserFriendsService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean

@SpringBootTest
class FriendServiceUnitTest {
    @TestConfiguration
    internal class UserFriendsServiceImplTestContextConfiguration {
        @Bean
        fun userFriendsService(): UserFriendsService = UserFriendsService()
    }

    @Autowired
    private lateinit var userFriendsService: UserFriendsService

    @MockBean
    private lateinit var userFriendRepository: FriendRepository
    @MockBean
    private lateinit var userRepository: UserRepository

    val user = User.of(email = "user@test.com").apply { id = 0 }
    val user1 = User.of(email = "user1@test.com").apply { id = 1 }
    val user2 = User.of(email = "user2@test.com").apply { id = 2 }

    val userFriend = UserFriends.of(user = user).apply { id = 0 }

    @BeforeEach
    fun setUp() {
        Mockito.`when`(userRepository.findFirstByEmail(user.email)).thenReturn(user)
        Mockito.`when`(userRepository.findFirstByEmail(user1.email)).thenReturn(user1)
        Mockito.`when`(userRepository.findFirstByEmail(user2.email)).thenReturn(user2)

        Mockito.`when`(userFriendRepository.findByUser_Email(user.email)).thenReturn(userFriend)
        Mockito.`when`(userFriendRepository.save(any<UserFriends>())).then {
            return@then it.arguments[0] as UserFriends
        }
    }

    @Test
    fun sendFriendRequest_simpleRequest_success() {
        // when
        userFriendsService.sendFriendRequest(user1.email, user.email)

        // then
        val desired = UserFriends.of(user = user, friendRequests = mutableSetOf(user1))
        val saved = ArgumentCaptor.forClass(UserFriends::class.java)
        Mockito.verify(userFriendRepository).save(saved.capture())
        assertUserFriendsEquals(saved.value, desired)
    }

}

