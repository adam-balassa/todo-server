package hu.badam.todoserver.controller

import hu.badam.todoserver.model.Course
import hu.badam.todoserver.model.UploadableCourse
import hu.badam.todoserver.model.UserFriends
import hu.badam.todoserver.repository.FriendRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class FriendController: ControllerBase() {

    @Autowired
    private lateinit var friendRepository: FriendRepository

    @GetMapping("/friends")
    @ResponseBody
    fun getFriends(auth: Authentication): UserFriends {
        return friendRepository.findByUser_Email(auth.email) ?: throw IllegalArgumentException("Invalid user id")
    }
}