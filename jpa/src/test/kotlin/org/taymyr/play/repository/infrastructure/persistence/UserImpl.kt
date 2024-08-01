package org.taymyr.play.repository.infrastructure.persistence

import org.taymyr.play.repository.domain.User
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "USER")
data class UserImpl(
    @Id override val id: String,
    override val fullname: String,
    override val email: String
) : User
