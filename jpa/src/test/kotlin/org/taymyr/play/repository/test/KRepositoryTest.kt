package org.taymyr.play.repository.test

import akka.Done
import com.google.inject.AbstractModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beInstanceOf
import org.taymyr.play.repository.domain.User
import org.taymyr.play.repository.domain.UserKRepository
import org.taymyr.play.repository.infrastructure.persistence.UserImpl
import org.taymyr.play.repository.infrastructure.persistence.UserKRepositoryImpl
import javax.inject.Inject
import javax.persistence.PersistenceException

class KRepositoryTest : WordSpec() {

    @Inject
    lateinit var repository: UserKRepository

    private val users = ArrayList<UserImpl>()

    init {
        listener(
            PlayListener(
                module = object : AbstractModule() {
                    public override fun configure() {
                        bind(UserKRepository::class.java).to(UserKRepositoryImpl::class.java)
                    }
                }
            )
        )
        "Repository" should {
            "save 2000 aggregates" {
                for (i in 0..1999) {
                    val user = UserImpl(repository.nextIdentity(), "User-$i", "user-$i@repo.com")
                    users.add(user)
                }
                for (i in 0..1000) {
                    repository.save(users[i]) shouldBe beInstanceOf<Done>()
                }
                repository.saveAll(users.subList(1001, 2000)) shouldBe beInstanceOf<Done>()
            }
            "get all aggregates" {
                val allUsers = repository.getAll()
                allUsers shouldHaveSize users.size
                allUsers shouldContainAll users
            }
            "get aggregate by id" {
                val user = repository.get(users[10].id)
                user shouldNotBe null
                user shouldBe users[10]
            }
            "get null for not exist aggregate" {
                repository.get("aaaa-bbb") shouldBe null
            }
            "get aggregates by ids" {
                val partUsers = users.subList(0, 100)
                val resultUsers = repository.findByIds(partUsers.map { it.id })
                resultUsers shouldHaveSize partUsers.size
                resultUsers shouldContainAll partUsers

                val resultUsers2 = repository.findByIds(users[0].id, users[1].id)
                resultUsers2 shouldHaveSize 2
                resultUsers2 shouldContain users[0]
                resultUsers2 shouldContain users[1]

                repository.findByIds() shouldHaveSize 0
            }
            "remove aggregate" {
                repository.remove(users[0]) shouldBe beInstanceOf<Done>()
                repository.get(users[0].id) shouldBe null
            }
            "remove not exist aggregate" {
                repository.remove(UserImpl("1", "User", "user@repo.com")) shouldBe beInstanceOf<Done>()
            }
            "remove collection aggregates" {
                repository.removeAll(users.subList(0, 1000)) shouldBe beInstanceOf<Done>()
            }
            "throw IllegalArgumentException for unknown entity" {
                val illegal = shouldThrow<IllegalArgumentException> {
                    repository.remove(object : User {
                        override val id: String = "1"
                        override val fullname: String = "User"
                        override val email: String = "user@repo.com"
                    })
                }
                illegal shouldBe beInstanceOf<IllegalArgumentException>()
            }
            "create 2000 new aggregates" {
                for (i in 2000..3999) {
                    val user = UserImpl(repository.nextIdentity(), "User-$i", "user-$i@repo.com")
                    users.add(user)
                }
                for (i in 2000..3000) {
                    repository.create(users[i]) shouldBe beInstanceOf<Done>()
                }
                repository.createAll(users.subList(3001, 4000)) shouldBe beInstanceOf<Done>()
            }
            "throw PersistenceException for creating entity that already exists" {
                val persistence = shouldThrow<PersistenceException> {
                    repository.create(UserImpl(users[2000].id, "User-1", "user-1@repo.com"))
                }
                persistence shouldBe beInstanceOf<PersistenceException>()
            }
            "update for saving existing entity" {
                val updatedFullName = users[2000].fullname + "-updated"
                repository.save(UserImpl(users[2000].id, updatedFullName, users[2000].email))
                val user = repository.get(users[2000].id)
                user shouldNotBe null
                user?.fullname shouldBe updatedFullName
            }
            "find aggregates by jpa query" {
                val items = repository.findAggregates(
                    "select user from UserImpl user where user.fullname = :fullname",
                    mapOf("fullname" to users[2001].fullname)
                )
                items shouldHaveSize 1
                items shouldContainAll listOf(users[2001])
            }
            "find aggregates with pagination by jpa query" {
                val selectedUsers = listOf(users[2001], users[2002], users[2003], users[2004])
                val items = repository.findAggregates(
                    "select user from UserImpl user where user.fullname in :names",
                    mapOf("names" to selectedUsers.map { it.fullname }),
                    offset = 0, limit = 2
                )
                items shouldHaveSize 2
                selectedUsers shouldContain items[0]
                selectedUsers shouldContain items[1]
            }
            "find aggregate by jpa query" {
                val user = repository.findAggregate(
                    "select user from UserImpl user where user.fullname = :name",
                    mapOf("name" to users[2004].fullname)
                )
                user shouldNotBe null
                user shouldBe users[2004]
            }
            "find values by jpa query" {
                val selectedUsers = listOf(users[2001], users[2002])
                val items = repository.findValues(
                    "select user.email from UserImpl user where user.fullname in :names",
                    mapOf("names" to selectedUsers.map { it.fullname }),
                    clazz = String::class.java
                )
                items shouldHaveSize 2
                items shouldContain selectedUsers[0].email
                items shouldContain selectedUsers[1].email
            }
            "find value by jpa query" {
                repository.findValue(
                    "select user.email from UserImpl user where user.fullname in :fullname",
                    mapOf("fullname" to users[2005].fullname),
                    clazz = String::class.java
                ) shouldBe users[2005].email
            }
        }
    }
}
