package com.ecommerce.payments.feature.infrastructure

import com.ecommerce.payments.domain.Amount
import com.ecommerce.payments.domain.FraudError
import com.ecommerce.payments.domain.Payment
import com.ecommerce.payments.domain.SaleId
import com.ecommerce.payments.infrastructure.FraudResponseDTO
import com.ecommerce.payments.infrastructure.FraudRestClient
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.fail
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation1

class FraudRestClientShould {

    @Test
    fun `return a fraud score`() = testApplication {
        application {
            install(ServerContentNegotiation1) {
                jackson()
            }
            routing {
                post("/fraud/evaluation") {
                    call.respond(status = HttpStatusCode.OK, FraudResponseDTO(5))
                }
            }
        }
        val httpClient = createClient {
            install(ClientContentNegotiation) {
                jackson()
            }
        }
        val payment = Payment(SaleId("SALE123"), Amount.from("100000", "EUR"))
        val fraudRestClient = FraudRestClient(httpClient, url = "http://localhost:80")

        fraudRestClient.evaluate(payment).fold(
            { fail("Should not happen") },
            { assertThat(it.score).isEqualTo(5) }
        )

    }

    @Test
    fun `return an error when the fraud service call fails`() = testApplication {
        application {
            install(ServerContentNegotiation1) {
                jackson()
            }
            routing {
                post("/fraud/evaluation") {
                    call.respond(status = HttpStatusCode.BadRequest, "some server error")
                }
            }
        }
        val httpClient = createClient {
            install(ClientContentNegotiation) {
                jackson()
            }
        }
        val payment = Payment(SaleId("SALE123"), Amount.from("1", "EUR"))
        val fraudRestClient = FraudRestClient(httpClient, url = "http://localhost:80")

        fraudRestClient.evaluate(payment)

        fraudRestClient.evaluate(payment).fold(
            { assertThat(it).isInstanceOf(FraudError::class.java) },
            { fail("Should not happen") }
        )
    }
}