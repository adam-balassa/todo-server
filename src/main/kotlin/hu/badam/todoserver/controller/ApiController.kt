package hu.badam.todoserver.controller

import hu.badam.todoserver.model.User
import hu.badam.todoserver.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class ApiController (private val passwordEncoder: PasswordEncoder,
                     private val userService: UserService) {

    @PostMapping(value = ["/api/register"])
    @ResponseBody
    fun register(@RequestBody userData: User): ResponseEntity<String> {
        userData.encodePassword(passwordEncoder)
        userService.registerUser(userData)
        return ResponseEntity("User successfully created", HttpStatus.CREATED)
    }
}