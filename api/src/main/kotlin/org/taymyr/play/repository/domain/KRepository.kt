package org.taymyr.play.repository.domain

import akka.Done

/**
 * DDD repository for identified aggregate (use coroutines).
 */
interface KRepository<Aggregate, Identity> {

    /**
     * Generate a new identifier.
     */
    fun nextIdentity(): Identity

    /**
     * Get aggregate by the identifier.
     * @param id Identifier.
     * @return aggregate or null if aggregate not exist.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun get(id: Identity): Aggregate?

    /**
     * Get all aggregates from the repository.
     * @return List of aggregates or an empty list if the repository is empty.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun getAll(): List<Aggregate>

    /**
     * Finding aggregates on the repository by their identifiers.
     * @param ids List of identifiers.
     * @return List of aggregates or an empty list if aggregates not found.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun findByIds(ids: List<Identity>): List<Aggregate>

    /**
     * Finding aggregates on the repository by their identifiers.
     * @param ids List of identifiers.
     * @return List of aggregates or an empty list if aggregates not found.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun findByIds(vararg ids: Identity): List<Aggregate> = findByIds(ids.asList())

    /**
     * Removing aggregate from the repository.
     * @param aggregate Aggregate.
     * @return [Done] if removing successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun remove(aggregate: Aggregate): Done

    /**
     * Removing aggregates from the repository.
     * @param aggregates List of aggregates.
     * @return [Done] if removing successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun removeAll(aggregates: Collection<Aggregate>): Done

    /**
     * Create aggregate on the repository.
     * @param aggregate Aggregate.
     * @return [Done] if creation successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun create(aggregate: Aggregate): Done

    /**
     * Create aggregates on the repository.
     * @param aggregates Aggregates.
     * @return [Done] if creation successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun createAll(aggregates: Collection<Aggregate>): Done

    /**
     * Saving aggregate on the repository.
     * @param aggregate Aggregate.
     * @return [Done] if saving successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun save(aggregate: Aggregate): Done

    /**
     * Saving aggregates on the repository.
     * @param aggregates Aggregates.
     * @return [Done] if saving successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun saveAll(aggregates: Collection<Aggregate>): Done

    /**
     * Finding aggregates on the repository by jpaQuery.
     * @param jpaQuery Jpa query.
     * @param parameters Map of parameters name and value in jpa query.
     * @return List of aggregates or an empty list if aggregates not found.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun findAggregates(jpaQuery: String, parameters: Map<String, Any>): List<Aggregate>

    /**
     * Finding aggregates on the repository by jpaQuery and using pagination.
     * @param jpaQuery Jpa query.
     * @param parameters Map of parameters name and value in jpa query.
     * @param offset Offset from the beginning of the list
     * @param limit The number of elements in the sample.
     * @return List of aggregates or an empty list if aggregates not found.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun findAggregates(jpaQuery: String, parameters: Map<String, Any>, offset: Int, limit: Int): List<Aggregate>

    /**
     * Finding aggregate on the repository by jpaQuery.
     * @param jpaQuery Jpa query.
     * @param parameters Map of parameters name and value in jpa query.
     * @return aggregate or null if aggregate not exist.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun findAggregate(jpaQuery: String, parameters: Map<String, Any>): Aggregate?

    /**
     * Finding specific values on the repository by jpaQuery.
     * @param jpaQuery Jpa query.
     * @param parameters Map of parameters name and value in jpa query.
     * @param clazz Class of the find value.
     * @return List of aggregates or an empty list if aggregates not found.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun <E> findValues(jpaQuery: String, parameters: Map<String, Any>, clazz: Class<E>): List<E>

    /**
     * Find specific value on the repository by jpaQuery.
     * @param jpaQuery Jpa query.
     * @param parameters Map of parameters name and value in jpa query.
     * @param clazz Class of the find value.
     * @return Specific value or null.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    suspend fun <E> findValue(jpaQuery: String, parameters: Map<String, Any>, clazz: Class<E>): E?
}
