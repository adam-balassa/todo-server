package hu.badam.todoserver.service

import hu.badam.todoserver.model.Task
import hu.badam.todoserver.model.UpdatableTask
import hu.badam.todoserver.model.UploadableTask
import hu.badam.todoserver.repository.CourseRepository
import hu.badam.todoserver.repository.DeadlineRepository
import hu.badam.todoserver.repository.TaskRepository
import hu.badam.todoserver.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService {
    @Autowired
    private lateinit var taskRepository: TaskRepository
    @Autowired
    private lateinit var courseRepository: CourseRepository
    @Autowired
    private lateinit var deadlineRepository: DeadlineRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    @Transactional
    fun save(uploadableTask: UploadableTask): Task {
        require(uploadableTask.name.length > 3) { "Name must be at least 3 characters long" }
        require(uploadableTask.endDate > uploadableTask.startDate) { "End date must come after start date" }

        val task = Task().apply {
            name = uploadableTask.name
            description = uploadableTask.description
            startDate = uploadableTask.startDate
            endDate = uploadableTask.endDate
            priority = uploadableTask.priority

            owner = userRepository.findFirstByEmail(uploadableTask.ownerEmail) ?: throw IllegalArgumentException("Invalid user id")

            assigned = userRepository.findByEmailIn(uploadableTask.assignedUserEmails ?: setOf(uploadableTask.ownerEmail))

            course = uploadableTask.courseId?.let {
                courseRepository.findByIdIfOwned(it, owner.id) ?: throw IllegalArgumentException("Invalid course id")
            }
            deadline = uploadableTask.deadlineId?.let { deadlineRepository.findByIdOrNull(it) }
        }

        return taskRepository.save(task)
    }

    @Transactional
    @Modifying
    fun update(updatableTask: UpdatableTask, email: String): Task {
        require(updatableTask.name?.let { it.length > 3 } ?: true) { "Name must be at least 3 characters long" }

        val task = taskRepository.findByIdOrNull(updatableTask.id) ?: throw IllegalArgumentException("Invalid task id")
        if (task.owner.email != email) throw IllegalAccessException("The task cannot be modified by user")
        task.apply {
            updatableTask.name?.let { name = it }
            updatableTask.description?.let { description = it }
            updatableTask.priority?.let { priority = it }
            updatableTask.startDate?.let { startDate = it }
            updatableTask.endDate?.let { endDate = it }

            if (startDate > endDate) throw IllegalArgumentException("Task's start date cannot be greater than its end date")

            updatableTask.assignedEmails?.let {
                assigned = userRepository.findByEmailIn(it)
            }
            if (updatableTask.deadlineId != -1L) {
                deadline = updatableTask.deadlineId?.let {
                    deadlineRepository.findByIdOrNull(it) ?: throw IllegalArgumentException("Invalid deadline id")
                }
            }
            if (updatableTask.courseId != -1L) {
                course = updatableTask.courseId?.let {
                    courseRepository.findByIdOrNull(it) ?: throw IllegalArgumentException("Invalid course id")
                }
            }
        }
        return taskRepository.save(task)
    }
}