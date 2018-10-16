package org.taymyr.play.repository.infrastructure.persistence

import org.taymyr.play.repository.domain.User
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "USER")
data class UserImpl(
    @Id override val id: String,
    override val fullname: String,
    override val email: String
) : User