package com.ecommerce.payments.feature

import com.ecommerce.module
import com.ecommerce.payments.api.PaymentResponseDTO
import com.ecommerce.payments.domain.PspResponse
import com.ecommerce.payments.domain.Reference
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class PaymentFeature {

    @Test
    fun `should return an accepted payment`() = testApplication {
        application {
            module()
            routing {
                post("http://localhost:3000/fraud/evaluation"){
                    call.respond(status = HttpStatusCode.Created, 5)
                }
                post("http://localhost:4000/psp/payment"){
                    call.respond(status = HttpStatusCode.Created, PspResponse(Reference("12222-2222-222"), "ACCEPTED"))
                }
            }
        }
        val httpClient = createClient {
            install(ContentNegotiation){
                json()
            }
        }
        val response = httpClient.post("/ecommerce/payment") {
            contentType(ContentType.Application.Json)
            setBody("{\"saleId\": \"SALE123\", \"amount\": 100000, \"currency\": \"EUR\"}")
        }.body<PaymentResponseDTO>()


        assertThat(response.reference).isEqualTo("12222-2222-222")
        assertThat(response.result).isEqualTo("ACCEPTED")
        assertThat(response.fraudScore).isEqualTo("5")
    }
 }