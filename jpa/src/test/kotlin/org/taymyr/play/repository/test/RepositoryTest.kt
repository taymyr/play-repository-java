package org.taymyr.play.repository.test

import com.google.inject.AbstractModule
import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec
import io.kotlintest.whenReady
import jakarta.persistence.PersistenceException
import org.apache.pekko.Done
import org.taymyr.play.repository.domain.User
import org.taymyr.play.repository.domain.UserRepository
import org.taymyr.play.repository.infrastructure.persistence.UserImpl
import org.taymyr.play.repository.infrastructure.persistence.UserRepositoryImpl
import java.util.concurrent.ExecutionException
import javax.inject.Inject

class RepositoryTest : WordSpec() {
    override fun listeners(): List<TestListener> =
        listOf(
            PlayListener(
                object : AbstractModule() {
                    public override fun configure() {
                        bind(UserRepository::class.java).to(UserRepositoryImpl::class.java)
                    }
                },
            ),
        )

    @Inject
    lateinit var repository: UserRepository

    private val users = ArrayList<UserImpl>()

    init {
        "Repository" should {
            "save 2000 aggregates" {
                for (i in 0..1999) {
                    val user = UserImpl(repository.nextIdentity(), "User-$i", "user-$i@repo.com")
                    users.add(user)
                }
                for (i in 0..1000) {
                    whenReady(repository.save(users[i]).toCompletableFuture()) { result ->
                        result shouldBe beInstanceOf<Done>()
                    }
                }
                whenReady(repository.saveAll(users.subList(1001, 2000)).toCompletableFuture()) { result ->
                    result shouldBe beInstanceOf<Done>()
                }
            }
            "get all aggregates" {
                whenReady(repository.getAll().toCompletableFuture()) { allUsers ->
                    allUsers shouldHaveSize users.size
                    allUsers shouldContainAll users
                }
            }
            "get aggregate by id" {
                whenReady(repository.get(users[10].id).toCompletableFuture()) { user ->
                    user.isPresent shouldBe true
                    user.get() shouldBe users[10]
                }
            }
            "get empty optional for not exist aggregate" {
                whenReady(repository.get("aaaa-bbb").toCompletableFuture()) { user ->
                    user.isPresent shouldBe false
                }
            }
            "get aggregates by ids" {
                val partUsers = users.subList(0, 100)
                whenReady(repository.findByIds(partUsers.map { it.id }).toCompletableFuture()) { resultUsers ->
                    resultUsers shouldHaveSize partUsers.size
                    resultUsers shouldContainAll partUsers
                }
                whenReady(repository.findByIds(users[0].id, users[1].id).toCompletableFuture()) { resultUsers ->
                    resultUsers shouldHaveSize 2
                    resultUsers shouldContain users[0]
                    resultUsers shouldContain users[1]
                }
                whenReady(repository.findByIds().toCompletableFuture()) { resultUsers ->
                    resultUsers shouldHaveSize 0
                }
            }
            "remove aggregate" {
                whenReady(repository.remove(users[0]).toCompletableFuture()) { result ->
                    result shouldBe beInstanceOf<Done>()
                }
                whenReady(repository.get(users[0].id).toCompletableFuture()) { user ->
                    user.isPresent shouldBe false
                }
            }
            "remove not exist aggregate" {
                whenReady(repository.remove(UserImpl("1", "User", "user@repo.com")).toCompletableFuture()) { result ->
                    result shouldBe beInstanceOf<Done>()
                }
            }
            "remove collection aggregates" {
                whenReady(repository.removeAll(users.subList(0, 1000)).toCompletableFuture()) { result ->
                    result shouldBe beInstanceOf<Done>()
                }
            }
            "throw IllegalArgumentException for unknown entity" {
                val illegal =
                    shouldThrow<ExecutionException> {
                        whenReady(
                            repository
                                .remove(
                                    object : User {
                                        override val id: String = "1"
                                        override val fullname: String = "User"
                                        override val email: String = "user@repo.com"
                                    },
                                ).toCompletableFuture(),
                        ) {}
                    }
                illegal.cause shouldBe beInstanceOf<IllegalArgumentException>()
            }
            "create 2000 new aggregates" {
                for (i in 2000..3999) {
                    val user = UserImpl(repository.nextIdentity(), "User-$i", "user-$i@repo.com")
                    users.add(user)
                }
                for (i in 2000..3000) {
                    whenReady(repository.create(users[i]).toCompletableFuture()) { result ->
                        result shouldBe beInstanceOf<Done>()
                    }
                }
                whenReady(repository.createAll(users.subList(3001, 4000)).toCompletableFuture()) { result ->
                    result shouldBe beInstanceOf<Done>()
                }
            }
            "throw PersistenceException for creating entity that already exists" {
                val persistence =
                    shouldThrow<ExecutionException> {
                        whenReady(repository.create(UserImpl(users[2000].id, "User-1", "user-1@repo.com")).toCompletableFuture()) {}
                    }
                persistence.cause shouldBe beInstanceOf<PersistenceException>()
            }
            "update for saving existing entity" {
                val updatedFullName = users[2000].fullname + "-updated"
                whenReady(
                    repository
                        .save(UserImpl(users[2000].id, updatedFullName, users[2000].email))
                        .thenCompose { _ -> repository.get(users[2000].id) }
                        .toCompletableFuture(),
                ) { user ->
                    user.isPresent shouldBe true
                    user.get().fullname shouldBe updatedFullName
                }
            }
        }
    }
}
