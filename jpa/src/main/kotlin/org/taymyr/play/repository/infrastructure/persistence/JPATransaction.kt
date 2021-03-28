package org.taymyr.play.repository.infrastructure.persistence

import akka.Done
import org.taymyr.play.repository.domain.Transaction
import play.db.jpa.JPAApi
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Supplier
import javax.persistence.EntityManager

class JPATransaction(
    protected val jpaApi: JPAApi,
    protected val executionContext: DatabaseExecutionContext,
    protected val persistenceUnitName: String = "default"
) : Transaction {

    private val oparetionsLog: MutableList<Operation<*>> = mutableListOf()

    /**
     * Saves to transaction log remove operation for repository and aggregate.
     *
     * @param repository JPA repository
     * @param aggregate removed aggregate
     */
    fun <AGGREGATE : Any> remove(repository: JPARepository<AGGREGATE, *>, aggregate: AGGREGATE): CompletionStage<Done> {
        oparetionsLog.add(Remove(repository, listOf(aggregate)))
        return CompletableFuture.completedFuture(Done.getInstance())
    }

    /**
     * Saves to transaction log remove operation for repository and aggregates.
     *
     * @param repository JPA repository
     * @param aggregate removed aggregates
     */
    fun <AGGREGATE : Any> remove(repository: JPARepository<AGGREGATE, *>, aggregates: Collection<AGGREGATE>): CompletionStage<Done> {
        oparetionsLog.add(Remove(repository, aggregates))
        return CompletableFuture.completedFuture(Done.getInstance())
    }

    /**
     * Saves to transaction log create operation for repository and aggregate.
     *
     * @param repository JPA repository
     * @param aggregate created aggregate
     */
    fun <AGGREGATE : Any> create(repository: JPARepository<AGGREGATE, *>, aggregate: AGGREGATE): CompletionStage<Done> {
        oparetionsLog.add(Create(repository, listOf(aggregate)))
        return CompletableFuture.completedFuture(Done.getInstance())
    }

    /**
     * Saves to transaction log create operation for repository and aggregates.
     *
     * @param repository JPA repository
     * @param aggregate created aggregates
     */
    fun <AGGREGATE : Any> create(repository: JPARepository<AGGREGATE, *>, aggregates: Collection<AGGREGATE>): CompletionStage<Done> {
        oparetionsLog.add(Create(repository, aggregates))
        return CompletableFuture.completedFuture(Done.getInstance())
    }

    /**
     * Saves to transaction log save operation for repository and aggregate.
     *
     * @param repository JPA repository
     * @param aggregate saved aggregate
     */
    fun <AGGREGATE : Any> save(repository: JPARepository<AGGREGATE, *>, aggregate: AGGREGATE): CompletionStage<Done> {
        oparetionsLog.add(Save(repository, listOf(aggregate)))
        return CompletableFuture.completedFuture(Done.getInstance())
    }

    /**
     * Saves to transaction log save operation for repository and aggregates.
     *
     * @param repository JPA repository
     * @param aggregate saved aggregates
     */
    fun <AGGREGATE : Any> save(repository: JPARepository<AGGREGATE, *>, aggregates: Collection<AGGREGATE>): CompletionStage<Done> {
        oparetionsLog.add(Save(repository, aggregates))
        return CompletableFuture.completedFuture(Done.getInstance())
    }

    override fun commit(): CompletionStage<Done> = execute { em ->
        oparetionsLog.forEach { it.process(em) }
        Done.getInstance()
    }

    protected fun <E> transaction(function: (EntityManager) -> E): E = jpaApi.withTransaction(persistenceUnitName, function)

    protected fun <E> execute(function: (EntityManager) -> E): CompletionStage<E> =
        CompletableFuture.supplyAsync(Supplier { transaction(function) }, executionContext)

    private abstract class Operation<AGGREGATE : Any>(open val repository: JPARepository<AGGREGATE, *>, open val aggregates: Collection<AGGREGATE>) {
        abstract fun process(em: EntityManager)
    }

    private data class Remove<AGGREGATE : Any>(override val repository: JPARepository<AGGREGATE, *>, override val aggregates: Collection<AGGREGATE>) : Operation<AGGREGATE>(repository, aggregates) {
        override fun process(em: EntityManager) {
            aggregates.forEach {
                if (em.contains(it)) em.remove(it)
                else em.remove(em.merge(it))
            }
        }
    }

    private data class Create<AGGREGATE : Any>(override val repository: JPARepository<AGGREGATE, *>, override val aggregates: Collection<AGGREGATE>) : Operation<AGGREGATE>(repository, aggregates) {
        override fun process(em: EntityManager) {
            aggregates.forEach { em.persist(it) }
        }
    }

    private data class Save<AGGREGATE : Any>(override val repository: JPARepository<AGGREGATE, *>, override val aggregates: Collection<AGGREGATE>) : Operation<AGGREGATE>(repository, aggregates) {
        override fun process(em: EntityManager) {
            aggregates.forEach { em.merge(it) }
        }
    }
}
