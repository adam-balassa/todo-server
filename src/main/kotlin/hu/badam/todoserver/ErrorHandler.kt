package hu.badam.todoserver


import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class ErrorHandler {
    private val logger: Logger = LogManager.getLogger(ErrorHandler::class)

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequests(exception: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
                ErrorResponse("Bad request", exception.localizedMessage ?: "Illegal arguments provided"),
                HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalAccessException::class)
    fun illegalAccess(exception: IllegalAccessException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
                ErrorResponse("Not allowed", exception.message ?: "The requested method is not allowed"),
                HttpStatus.FORBIDDEN)
    }
}

data class ErrorResponse (
        val error: String,
        val reason: String
)