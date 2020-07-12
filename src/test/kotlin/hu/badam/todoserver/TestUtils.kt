package hu.badam.todoserver

import hu.badam.todoserver.model.*
import org.assertj.core.api.Assertions.assertThat
import java.text.SimpleDateFormat
import java.util.*

fun Task.Companion.of(name: String = "Test name",
                      description: String? = "Test description",
                      priority: Priority = Priority.NORMAL,
                      done: Boolean = false,
                      startDate: Date = dateOf(),
                      endDate: Date = dateOf(hour = 13),
                      owner: User,
                      assigned: Set<User>? = null,
                      course: Course? = null
) = Task().also {
    it.name = name
    it.description = description
    it.done = done
    it.startDate = startDate
    it.endDate = endDate
    it.owner = owner
    it.assigned = assigned ?: setOf(owner)
    it.priority = priority
    it.course = course
}

fun UploadableTask.Companion.of(name: String = "Test name",
                                description: String? = "Test description",
                                priority: Priority = Priority.NORMAL,
                                done: Boolean = false,
                                startDate: Date = dateOf(),
                                endDate: Date = dateOf(hour = 13),
                                ownerEmail: String,
                                assigned: Set<String>? = null,
                                courseId: Long? = null
) = UploadableTask().also {
    it.name = name
    it.description = description
    it.done = done
    it.startDate = startDate
    it.endDate = endDate
    it.ownerEmail = ownerEmail
    it.assignedUserEmails = assigned
    it.priority = priority
    it.courseId = courseId
}

fun UpdatableTask.Companion.of(
        id: Long,
        name: String? = null,
        description: String? = null,
        priority: Priority? = null,
        done: Boolean? = null,
        startDate: Date? = null,
        endDate: Date? = null,
        assigned: Set<String>? = null,
        courseId: Long? = null
) = UpdatableTask().also {
    it.id = id
    it.name = name
    it.description = description
    it.done = done
    it.startDate = startDate
    it.endDate = endDate
    it.assignedEmails = assigned
    it.priority = priority
    it.courseId = courseId
}

fun User.Companion.of(name: String = "Test User",
                      email: String = "test.user@test.com",
                      password: String = "password"
) = User().also {
    it.name = name
    it.email = email
    it.password = password
}

fun Course.Companion.of(name: String = "Test course",
                        owner: User
) = Course().also {
    it.name = name
    it.owner = owner
}

fun dateOf(month: Int = 7, date: Int = 6, hour: Int = 12, minute: Int = 0) = Calendar.getInstance().apply {
    set(2020, month, date, hour, minute, 0)
    set(Calendar.MILLISECOND, 0)
}.time


fun assertTaskEquals(task1: Task, task2: Task) {
    assertThat(task1.name).isEqualTo(task2.name)
    assertThat(task1.description).isEqualTo(task2.description)
    assertThat(task1.startDate).isEqualTo(task2.startDate)
    assertThat(task1.endDate).isEqualTo(task2.endDate)
    assertThat(task1.priority).isEqualTo(task2.priority)
    assertThat(task1.done).isEqualTo(task2.done)
    assertThat(task1.owner.id).isEqualTo(task2.owner.id)
    assertThat(task1.assigned.map { it.id }).containsExactlyInAnyOrderElementsOf(task2.assigned.map { it.id })
//    assertThat(task1.assigned).allMatch { assigned1 ->
//        task2.assigned.any { assigned1.id == it.id }
//    }
    assertThat(task1.course?.id).isEqualTo(task2.course?.id)
}

fun Date.toISOString(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    return formatter.format(this)
}