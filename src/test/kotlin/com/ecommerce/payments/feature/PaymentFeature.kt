package com.ecommerce.payments.feature

import com.ecommerce.payments.api.PaymentResponseDTO
import com.ecommerce.payments.api.configureRouting
import com.ecommerce.payments.domain.PspResponse
import com.ecommerce.payments.domain.Reference
import com.ecommerce.payments.infrastructure.FraudResponseDTO
import com.ecommerce.payments.infrastructure.PspResponseDTO
import com.ecommerce.payments.infrastructure.paymentService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

class PaymentFeature {

    @Test
    fun `should return an accepted payment`() = testApplication {
        val httpClient = createClient {
            install(ClientContentNegotiation){
                json()
            }
        }
        application {
            install(ServerContentNegotiation){
                json()
            }
            configureRouting(paymentService(
                httpClient,
                "http://localhost:80",
                "http://localhost:80")
            )
            routing {
                post("/fraud/evaluation"){
                    call.respond(status = HttpStatusCode.OK, FraudResponseDTO(5))
                }
                post("/psp/payment"){
                    call.respond(status = HttpStatusCode.OK, PspResponseDTO("12222-2222-222", "ACCEPTED"))
                }
            }
        }

        val response = httpClient.post("/ecommerce/payment") {
            contentType(ContentType.Application.Json)
            setBody("{\"saleId\": \"SALE123\", \"amount\": 100000, \"currency\": \"EUR\"}")
        }.body<PaymentResponseDTO>()


        assertThat(response.reference).isEqualTo("12222-2222-222")
        assertThat(response.result).isEqualTo("ACCEPTED")
        assertThat(response.fraudScore).isEqualTo(5)
    }
 }