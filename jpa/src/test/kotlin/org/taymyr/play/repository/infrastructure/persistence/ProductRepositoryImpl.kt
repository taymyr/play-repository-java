package org.taymyr.play.repository.infrastructure.persistence

import org.taymyr.play.repository.domain.Product
import org.taymyr.play.repository.domain.ProductRepository
import play.db.jpa.JPAApi
import java.util.UUID
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    jpaApi: JPAApi,
    executionContext: DatabaseExecutionContext
) : JPARepository<Product, String>(jpaApi, executionContext, ProductImpl::class.java), ProductRepository {

    override fun nextIdentity(): String = UUID.randomUUID().toString()
}
