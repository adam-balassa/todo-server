package hu.badam.todoserver.model

import com.fasterxml.jackson.annotation.*
import java.util.*
import javax.persistence.*
import kotlin.properties.Delegates

@Entity
@Table(name = "tasks")
class Task {
    companion object

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @Column(nullable = false)
    lateinit var name: String

    var description: String? = null

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    lateinit var priority: Priority

    @Column(nullable = false)
    var done: Boolean = false

    @JsonFormat(pattern="yyyy-MM-dd hh:mm:ss", timezone = "Europe/Budapest")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    lateinit var startDate: Date

    @JsonFormat(pattern="yyyy-MM-dd hh:mm:ss", timezone = "Europe/Budapest")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    lateinit var endDate: Date

    @ManyToMany (fetch = FetchType.EAGER)
    @JoinTable(
            name = "task_assigned",
            joinColumns = [JoinColumn(name = "task_id", nullable = false)],
            inverseJoinColumns = [JoinColumn(name = "user_id", nullable = false)])
    var assigned: Set<User> = emptySet()

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    lateinit var owner: User

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "deadline_id", nullable = true)
    @JsonIgnore
    var deadline: Deadline? = null

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    var course: Course? = null
}

class UploadableTask {
    companion object;

    lateinit var name: String
    var description: String? = null
    var priority: Priority = Priority.NORMAL
    var done by Delegates.notNull<Boolean>()
    lateinit var startDate: Date
    lateinit var endDate: Date
    var assignedUserEmails: Set<String>? = null
    lateinit var ownerEmail: String
    var deadlineId: Long? = null
    var courseId: Long? = null
}

class UpdatableTask {
    companion object

    var id by Delegates.notNull<Long>()
    var name: String? = null
    var description: String? = null
    var done: Boolean? = null
    var priority: Priority? = null
    var startDate: Date? = null
    var endDate: Date? = null
    var assignedEmails: Set<String>? = null
    /**
     * If deadline id is -1 than it shall not be updated
     * If deadline id is null, than the user requested deleting the association between task and deadline
     */
    var deadlineId: Long? = -1

    /**
     * If course id is -1 than it shall not be updated
     * If course id is null, than the user requested deleting the association between course and deadline
     */
    var courseId: Long? = -1
}