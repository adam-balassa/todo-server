package hu.badam.todoserver

import hu.badam.todoserver.repository.*
import hu.badam.todoserver.security.JwtSecurityConfiguration
import hu.badam.todoserver.security.TokenAuthenticationService
import hu.badam.todoserver.service.CourseService
import hu.badam.todoserver.service.TaskService
import hu.badam.todoserver.service.UserFriendsService
import hu.badam.todoserver.service.UserService
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.util.*
import javax.annotation.PostConstruct


@SpringBootApplication
@EnableAutoConfiguration
@EnableWebMvc
@WebAppConfiguration
@ComponentScan(basePackages = ["hu.badam.todoserver.controller"])
@Import(ErrorHandler::class, JwtSecurityConfiguration::class)
class TodoServerApplication {

    @Bean
    fun getUserService(userRepository: UserRepository): UserService = UserService(userRepository)

    @Bean
    fun getTaskService(courseRepository: CourseRepository,
                       taskRepository: TaskRepository,
                       deadlineRepository: DeadlineRepository,
                       userRepository: UserRepository): TaskService
            = TaskService(taskRepository, courseRepository, deadlineRepository, userRepository)

    @Bean
    fun getCourseService(courseRepository: CourseRepository, taskRepository: TaskRepository): CourseService
            = CourseService(courseRepository, taskRepository)

    @Bean
    fun getUserFriendsService(taskRepository: TaskRepository,
                              userFriendRepository: FriendRepository,
                              userRepository: UserRepository): UserFriendsService
            = UserFriendsService(userRepository, userFriendRepository, taskRepository)

    @Bean
    fun getTokenAuthenticationService(): TokenAuthenticationService = TokenAuthenticationService()

    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Budapest"))
    }
}


fun main(args: Array<String>) {
    runApplication<TodoServerApplication>(*args)
}
