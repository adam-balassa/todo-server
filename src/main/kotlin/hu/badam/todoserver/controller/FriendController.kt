package hu.badam.todoserver.controller

import hu.badam.todoserver.model.Task
import hu.badam.todoserver.model.UserFriends
import hu.badam.todoserver.repository.FriendRepository
import hu.badam.todoserver.service.UserFriendsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.*

@Controller
class FriendController(private var friendRepository: FriendRepository,
                       private var friendsService: UserFriendsService) : ControllerBase() {


    @GetMapping("/friends")
    @ResponseBody
    fun getFriends(auth: Authentication): UserFriends {
        return friendRepository.findByUser_Email(auth.email) ?: throw IllegalArgumentException("Invalid user id")
    }

    @PostMapping("/friends/{email}")
    @ResponseBody
    fun sendFriendRequest(@PathVariable("email") to: String, auth: Authentication): String {
        friendsService.sendFriendRequest(auth.email, to)
        return "Request sent successfully"
    }

    @PatchMapping("/friends/{email}")
    @ResponseBody
    fun acceptRequest(@PathVariable("email") to: String, auth: Authentication): String {
        friendsService.acceptFriendRequest(auth.email, to)
        return "Request accepted successfully"
    }

    @DeleteMapping("/friends/{email}")
    @ResponseBody
    fun declineRequest(@PathVariable("email") to: String, auth: Authentication): String {
        friendsService.declineFriendRequest(auth.email, to)
        return "Request declined successfully"
    }

    @GetMapping("/friends/{email}/tasks")
    @ResponseBody
    fun getFriendsTasks(@PathVariable("email") friend: String,
                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") from: Date?,
                        auth: Authentication): List<Task> {
        return friendsService.getTasks(auth.email, friend, from)
    }
}