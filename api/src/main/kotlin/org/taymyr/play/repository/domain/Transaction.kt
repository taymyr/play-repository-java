package org.taymyr.play.repository.domain

import akka.Done
import java.util.concurrent.CompletionStage

/**
 * DDD repository transaction
 */
interface Transaction {

    /**
     * Commits transaction.
     */
    fun commit(): CompletionStage<Done>
}
