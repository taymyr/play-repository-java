package org.taymyr.play.repository.test

import com.google.inject.AbstractModule
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import io.kotlintest.whenReady
import org.taymyr.play.repository.domain.OrderRepository
import org.taymyr.play.repository.domain.ProductRepository
import org.taymyr.play.repository.infrastructure.persistence.OrderImpl
import org.taymyr.play.repository.infrastructure.persistence.OrderRepositoryImpl
import org.taymyr.play.repository.infrastructure.persistence.ProductImpl
import org.taymyr.play.repository.infrastructure.persistence.ProductRepositoryImpl
import javax.inject.Inject

class TransactionTest : WordSpec() {

    override fun listeners(): List<TestListener> = listOf(PlayListener(
        object : AbstractModule() {
            public override fun configure() {
                bind(ProductRepository::class.java).to(ProductRepositoryImpl::class.java)
                bind(OrderRepository::class.java).to(OrderRepositoryImpl::class.java)
            }
        }
    ))

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var orderRepository: OrderRepository

    init {
        "Transaction" should {
            "save all entities if transaction success" {
                val product = ProductImpl(productRepository.nextIdentity(), "product", 1000)
                whenReady(
                    productRepository.save(product)
                        .thenApply { done -> "success" }
                        .toCompletableFuture()) { result ->
                    result shouldBe "success"
                }

                val orderedVolume = 100
                val updatedProduct = product.copy(volume = product.volume - orderedVolume)
                val order = OrderImpl(orderRepository.nextIdentity(), updatedProduct, orderedVolume)
                val transaction = productRepository.createTransaction()
                whenReady(
                    productRepository.save(updatedProduct, transaction)
                        .thenCompose { orderRepository.save(order, transaction) }
                        .thenCompose { transaction.commit() }
                        .thenApply { done -> "success" }
                        .toCompletableFuture()) { result ->
                    result shouldBe "success"
                }
                whenReady(productRepository.get(product.id).toCompletableFuture()) { result ->
                    result.isPresent shouldBe true
                    result.get() shouldBe updatedProduct
                }
                whenReady(orderRepository.get(order.id).toCompletableFuture()) { result ->
                    result.isPresent shouldBe true
                    result.get() shouldBe order
                }
            }

            "save none entities if transaction fail" {
                val product = ProductImpl(productRepository.nextIdentity(), "product", 1000)
                whenReady(
                    productRepository.save(product)
                        .thenApply { done -> "success" }
                        .toCompletableFuture()) { result ->
                    result shouldBe "success"
                }

                val orderedVolume = 100
                val updatedProduct = product.copy(volume = product.volume - orderedVolume)
                val order = OrderImpl(orderRepository.nextIdentity(), updatedProduct, null)
                val transaction = productRepository.createTransaction()
                whenReady(
                    productRepository.save(updatedProduct, transaction)
                        .thenCompose { orderRepository.save(order, transaction) }
                        .thenCompose { transaction.commit() }
                        .thenApply { done -> "success" }
                        .exceptionally { th -> "fail" }
                        .toCompletableFuture()) { result ->
                    result shouldBe "fail"
                }
                whenReady(productRepository.get(product.id).toCompletableFuture()) { result ->
                    result.isPresent shouldBe true
                    result.get() shouldBe product
                }
                whenReady(orderRepository.get(order.id).toCompletableFuture()) { result ->
                    result.isPresent shouldBe false
                }
            }
        }
    }
}
