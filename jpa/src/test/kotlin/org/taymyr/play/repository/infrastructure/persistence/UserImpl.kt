package org.taymyr.play.repository.infrastructure.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.taymyr.play.repository.domain.User

@Entity
@Table(name = "USER")
data class UserImpl(
    @Id override val id: String,
    override val fullname: String,
    override val email: String,
) : User
