package hu.badam.todoserver

import hu.badam.todoserver.model.*
import hu.badam.todoserver.repository.CourseRepository
import hu.badam.todoserver.repository.DeadlineRepository
import hu.badam.todoserver.repository.TaskRepository
import hu.badam.todoserver.repository.UserRepository
import hu.badam.todoserver.service.TaskService
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@SpringBootTest
@AutoConfigureTestDatabase
class TaskServiceUnitTest {

    @Autowired
    private lateinit var taskService: TaskService

    @MockBean
    private lateinit var taskRepository: TaskRepository
    @MockBean
    private lateinit var userRepository: UserRepository
    @MockBean
    private lateinit var courseRepository: CourseRepository
    @MockBean
    private lateinit var deadlineRepository: DeadlineRepository


    val user = User.of(email = "user@test.com").apply { id = 0 }
    val course = Course.of(owner = user).apply { id = 0 }
    val storedTask = Task.of(owner = user).apply { id = 0 }

    @BeforeEach
    fun setUp() {
        Mockito.`when`(userRepository.findFirstByEmail(user.email)).thenReturn(user)
        Mockito.`when`(userRepository.findByEmailIn(Mockito.any<Set<String>>() ?: emptySet())).then {
            val emails = it.arguments[0] as Set<String>
            if (emails.contains(user.email)) setOf(user)
            else emptySet()
        }

        Mockito.`when`(courseRepository.findByIdIfOwned(0, 0)).thenReturn(course)
        Mockito.`when`(courseRepository.findByIdIfOwned(0, 1)).thenReturn(null)
        Mockito.`when`(courseRepository.findById(0)).thenReturn(Optional.of(course))


        Mockito.`when`(taskRepository.findById(0)).thenReturn(Optional.of(storedTask))
        Mockito.`when`(taskRepository.save(Mockito.any<Task>())).then {
            return@then it.arguments[0] as Task
        }

        Mockito.`when`(deadlineRepository.findByIdOrNull(0)).thenReturn(null)
    }

    @Test
    fun contextLoads() {}

    @Test
    fun save_simpleTask_success() {
        //given
        val taskToSave = UploadableTask.of(ownerEmail = user.email)

        //when
        taskService.save(taskToSave)

        //then
        val desired = Task.of(owner = user)
        val saved = ArgumentCaptor.forClass(Task::class.java)
        Mockito.verify(taskRepository).save(saved.capture())
        assertTaskEquals(saved.value, desired)
    }

    @Test
    fun save_taskWithCourse_success() {
        //given
        val taskToSave = UploadableTask.of(ownerEmail = user.email, courseId = course.id)

        //when
        taskService.save(taskToSave)

        //then
        val desired = Task.of(owner = user, course = course)
        val saved = ArgumentCaptor.forClass(Task::class.java)
        Mockito.verify(taskRepository).save(saved.capture())
        assertTaskEquals(saved.value, desired)
    }

    @Test
    fun save_taskWithAssigned_success() {
        //given
        val taskToSave = UploadableTask.of(ownerEmail = user.email, assigned = setOf(user.email))

        //when
        taskService.save(taskToSave)

        //then
        val desired = Task.of(owner = user)
        val saved = ArgumentCaptor.forClass(Task::class.java)
        Mockito.verify(taskRepository).save(saved.capture())
        assertTaskEquals(saved.value, desired)
    }

    @Test
    fun save_taskWithNotExistingCourse_exceptionThrown() {
        //given
        val taskToSave = UploadableTask.of(ownerEmail = user.email, courseId = 1)

        //when, then
        assertThatIllegalArgumentException().isThrownBy {
            taskService.save(taskToSave)
        }.withMessage("Invalid course id")
    }

    @Test
    fun save_taskWithNotPermittedCourse_exceptionThrown() {
        //given
        val otherUser = User.of("other@test.com")
        val notPermittedCourse = Course.of(owner = otherUser).apply { id = 1 }
        Mockito.`when`(userRepository.findFirstByEmail(otherUser.email)).thenReturn(otherUser)
        Mockito.`when`(courseRepository.findByIdIfOwned(notPermittedCourse.id!!, otherUser.id)).thenReturn(notPermittedCourse)

        val taskToSave = UploadableTask.of(ownerEmail = user.email, courseId = 1)

        //when, then
        assertThatIllegalArgumentException().isThrownBy {
            taskService.save(taskToSave)
        }.withMessage("Invalid course id")
    }

    @Test
    fun save_taskWithInvalidUserId_exceptionThrown() {
        //given
        val taskToSave = UploadableTask.of(ownerEmail = "invalid email address")

        //when, then
        assertThatIllegalArgumentException().isThrownBy {
            taskService.save(taskToSave)
        }.withMessage("Invalid user id")
    }

    @Test
    fun save_taskWithEmptyName_exceptionThrown() {
        //given
        val taskToSave = UploadableTask.of(name = "", ownerEmail = user.email)

        //when, then
        assertThatIllegalArgumentException().isThrownBy {
            taskService.save(taskToSave)
        }
    }

    @Test
    fun save_taskWithInvalidDates_exceptionThrown() {
        //given
        val taskToSave = UploadableTask.of(ownerEmail = user.email, startDate = dateOf(date = 2), endDate = dateOf(date = 1))

        //when, then
        assertThatIllegalArgumentException().isThrownBy {
            taskService.save(taskToSave)
        }
    }

    @Test
    fun update_simpleTask_success() {
        //given
        val taskToUpdate = UpdatableTask.of(id = 0, name = "New name")

        //when
        taskService.update(taskToUpdate, user.email)

        //then
        val desired = Task.of(owner = user, name = taskToUpdate.name!!)
        val saved = ArgumentCaptor.forClass(Task::class.java)
        Mockito.verify(taskRepository).save(saved.capture())
        assertTaskEquals(saved.value, desired)
    }

    @Test
    fun update_taskWithCourse_success() {
        //given
        val taskToUpdate = UpdatableTask.of(id = 0, courseId = course.id)

        //when
        taskService.update(taskToUpdate, user.email)

        //then
        val desired = Task.of(owner = user, course = course)
        val saved = ArgumentCaptor.forClass(Task::class.java)
        Mockito.verify(taskRepository).save(saved.capture())
        assertTaskEquals(saved.value, desired)
    }

    @Test
    fun update_taskWithInvalidCourse_exceptionThrown() {
        //given
        val taskToUpdate = UpdatableTask.of(id = 0, courseId = 1)

        //when, then
        assertThatIllegalArgumentException().isThrownBy {
            taskService.update(taskToUpdate, user.email)
        }.withMessage("Invalid course id")
    }

    @Test
    fun update_invalidTask_exceptionThrown() {
        //given
        val taskToUpdate = UpdatableTask.of(id = 1)

        //when, then
        assertThatIllegalArgumentException().isThrownBy {
            taskService.update(taskToUpdate, user.email)
        }.withMessage("Invalid task id")
    }
}