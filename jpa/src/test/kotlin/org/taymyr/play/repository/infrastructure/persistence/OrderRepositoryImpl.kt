package org.taymyr.play.repository.infrastructure.persistence

import org.taymyr.play.repository.domain.Order
import org.taymyr.play.repository.domain.OrderRepository
import play.db.jpa.JPAApi
import java.util.UUID
import javax.inject.Inject

class OrderRepositoryImpl@Inject constructor(
    jpaApi: JPAApi,
    executionContext: DatabaseExecutionContext
) : JPARepository<Order, String>(jpaApi, executionContext, OrderImpl::class.java), OrderRepository {

    override fun nextIdentity(): String = UUID.randomUUID().toString()
}
