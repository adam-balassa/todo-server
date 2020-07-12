package hu.badam.todoserver

import hu.badam.todoserver.model.Course
import hu.badam.todoserver.security.JwtSecurityConfiguration
import hu.badam.todoserver.security.TokenAuthenticationService
import hu.badam.todoserver.service.CourseService
import hu.badam.todoserver.service.TaskService
import hu.badam.todoserver.service.UserService
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableAutoConfiguration
@EnableWebMvc
@WebAppConfiguration
@ComponentScan(basePackages = ["hu.badam.todoserver.controller"])
@Import(ErrorHandler::class, JwtSecurityConfiguration::class)
class TodoServerApplication {

	@Bean
	fun getUserService(): UserService = UserService()

	@Bean
	fun getTaskService(): TaskService = TaskService()

	@Bean
	fun getCourseService(): CourseService = CourseService()

	@Bean
	fun getTokenAuthenticationService(): TokenAuthenticationService = TokenAuthenticationService()
}


fun main(args: Array<String>) {
	runApplication<TodoServerApplication>(*args)
}
