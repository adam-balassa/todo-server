package hu.badam.todoserver.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonTypeId
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.util.*
import javax.persistence.*
import kotlin.properties.Delegates

@Entity
@Table(name = "courses")
class Course {
    companion object

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @Column(nullable = false)
    lateinit var name: String

    @OneToMany(mappedBy = "course", fetch = FetchType.EAGER)
    var tasks: Set<Task>? = emptySet()

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var owner: User
}

class UploadableCourse {
    lateinit var name: String
    lateinit var ownerEmail: String
}

class UpdatableCourse {
    var id by Delegates.notNull<Long>()
    lateinit var name: String
}