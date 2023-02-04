package com.ecommerce.payments.feature.infrastructure

import com.ecommerce.payments.domain.Amount
import com.ecommerce.payments.domain.Payment
import com.ecommerce.payments.domain.PspError
import com.ecommerce.payments.domain.SaleId
import com.ecommerce.payments.infrastructure.PspResponseDTO
import com.ecommerce.payments.infrastructure.PspRestClient
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.fail
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

class PspRestClientShould {

    @Test
    fun `return an Accepted with reference`() = testApplication {
        application {
            install(ServerContentNegotiation) {
                json()
            }
            routing {
                post("/psp/payment") {
                    call.respond(status = HttpStatusCode.OK, PspResponseDTO("12222-2222-222","ACCEPTED"))
                }
            }
        }
        val httpClient = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }
        val payment = Payment(SaleId("SALE123"), Amount.from("100000", "EUR"))
        val pspRestClient = PspRestClient(httpClient, url = "http://localhost:80")

        pspRestClient.payWith(payment).fold(
            { fail("Should not happen") },
            { assertThat(it.reference.value).isEqualTo("12222-2222-222")
              assertThat(it.result).isEqualTo("ACCEPTED") }
        )

    }

    @Test
    fun `return an error when call to psp service fails`() = testApplication {
        application {
            install(ServerContentNegotiation) {
                json()
            }
            routing {
                post("/psp/payment") {
                    call.respond(status = HttpStatusCode.BadRequest, "some server error")
                }
            }
        }
        val httpClient = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }
        val payment = Payment(SaleId("SALE123"), Amount.from("100000", "EUR"))
        val pspRestClient = PspRestClient(httpClient, url = "http://localhost:80")

        pspRestClient.payWith(payment).fold(
            { assertThat(it).isInstanceOf(PspError::class.java) },
            { fail("Should not happen") }
        )
    }

}