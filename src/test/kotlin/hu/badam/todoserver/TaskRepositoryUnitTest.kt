package hu.badam.todoserver

import hu.badam.todoserver.model.Course
import hu.badam.todoserver.model.Task
import hu.badam.todoserver.model.User
import hu.badam.todoserver.repository.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import javax.transaction.Transactional

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureDataJpa
@Transactional
    class TaskRepositoryUnitTest {
        @Autowired
        private lateinit var em: TestEntityManager
        @Autowired
        private lateinit var taskRepository: TaskRepository

    @Test
    fun testContextLoads() {}

    @Test
    fun findAllFiltered_noFilter_success() {
        // given
        val user = User.of().let { em.persist(it) }

        var task = Task.of(owner = user)
        task = em.persist(task)

        em.flush()

        //when
        val tasks = taskRepository.findAllFiltered(user.email, null, null)

        //then
        assertThat(tasks).containsExactly(task)
    }

    @Test
    fun findAllFiltered_noFilterMultipleTasks_success() {
        // given
        val user = User.of().let { em.persist(it) }

        val testTasks = mutableListOf(Task.of(owner = user), Task.of(owner = user))
        testTasks.forEachIndexed { i, task -> testTasks[i] = em.persist(task) }

        em.flush()

        //when
        val tasks = taskRepository.findAllFiltered(user.email, null, null)

        //then
        assertThat(tasks).containsExactlyInAnyOrder(testTasks[0], testTasks[1])
    }

    @Test
    fun findAllFiltered_filterDate_success() {
        // given
        val user = User.of().let { em.persist(it) }

        var task1 = Task.of(owner = user, startDate = dateOf(date = 1), endDate = dateOf(date = 2))
        var task2 = Task.of(owner = user, startDate = dateOf(date = 1), endDate = dateOf(date = 4))
        task1 = em.persist(task1)
        task2 = em.persist(task2)
        em.flush()

        //when
        val tasks = taskRepository.findAllFiltered(user.email, dateOf(date = 3), null)

        //then
        assertThat(tasks).containsExactly(task2)
    }

    @Test
    fun findAllFiltered_filterDate2_success() {
        // given
        val user = User.of().let { em.persist(it) }

        var task1 = Task.of(owner = user, startDate = dateOf(date = 1), endDate = dateOf(date = 2))
        var task2 = Task.of(owner = user, startDate = dateOf(date = 1), endDate = dateOf(date = 4))
        task1 = em.persist(task1)
        task2 = em.persist(task2)
        em.flush()

        //when
        val tasks = taskRepository.findAllFiltered(user.email, dateOf(date = 4), null)

        //then
        assertThat(tasks).containsExactly(task2)
    }

    @Test
    fun findAllFiltered_filterCourse_success() {
        // given
        val user = User.of().let { em.persist(it) }

        var course = Course.of(owner = user)
        course = em.persist(course)

        var task1 = Task.of(owner = user, course = course)
        var task2 = Task.of(owner = user, course = null)
        task1 = em.persist(task1)
        task2 = em.persist(task2)

        em.flush()

        //when
        val tasks = taskRepository.findAllFiltered(user.email, null, course.id)

        //then
        assertThat(tasks).containsExactly(task1)
    }

    @Test
    fun findAllFiltered_filterCourse2_success() {
        // given
        val user = User.of().let { em.persist(it) }

        var course1 = Course.of(owner = user)
        course1 = em.persist(course1)

        var course2 = Course.of(owner = user)
        course2 = em.persist(course2)

        var task1 = Task.of(owner = user, course = course1)
        var task2 = Task.of(owner = user, course = course2)
        task1 = em.persist(task1)
        task2 = em.persist(task2)

        em.flush()

        //when
        val tasks = taskRepository.findAllFiltered(user.email, null, course1.id)

        //then
        assertThat(tasks).containsExactly(task1)
    }

    @Test
    fun findAllFiltered_filterCourseAndDate_success() {
        // given
        val user = User.of().let { em.persist(it) }
        val course = Course.of(owner = user).let { em.persist(it) }
        val task1 = Task.of(owner = user, course = course, startDate = dateOf(date = 1), endDate = dateOf(date = 1))
                .let { em.persist(it) }
        val task2 = Task.of(owner = user, course = null, startDate = dateOf(date = 1), endDate = dateOf(date = 2))
                .let { em.persist(it) }
        em.flush()

        //when
        val tasks = taskRepository.findAllFiltered(user.email, dateOf(date = 2), course.id)

        //then
        assertThat(tasks).isEmpty()
    }

    @Test
    fun findAllFilter_userDoesNotOwnTask_nothingReturned() {
        // given

        val userA = User.of(email = "a@test.com").let { em.persist(it) }
        val userB = User.of(email = "b@test.com").let { em.persist(it) }

        val task = Task.of(owner = userA).let { em.persist(it) }

        em.flush()

        //when
        val tasks = taskRepository.findAllFiltered(userB.email, null, null)

        //then
        assertThat(tasks).isEmpty()
    }

    @Test
    fun findAllFilter_userDoesNotOwnTaskButIsAssigned_success() {
        // given
        val userA = User.of(email = "a@test.com").let { em.persist(it) }
        val userB = User.of(email = "b@test.com").let { em.persist(it) }

        val task = Task.of(owner = userA, assigned = setOf(userB)).let { em.persist(it) }

        em.flush()

        //when
        val tasks = taskRepository.findAllFiltered(userB.email, null, null)

        //then
        assertThat(tasks).containsExactly(task)
    }

    @Test
    fun findAllFilter_invalidUser_nothingReturned() {
        // given
        val user = User.of().let { em.persist(it) }
        val task = Task.of(owner = user).let { em.persist(it) }

        em.flush()

        //when
        val tasks = taskRepository.findAllFiltered("invalid user", null, null)

        //then
        assertThat(tasks).isEmpty()
    }
}