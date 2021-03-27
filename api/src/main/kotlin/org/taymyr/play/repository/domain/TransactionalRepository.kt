package org.taymyr.play.repository.domain

import akka.Done
import java.util.concurrent.CompletionStage

/**
 * DDD repository for identified aggregate with multy-repository transactions.
 */
interface TransactionalRepository<Aggregate, Identity> : Repository<Aggregate, Identity> {

    /**
     * Removing aggregate from the repository within multy-repository transaction.
     * @param aggregate Aggregate.
     * @param transaction Transaction
     * @return [Done] if removing successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun remove(aggregate: Aggregate, transaction: Transaction): CompletionStage<Done>

    /**
     * Removing aggregates from the repository within multy-repository transaction.
     * @param aggregates List of aggregates.
     * @param transaction Transaction
     * @return [Done] if removing successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun removeAll(aggregates: Collection<Aggregate>, transaction: Transaction): CompletionStage<Done>

    /**
     * Create aggregate on the repository within multy-repository transaction.
     * @param aggregate Aggregate.
     * @param transaction Transaction
     * @return [Done] if creation successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun create(aggregate: Aggregate, transaction: Transaction): CompletionStage<Done>

    /**
     * Create aggregates on the repository within multy-repository transaction.
     * @param aggregates Aggregates.
     * @param transaction Transaction
     * @return [Done] if creation successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun createAll(aggregates: Collection<Aggregate>, transaction: Transaction): CompletionStage<Done>

    /**
     * Saving aggregate on the repository within multy-repository transaction.
     * @param aggregate Aggregate.
     * @param transaction Transaction
     * @return [Done] if saving successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun save(aggregate: Aggregate, transaction: Transaction): CompletionStage<Done>

    /**
     * Saving aggregates on the repository within multy-repository transaction.
     * @param aggregates Aggregates.
     * @param transaction Transaction
     * @return [Done] if saving successfully. Otherwise will throw an exception.
     * @throws Exception Any exceptions while execute a query on the database will wrapped.
     */
    fun saveAll(aggregates: Collection<Aggregate>, transaction: Transaction): CompletionStage<Done>

    /**
     * Create new multy-repository transaction.
     */
    fun createTransaction(): Transaction
}
