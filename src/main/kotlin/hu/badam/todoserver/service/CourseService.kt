package hu.badam.todoserver.service

import hu.badam.todoserver.repository.CourseRepository
import hu.badam.todoserver.repository.TaskRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseService {
    @Autowired
    private lateinit var courseRepository: CourseRepository
    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Modifying
    @Transactional
    fun deleteCourse(courseId: Long, deleteTasks: Boolean, email: String) {
        val course = courseRepository.findByIdOrNull(courseId) ?: throw IllegalArgumentException("Invalid course id")
        if (course.owner.email != email) throw IllegalAccessException("User is not permitted to delete this course")
        if (deleteTasks)
            taskRepository.deleteByCourse_id(courseId)
        else
            taskRepository.removeAllFromCourse(courseId)
        courseRepository.deleteById(courseId)
    }
}