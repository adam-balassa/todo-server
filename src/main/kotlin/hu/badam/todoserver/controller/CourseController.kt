package hu.badam.todoserver.controller

import hu.badam.todoserver.model.*
import hu.badam.todoserver.repository.CourseRepository
import hu.badam.todoserver.service.CourseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class CourseController (private val courseRepository: CourseRepository,
                        private val courseService: CourseService): ControllerBase() {


    @GetMapping("/courses")
    @ResponseBody
    fun getCourses(auth: Authentication): List<Course> {
        return courseRepository.findCourses(auth.email).also {
            for (course in it) for (task in course.tasks ?: emptySet())
                task.course = null
        }
    }

    @PostMapping("/courses")
    fun createCourse(@RequestBody uploadableCourse: UploadableCourse, auth: Authentication): ResponseEntity<Course> {
        uploadableCourse.ownerEmail = auth.email
        val course = courseRepository.save(uploadableCourse)
        return ResponseEntity(course, HttpStatus.CREATED)
    }

    @PatchMapping("/courses/{courseId}")
    @ResponseBody
    fun updateCourse(@RequestBody updatableCourse: UpdatableCourse,
                   @PathVariable("courseId") courseId: Long,
                   auth: Authentication): Course {
        updatableCourse.id = courseId
        return courseRepository.update(updatableCourse, auth.email)
    }

    @DeleteMapping("/courses/{courseId}")
    @ResponseBody
    fun deleteTask(@PathVariable("courseId") id: Long,
                   @RequestParam("deleteTasks") deleteTasks: Boolean?,
                   auth: Authentication): String {
        courseService.deleteCourse(id, deleteTasks == true, auth.email)
        return "Course (id: $id) deleted successfully"
    }
}