package hu.badam.todoserver

import com.fasterxml.jackson.databind.ObjectMapper
import hu.badam.todoserver.model.Course
import hu.badam.todoserver.model.Task
import hu.badam.todoserver.model.UploadableTask
import hu.badam.todoserver.model.User
import hu.badam.todoserver.repository.CourseRepository
import hu.badam.todoserver.repository.DeadlineRepository
import hu.badam.todoserver.repository.TaskRepository
import hu.badam.todoserver.repository.UserRepository
import hu.badam.todoserver.security.JwtTokenContent
import hu.badam.todoserver.security.TokenAuthenticationService
import hu.badam.todoserver.service.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*
import javax.servlet.http.HttpServletRequest


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class TaskControllerUnitTest {

	@Autowired
	private lateinit var mvc: MockMvc

	@TestConfiguration
	internal class TokenAuthenticationImplConfiguration {
		@Bean
		fun getTokenAuthenticationService(): TokenAuthenticationService = object : TokenAuthenticationService() {
			override fun generateToken(tokenContent: JwtTokenContent, userName: String): String = ""
			override fun getAuthentication(tokenContent: JwtTokenContent, request: HttpServletRequest): Authentication? =
					UsernamePasswordAuthenticationToken("user@test.com", null, listOf(SimpleGrantedAuthority("USER")))
		}
	}

	@MockBean
	private lateinit var service: TaskService
	@MockBean
	private lateinit var taskRepository: TaskRepository
	@MockBean
	private lateinit var userRepository: UserRepository
	@MockBean
	private lateinit var courseRepository: CourseRepository
	@MockBean
	private lateinit var deadlineRepository: DeadlineRepository

	private final val testUser = User.of(email = "user@test.com", password = "password").apply { id = 0 }
	private final val course = Course.of(owner = testUser).apply { id = 0 }
	private final val storedTask = Task.of(owner = testUser).apply { id = 0 }

	@BeforeEach
	fun setUp() {
		Mockito.`when`(userRepository.findFirstByEmail(testUser.email)).thenReturn(testUser)

		Mockito.`when`(courseRepository.findByIdIfOwned(0, 0)).thenReturn(course)
		Mockito.`when`(courseRepository.findByIdIfOwned(0, 1)).thenReturn(null)
		Mockito.`when`(courseRepository.findById(0)).thenReturn(Optional.of(course))


		Mockito.`when`(taskRepository.findById(0)).thenReturn(Optional.of(storedTask))
		Mockito.`when`(taskRepository.findAllFiltered(Mockito.eq<String>(testUser.email) ?: testUser.email, Mockito.any(), Mockito.any()))
				.thenReturn(listOf(storedTask))

		Mockito.`when`(service.save(Mockito.any<UploadableTask>() ?: UploadableTask.of(ownerEmail = ""))).then {
			return@then Task.of(owner = testUser)
		}

		Mockito.`when`(deadlineRepository.findByIdOrNull(0)).thenReturn(null)
	}


	@Test
	fun contextLoads() {}

	@Test
	fun getTasks_noArguments_success() {
		mvc.perform(get("/tasks")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpectTask("$[0]", storedTask)

		Mockito.verify(taskRepository).findAllFiltered(testUser.email, null, null)
	}

	@Test
	fun getTasks_dateFilter_success() {
		val params: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
			add("from", dateOf().toISOString().replace(' ', 'T'))
		}
		mvc.perform(get("/tasks")
				.params(params)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpectTask("$[0]", storedTask)

		Mockito.verify(taskRepository).findAllFiltered(testUser.email, dateOf(), null)
	}

	@Test
	fun getTasks_courseFilter_success() {
		val params: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
			add("course", "0")
		}
		mvc.perform(get("/tasks")
				.params(params)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpectTask("$[0]", storedTask)

		Mockito.verify(taskRepository).findAllFiltered(testUser.email, null, 0)
	}

	@Test
	fun getTasks_dateAndCourseFilter_success() {
		val params: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
			add("from", dateOf().toISOString().replace(' ', 'T'))
			add("course", "0")
		}
		mvc.perform(get("/tasks")
				.params(params)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpectTask("$[0]", storedTask)

		Mockito.verify(taskRepository).findAllFiltered(testUser.email, dateOf(), 0)
	}

	@Test
	fun getTasks_invalidDate_badRequest() {
		val params: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
			add("from", "invalid date")
		}
		mvc.perform(get("/tasks")
				.params(params)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest)
	}
	@Test
	fun getTasks_invalidCourse_badRequest() {
		val params: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
			add("course", "invalid course")
		}
		mvc.perform(get("/tasks")
				.params(params)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest)
	}

	@Test
	fun createTasks_simpleTask_success() {
		val uploadableTask = UploadableTask.of(ownerEmail = testUser.email)
		mvc.perform(post("/tasks")
				.body(uploadableTask)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))

		val saved = ArgumentCaptor.forClass(UploadableTask::class.java)
		Mockito.verify(service).save(saved.capture() ?: uploadableTask)
		assertThat(saved.value.name).isEqualTo(uploadableTask.name)
		assertThat(saved.value.ownerEmail).isEqualTo(uploadableTask.ownerEmail)
	}

	@Test
	fun createTasks_invalidTask_badRequest() {
		mvc.perform(post("/tasks")
				.body("Invalid content")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest)
	}


	fun ResultActions.andExpectTask(pathRoot: String = "", task: Task): ResultActions {
		return apply {
			andExpect(jsonPath("$pathRoot.id", `is`(task.id?.toInt())))
			andExpect(jsonPath("$pathRoot.name", `is`(task.name)))
			andExpect(jsonPath("$pathRoot.description", `is`(task.description)))
			andExpect(jsonPath("$pathRoot.priority", `is`(task.priority.toString())))
			andExpect(jsonPath("$pathRoot.startDate", `is`(task.startDate.toISOString())))
			andExpect(jsonPath("$pathRoot.endDate", `is`(task.endDate.toISOString())))
			andExpect(jsonPath("$pathRoot.owner.id", `is`(task.owner.id?.toInt())))
			if (task.course != null)
				andExpect(jsonPath("$pathRoot.course.id", `is`(task.course?.id?.toInt())))
			else
				andExpect(jsonPath("$pathRoot.course", equalTo(null)))
		}
	}

	fun MockHttpServletRequestBuilder.body(body: Any): MockHttpServletRequestBuilder {
		return apply {
			content(ObjectMapper().writeValueAsString(body))
		}
	}
}