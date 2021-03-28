package org.taymyr.play.repository.infrastructure.persistence

import org.taymyr.play.repository.domain.Order
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "ORDERS")
data class OrderImpl(
    @Id override val id: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    override val product: ProductImpl,

    override val volume: Int?
) : Order
