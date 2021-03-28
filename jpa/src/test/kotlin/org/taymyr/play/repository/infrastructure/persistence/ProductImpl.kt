package org.taymyr.play.repository.infrastructure.persistence

import org.taymyr.play.repository.domain.Product
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "PRODUCTS")
data class ProductImpl(
    @Id override val id: String,
    override val name: String,
    override val volume: Int
) : Product
