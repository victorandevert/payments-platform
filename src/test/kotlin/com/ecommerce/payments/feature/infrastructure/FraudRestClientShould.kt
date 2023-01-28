package com.ecommerce.payments.feature.infrastructure

import com.ecommerce.payments.domain.Amount
import com.ecommerce.payments.domain.Payment
import com.ecommerce.payments.domain.SaleId
import com.ecommerce.payments.infrastructure.FraudResponseDTO
import com.ecommerce.payments.infrastructure.FraudRestClient
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation1

class FraudRestClientShould {

    @Test
    fun `return a fraud score`() = testApplication {
        application {
            install(ServerContentNegotiation1){
                json()
            }
            routing {
                post("/fraud/evaluation"){
                    call.respond(status = HttpStatusCode.Created, FraudResponseDTO(5))
                }
            }
        }
        val httpClient = createClient {
            install(ClientContentNegotiation){
                json()
            }
        }
        val payment = Payment(SaleId("SALE123"), Amount.from("100000","EUR"))
        val fraudRestClient = FraudRestClient(httpClient)

        val response = fraudRestClient.evaluate(payment)

        assertThat(response.score).isEqualTo(5)
    }

    @Test
    fun `return an error when call to fraud service fails`() = testApplication {
        application {
            install(ServerContentNegotiation1){
                json()
            }
            routing {
                post("/fraud/evaluation"){
                    call.respond(status = HttpStatusCode.BadRequest, "some server error")
                }
            }
        }
        val httpClient = createClient {
            install(ClientContentNegotiation){
                json()
            }
        }
        val payment = Payment(SaleId("SALE123"), Amount.from("1","EUR"))
        val fraudRestClient = FraudRestClient(httpClient)

        val response = fraudRestClient.evaluate(payment)

        assertThat(response.score).isEqualTo(5)
    }
}