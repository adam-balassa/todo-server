package hu.badam.todoserver.model

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "deadlines")
class Deadline {
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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    lateinit var deadLine: Date

    @ManyToMany (fetch = FetchType.EAGER)
    @JoinTable(
            name = "deadline_user",
            joinColumns = [JoinColumn(name = "deadline_id", nullable = false)],
            inverseJoinColumns = [JoinColumn(name = "user_id", nullable = false)])
    lateinit var assigned: Set<User>

    @OneToMany(mappedBy = "deadline")
    var tasks: Set<Task> = emptySet()

    @ManyToOne
    @JoinColumn(name = "course_id")
    var course: Course? = null
}