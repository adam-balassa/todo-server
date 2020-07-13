package hu.badam.todoserver.controller

import hu.badam.todoserver.model.Task
import hu.badam.todoserver.model.UpdatableTask
import hu.badam.todoserver.model.UploadableTask
import hu.badam.todoserver.repository.TaskRepository
import hu.badam.todoserver.service.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.*

@Controller
class TaskController: ControllerBase() {
    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var taskService: TaskService

    @GetMapping("/tasks")
    @ResponseBody
    fun getTasks (@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") from: Date?,
                  @RequestParam course: Long?,
                  auth: Authentication): List<Task> {
        return taskRepository.findAllFiltered(auth.email, from, course)
    }

    @PostMapping("/tasks")
    fun createTask(@RequestBody uploadableTask: UploadableTask, auth: Authentication): ResponseEntity<Task> {
        uploadableTask.ownerEmail = auth.email
        val task = taskService.save(uploadableTask).apply {
            course?.let { it.tasks = null }
        }
        return ResponseEntity(task, HttpStatus.CREATED)
    }

    @PatchMapping("/tasks/{taskId}")
    @ResponseBody
    fun updateTask(@RequestBody updatableTask: UpdatableTask,
                   @PathVariable("taskId") taskId: Long,
                   auth: Authentication): Task {
        updatableTask.id = taskId
        return taskService.update(updatableTask, auth.email).apply {
            course?.let {
                it.tasks = null
            }
        }
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseBody
    fun deleteTask(@PathVariable("taskId") id: Long, auth: Authentication): String {
        val task = taskRepository.findByIdOrNull(id) ?: throw IllegalArgumentException("Invalid task id")
        if (task.owner.email != auth.email) throw IllegalAccessException("User is not permitted to delete this task")
        taskRepository.deleteById(id)
        return "Task (id: $id) successfully deleted"
    }
}