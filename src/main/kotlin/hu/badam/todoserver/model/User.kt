package hu.badam.todoserver.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.crypto.password.PasswordEncoder
import javax.persistence.*

@Entity
@Table(name = "users")
class User () {
    companion object

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false, unique = true)
    lateinit var email: String

    @JsonIgnore
    @Column(nullable = false)
    lateinit var password: String

    fun encodePassword(passwordEncoder: PasswordEncoder) {
        this.password = passwordEncoder.encode(this.password)
    }
}

@Entity
@Table(name = "user_authorities")
class UserAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @Enumerated(value = EnumType.STRING)
    lateinit var authority: Authority

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    lateinit var user: User

    enum class Authority {
        USER, ADMIN
    }
}

@Entity
@Table(name = "user_friends")
class UserFriends {
    companion object;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    lateinit var user: User

    @ManyToMany (fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_user_friends",
            joinColumns = [JoinColumn(name = "user_id", nullable = false)],
            inverseJoinColumns = [JoinColumn(name = "friend_id", nullable = false)])
    lateinit var friends: MutableSet<User>

    @ManyToMany (fetch = FetchType.EAGER)
    @JoinTable(
            name = "friend_requests",
            joinColumns = [JoinColumn(name = "user_id", nullable = false)],
            inverseJoinColumns = [JoinColumn(name = "requester_id", nullable = false)])
    lateinit var friendRequests: MutableSet<User>
}