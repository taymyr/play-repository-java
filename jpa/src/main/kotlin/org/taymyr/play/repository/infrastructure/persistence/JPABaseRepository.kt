package org.taymyr.play.repository.infrastructure.persistence

import play.db.jpa.JPAApi
import java.io.Serializable
import javax.persistence.EntityManager

abstract class JPABaseRepository<Aggregate : Any, Identity : Serializable> @JvmOverloads constructor(
    protected val jpaApi: JPAApi,
    protected val executionContext: DatabaseExecutionContext,
    protected val clazz: Class<out Aggregate>,
    protected val persistenceUnitName: String = "default"
) {
    protected fun <E> transaction(function: (EntityManager) -> E): E = jpaApi.withTransaction(persistenceUnitName, function)

    protected fun <E> readOnly(function: (EntityManager) -> E): E = jpaApi.withTransaction(persistenceUnitName, true, function)
}
