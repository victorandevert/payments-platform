package com.ecommerce

import com.ecommerce.payments.api.configureRouting
import com.ecommerce.payments.infrastructure.createHttpClient
import com.ecommerce.payments.infrastructure.fraudURL
import com.ecommerce.payments.infrastructure.paymentService
import com.ecommerce.payments.infrastructure.pspURL
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*



fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation){
        jackson()
    }
    configureRouting(paymentService(createHttpClient(), pspURL, fraudURL))
}

