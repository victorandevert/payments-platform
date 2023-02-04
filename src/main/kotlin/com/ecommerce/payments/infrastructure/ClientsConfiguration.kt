package com.ecommerce.payments.infrastructure

import com.ecommerce.payments.domain.PaymentService
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

const val pspURL = "http://localhost:4000"
const val fraudURL = "http://localhost:3000"

fun createHttpClient() = HttpClient(Apache) {
    install(ContentNegotiation) {
        json()
    }
}

fun pspRestClient(client: HttpClient, pspURL: String) = PspRestClient(client, url = pspURL)
fun fraudRestClient(client: HttpClient, fraudURL: String) = FraudRestClient(client, url = fraudURL)

fun paymentService(client: HttpClient, pspURL: String, fraudURL: String): PaymentService {
    return PaymentService(
        pspRestClient(client, pspURL),
        fraudRestClient(client, fraudURL)
    )
}

