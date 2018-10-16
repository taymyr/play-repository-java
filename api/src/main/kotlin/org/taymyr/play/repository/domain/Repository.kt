package org.taymyr.play.repository.domain

import akka.Done
import java.util.Optional
import java.util.concurrent.CompletionStage

/**
 * DDD repository for identified aggregate.
 */
interface Repository<Aggregate, Identity> {

    /**
     * Generate a new identifier.
     */
    fun nextIdentity(): Identity

    /**
     * Get aggregate by the identifier.
     * @param id Identifier.
     * @return Optional with aggregate or empty Optional if aggregate not exist.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun get(id: Identity): CompletionStage<Optional<Aggregate>>

    /**
     * Get all aggregates from the repository.
     * @return List of aggregates or an empty list if the repository is empty.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun getAll(): CompletionStage<List<Aggregate>>

    /**
     * Finding aggregates on the repository by their identifiers.
     * @param ids List of identifiers.
     * @return List of aggregates or an empty list if aggregates not found.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun findByIds(ids: List<Identity>): CompletionStage<List<Aggregate>>

    /**
     * Finding aggregates on the repository by their identifiers.
     * @param ids List of identifiers.
     * @return List of aggregates or an empty list if aggregates not found.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    @JvmDefault
    fun findByIds(vararg ids: Identity): CompletionStage<List<Aggregate>> = findByIds(ids.asList())

    /**
     * Removing aggregate from the repository.
     * @param aggregate Aggregate.
     * @return [Done] if removing successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun remove(aggregate: Aggregate): CompletionStage<Done>

    /**
     * Removing aggregates from the repository.
     * @param aggregates List of aggregates.
     * @return [Done] if removing successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun removeAll(aggregates: Collection<Aggregate>): CompletionStage<Done>

    /**
     * Saving aggregate on the repository.
     * @param aggregate Aggregate.
     * @return [Done] if saving successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun save(aggregate: Aggregate): CompletionStage<Done>

    /**
     * Saving aggregates on the repository.
     * @param aggregates Aggregates.
     * @return [Done] if saving successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun saveAll(aggregates: Collection<Aggregate>): CompletionStage<Done>
}