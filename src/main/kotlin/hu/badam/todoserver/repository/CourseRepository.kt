package hu.badam.todoserver.repository

import hu.badam.todoserver.model.Course
import hu.badam.todoserver.model.UpdatableCourse
import hu.badam.todoserver.model.UploadableCourse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface CourseRepository : CourseRepositoryCustom, JpaRepository<Course, Long>  {
    @Query("select c from Course c join fetch c.owner where c.owner.email = :email")
    fun findCourses(@Param("email") email: String): List<Course>

    @Query("select c from Course c join fetch c.owner where c.owner.id = :userId and c.id = :courseId")
    fun findByIdIfOwned(@Param("courseId") courseId: Long, @Param("userId") userId: Long?): Course?
}

interface CourseRepositoryCustom {
    fun save(uploadableCourse: UploadableCourse): Course
    fun update(updatableCourse: UpdatableCourse, email: String): Course
}

open class CourseRepositoryImpl: CourseRepositoryCustom {
    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Transactional
    override fun save(uploadableCourse: UploadableCourse): Course {
        val course = Course().apply {
            name = uploadableCourse.name
            owner = userRepository.findFirstByEmail(uploadableCourse.ownerEmail) ?: throw IllegalArgumentException("Invalid user id")
        }
        return courseRepository.save(course)
    }

    @Transactional
    @Modifying
    override fun update(updatableCourse: UpdatableCourse, email: String): Course {
        val course = courseRepository.findByIdOrNull(updatableCourse.id) ?: throw IllegalArgumentException("Invalid course id")
        if (course.owner.email != email) throw IllegalAccessException("User is not permitted to update this course")
        course.name = updatableCourse.name
        return courseRepository.save(course)
    }
}
