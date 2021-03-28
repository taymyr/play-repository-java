package org.taymyr.play.repository.infrastructure.persistence

import akka.Done
import org.hibernate.Session
import org.taymyr.play.repository.domain.Transaction
import org.taymyr.play.repository.domain.TransactionalRepository
import play.db.jpa.JPAApi
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.util.Optional
import java.util.Optional.ofNullable
import java.util.concurrent.CompletableFuture.supplyAsync
import java.util.concurrent.CompletionStage
import java.util.function.Supplier
import javax.persistence.EntityManager

/**
 * JPA implementation of DDD repository for aggregates.
 */
abstract class JPARepository<Aggregate : Any, Identity : Serializable> @JvmOverloads constructor(
    protected val jpaApi: JPAApi,
    protected val executionContext: DatabaseExecutionContext,
    protected val clazz: Class<out Aggregate>,
    protected val persistenceUnitName: String = "default"
) : TransactionalRepository<Aggregate, Identity> {

    protected fun <E> transaction(function: (EntityManager) -> E): E = jpaApi.withTransaction(persistenceUnitName, function)

    protected fun <E> readOnly(function: (EntityManager) -> E): E = jpaApi.withTransaction(persistenceUnitName, true, function)

    protected fun <E> execute(function: (EntityManager) -> E): CompletionStage<E> =
        supplyAsync(Supplier { transaction(function) }, executionContext)

    protected fun <E> executeRO(function: (EntityManager) -> E): CompletionStage<E> =
        supplyAsync(Supplier { readOnly(function) }, executionContext)

    protected fun <E> executeSession(function: (Session) -> E): CompletionStage<E> =
        execute { em -> function.invoke(em.unwrap(Session::class.java)) }

    protected fun <E> executeSessionRO(function: (Session) -> E): CompletionStage<E> =
        executeRO { em -> function.invoke(em.unwrap(Session::class.java)) }

    override fun get(id: Identity): CompletionStage<Optional<Aggregate>> =
        execute { em -> ofNullable(em.find(clazz, id)) }

    override fun getAll(): CompletionStage<List<Aggregate>> = executeRO { em ->
        val criteriaBuilder = em.criteriaBuilder
        @Suppress("UNCHECKED_CAST")
        val criteriaQuery = criteriaBuilder.createQuery(clazz as Class<Nothing>)
        val root = criteriaQuery.from(clazz)
        val all = criteriaQuery.select(root)
        em.createQuery(all).resultList
    }

    override fun findByIds(ids: List<Identity>): CompletionStage<List<Aggregate>> =
        executeSessionRO { session -> session.byMultipleIds(clazz).multiLoad<Identity>(ids) }

    override fun remove(aggregate: Aggregate): CompletionStage<Done> = execute { em ->
        if (em.contains(aggregate)) em.remove(aggregate)
        else em.remove(em.merge(aggregate))
        Done.getInstance()
    }

    override fun remove(aggregate: Aggregate, transaction: Transaction): CompletionStage<Done> {
        if (!(transaction is JPATransaction)) throw IllegalArgumentException("transaction must be JPATransaction")
        return transaction.remove(this, aggregate)
    }

    override fun removeAll(aggregates: Collection<Aggregate>): CompletionStage<Done> = execute { em ->
        aggregates.forEach {
            if (em.contains(it)) em.remove(it)
            else em.remove(em.merge(it))
        }
        Done.getInstance()
    }

    override fun removeAll(aggregates: Collection<Aggregate>, transaction: Transaction): CompletionStage<Done> {
        if (!(transaction is JPATransaction)) throw IllegalArgumentException("transaction must be JPATransaction")
        return transaction.remove(this, aggregates)
    }

    override fun create(aggregate: Aggregate): CompletionStage<Done> = execute { em ->
        em.persist(aggregate)
        Done.getInstance()
    }

    override fun create(aggregate: Aggregate, transaction: Transaction): CompletionStage<Done> {
        if (!(transaction is JPATransaction)) throw IllegalArgumentException("transaction must be JPATransaction")
        return transaction.create(this, aggregate)
    }

    override fun createAll(aggregates: Collection<Aggregate>): CompletionStage<Done> = execute { em ->
        aggregates.forEach { em.persist(it) }
        Done.getInstance()
    }

    override fun createAll(aggregates: Collection<Aggregate>, transaction: Transaction): CompletionStage<Done> {
        if (!(transaction is JPATransaction)) throw IllegalArgumentException("transaction must be JPATransaction")
        return transaction.create(this, aggregates)
    }

    override fun save(aggregate: Aggregate): CompletionStage<Done> = execute { em ->
        em.merge(aggregate)
        Done.getInstance()
    }

    override fun save(aggregate: Aggregate, transaction: Transaction): CompletionStage<Done> {
        if (!(transaction is JPATransaction)) throw IllegalArgumentException("transaction must be JPATransaction")
        return transaction.save(this, aggregate)
    }

    override fun saveAll(aggregates: Collection<Aggregate>): CompletionStage<Done> = execute { em ->
        aggregates.forEach { em.merge(it) }
        Done.getInstance()
    }

    override fun saveAll(aggregates: Collection<Aggregate>, transaction: Transaction): CompletionStage<Done> {
        if (!(transaction is JPATransaction)) throw IllegalArgumentException("transaction must be JPATransaction")
        return transaction.save(this, aggregates)
    }

    override fun createTransaction(): Transaction = JPATransaction(jpaApi, executionContext, persistenceUnitName)
}
