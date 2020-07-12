package hu.badam.todoserver.controller

import org.springframework.security.core.Authentication
import java.lang.IllegalStateException

open class ControllerBase {
    protected val Authentication.email: String
        get() = principal as? String ?: throw IllegalStateException("Authentication principal should be an email address")
}