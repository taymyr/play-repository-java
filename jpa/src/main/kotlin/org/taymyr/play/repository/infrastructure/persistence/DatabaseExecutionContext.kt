package org.taymyr.play.repository.infrastructure.persistence

import akka.actor.ActorSystem
import scala.concurrent.ExecutionContextExecutor
import javax.inject.Inject

/**
 * The context for asynchronously execute database queries.
 */
class DatabaseExecutionContext @Inject constructor(actorSystem: ActorSystem) : ExecutionContextExecutor {

    private val executionContext = actorSystem.dispatchers().lookup(DISPATCHER_NAME)

    override fun execute(command: Runnable) {
        executionContext.execute(command)
    }

    override fun reportFailure(cause: Throwable) {
        executionContext.reportFailure(cause)
    }

    companion object {
        const val DISPATCHER_NAME = "database.dispatcher"
    }
}
