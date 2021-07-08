package org.taymyr.play.repository.infrastructure.persistence

import akka.Done
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.hibernate.Session
import org.taymyr.play.repository.domain.KRepository
import play.db.jpa.JPAApi
import java.io.Serializable
import java.lang.IllegalArgumentException
import javax.persistence.EntityManager

abstract class KJPARepository <Aggregate : Any, Identity : Serializable> @JvmOverloads constructor(
    jpaApi: JPAApi,
    executionContext: DatabaseExecutionContext,
    clazz: Class<out Aggregate>,
    persistenceUnitName: String = "default"
) : JPABaseRepository<Aggregate, Identity> (jpaApi, executionContext, clazz, persistenceUnitName), KRepository<Aggregate, Identity> {

    private val dispatcher = executionContext.asCoroutineDispatcher()

    private suspend fun <E> execute(function: (EntityManager) -> E): E = withContext(dispatcher) {
        transaction { function.invoke(it) }
    }

    private suspend fun <E> executeRO(function: (EntityManager) -> E): E = withContext(dispatcher) {
        readOnly { function.invoke(it) }
    }

    private suspend fun <E> executeSession(function: (Session) -> E): E = execute { em -> function.invoke(em.unwrap(Session::class.java)) }

    private suspend fun <E> executeSessionRO(function: (Session) -> E): E = executeRO { em -> function.invoke(em.unwrap(Session::class.java)) }

    override suspend fun get(id: Identity): Aggregate? = execute { em -> em.find(clazz, id) }

    override suspend fun getAll(): List<Aggregate> = executeRO { em ->
        val criteriaBuilder = em.criteriaBuilder
        @Suppress("UNCHECKED_CAST")
        val criteriaQuery = criteriaBuilder.createQuery(clazz as Class<Nothing>)
        val root = criteriaQuery.from(clazz)
        val all = criteriaQuery.select(root)
        em.createQuery(all).resultList
    }

    override suspend fun findByIds(ids: List<Identity>): List<Aggregate> = executeSessionRO { session -> session.byMultipleIds(clazz).multiLoad(ids) }

    override suspend fun remove(aggregate: Aggregate): Done = execute { em ->
        if (em.contains(aggregate)) em.remove(aggregate)
        else em.remove(em.merge(aggregate))
        Done.getInstance()
    }

    override suspend fun removeAll(aggregates: Collection<Aggregate>): Done = execute { em ->
        aggregates.forEach {
            if (em.contains(it)) em.remove(it)
            else em.remove(em.merge(it))
        }
        Done.getInstance()
    }

    override suspend fun create(aggregate: Aggregate): Done = execute { em ->
        em.persist(aggregate)
        Done.getInstance()
    }

    override suspend fun createAll(aggregates: Collection<Aggregate>): Done = execute { em ->
        aggregates.forEach { em.persist(it) }
        Done.getInstance()
    }

    override suspend fun save(aggregate: Aggregate): Done = execute { em ->
        em.merge(aggregate)
        Done.getInstance()
    }

    override suspend fun saveAll(aggregates: Collection<Aggregate>): Done = execute { em ->
        aggregates.forEach { em.merge(it) }
        Done.getInstance()
    }

    override suspend fun findAggregates(jpaQuery: String, parameters: Map<String, Any>): List<Aggregate> = executeRO { em ->
        val query = em.createQuery(jpaQuery, clazz)
        parameters.forEach { query.setParameter(it.key, it.value) }
        query.resultList.toList()
    }

    override suspend fun findAggregates(jpaQuery: String, parameters: Map<String, Any>, offset: Int, limit: Int): List<Aggregate> {
        if (offset < 0 || limit < 0) throw IllegalArgumentException("offset and limit must not be negative")
        return executeRO { em ->
            val query = em.createQuery(jpaQuery, clazz)
            parameters.forEach { query.setParameter(it.key, it.value) }
            query.firstResult = offset
            query.maxResults = limit
            query.resultList.toList()
        }
    }

    override suspend fun findAggregate(jpaQuery: String, parameters: Map<String, Any>): Aggregate? = executeRO { em ->
        val query = em.createQuery(jpaQuery, clazz)
        parameters.forEach { query.setParameter(it.key, it.value) }
        query.maxResults = 1
        return@executeRO if (query.resultList.toList().isEmpty()) null else query.resultList[0]
    }

    override suspend fun <E> findValues(jpaQuery: String, parameters: Map<String, Any>, clazz: Class<E>): List<E> = executeRO { em ->
        val query = em.createQuery(jpaQuery, clazz)
        parameters.forEach { query.setParameter(it.key, it.value) }
        query.resultList.toList()
    }

    override suspend fun <E> findValue(jpaQuery: String, parameters: Map<String, Any>, clazz: Class<E>): E? = executeRO { em ->
        val query = em.createQuery(jpaQuery, clazz)
        parameters.forEach { query.setParameter(it.key, it.value) }
        query.maxResults = 1
        return@executeRO if (query.resultList.toList().isEmpty()) null else query.resultList[0]
    }
}
