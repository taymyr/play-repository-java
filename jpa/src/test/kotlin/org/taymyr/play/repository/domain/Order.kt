package org.taymyr.play.repository.domain

interface Order {
    val id: String
    val product: Product
    val volume: Int?
}
