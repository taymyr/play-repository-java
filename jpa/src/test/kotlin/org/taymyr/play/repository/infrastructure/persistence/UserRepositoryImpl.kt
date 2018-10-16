package org.taymyr.play.repository.infrastructure.persistence

import org.taymyr.play.repository.domain.User
import org.taymyr.play.repository.domain.UserRepository
import play.db.jpa.JPAApi
import java.util.UUID
import javax.inject.Inject

/**
 * @author Sergey Morgunov {@literal <smorgunov@at-consulting.ru>}
 */
class UserRepositoryImpl @Inject constructor(
    jpaApi: JPAApi,
    executionContext: DatabaseExecutionContext
) : JPARepository<User, String>(jpaApi, executionContext, UserImpl::class.java), UserRepository {

    override fun nextIdentity(): String = UUID.randomUUID().toString()
}