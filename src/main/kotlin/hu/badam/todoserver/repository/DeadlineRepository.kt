package hu.badam.todoserver.repository

import hu.badam.todoserver.model.Deadline
import org.springframework.data.jpa.repository.JpaRepository

interface DeadlineRepository: JpaRepository<Deadline, Long>