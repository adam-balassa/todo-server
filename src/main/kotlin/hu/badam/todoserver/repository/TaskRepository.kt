package hu.badam.todoserver.repository

import hu.badam.todoserver.model.Course
import hu.badam.todoserver.model.Task
import hu.badam.todoserver.model.User
import hu.badam.todoserver.util.criteriaQuery
import hu.badam.todoserver.util.ifNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

interface TaskRepository: JpaRepository<Task, Long>, TaskRepositoryCustom {

    @Transactional
    @Modifying
    @Query("update Task t set t.course = null where t.course.id = :courseId")
    fun removeAllFromCourse(@Param("courseId") courseId: Long)
    fun deleteByCourse_id(courseId: Long)
}

interface TaskRepositoryCustom {
    fun findAllFiltered (email: String, from: Date?, course: Long?) : List<Task>
}

open class TaskRepositoryImpl: TaskRepositoryCustom {
    @Autowired
    private lateinit var userRepository: UserRepository
    @PersistenceContext
    private lateinit var em: EntityManager

    override fun findAllFiltered(email: String, from: Date?, course: Long?): List<Task> {
        val user = userRepository.findFirstByEmail(email) ?: return emptyList()

        val tasks: List<Task> = criteriaQuery(em) { task, cb, cr ->
            select(task)
            val assigned = task.join<Task, Set<User>>("assigned")
            val userPredicate = cb.or(
                    cb.equal(assigned.get<Long>("id"), user.id),
                    cb.equal(task.get<User>("owner").get<Long>("id"), user.id)
            )
            val fromPredicate = ifNotNull(from, cb) { cb.greaterThanOrEqualTo(task.get("endDate"), from) }
            val coursePredicate = ifNotNull(course, cb) { cb.equal(task.get<Course>("course").get<Long>("id"), course) }
            where(userPredicate, fromPredicate, coursePredicate)
        }
        return tasks.also {
            for (task in tasks)
                task.course?.let { it.tasks = null }
        }
    }

}