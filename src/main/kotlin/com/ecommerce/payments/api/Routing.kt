package com.ecommerce.payments.api

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    routing {
        post("/ecommerce/payment") {
            TODO()
        }
    }
}

@Serializable
data class PaymentResponseDTO(val reference: String, val result: String, val fraudScore: Int)
