package com.ecommerce.payments.feature.infrastructure

import arrow.core.None
import com.ecommerce.payments.domain.Amount
import com.ecommerce.payments.domain.Payment
import com.ecommerce.payments.domain.PspError
import com.ecommerce.payments.domain.SaleId
import com.ecommerce.payments.infrastructure.PspResponseDTO
import com.ecommerce.payments.infrastructure.PspRestClient
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
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

class PspRestClientShould {

    @Test
    fun `return an cccepted with reference`() = testApplication {
        application {
            install(ServerContentNegotiation) {
                jackson()
            }
            routing {
                post("/psp/payment") {
                    call.respond(status = HttpStatusCode.OK, PspResponseDTO("12222-2222-222","ACCEPTED"))
                }
            }
        }
        val httpClient = createClient {
            install(ClientContentNegotiation) {
                jackson()
            }
        }
        val payment = Payment(SaleId("SALE123"), Amount.from("100000", "EUR"))
        val pspRestClient = PspRestClient(httpClient, url = "http://localhost:80")

        pspRestClient.payWith(payment).fold(
            { fail("Should not happen") },
            { it.reference.map { assertThat(it.value).isEqualTo("12222-2222-222") }
              assertThat(it.result).isEqualTo("ACCEPTED") }
        )

    }

    @Test
    fun `return an denied status with no reference`() = testApplication {
        application {
            install(ServerContentNegotiation) {
                jackson()
            }
            routing {
                post("/psp/payment") {
                    call.respond(status = HttpStatusCode.OK, PspResponseDTO(null,"DENIED"))
                }
            }
        }
        val httpClient = createClient {
            install(ClientContentNegotiation) {
                jackson()
            }
        }
        val payment = Payment(SaleId("SALE123"), Amount.from("100000", "EUR"))
        val pspRestClient = PspRestClient(httpClient, url = "http://localhost:80")

        pspRestClient.payWith(payment).fold(
            { fail("Should not happen") },
            { assertThat(it.reference).isEqualTo(None)
                assertThat(it.result).isEqualTo("DENIED") }
        )

    }

    @Test
    fun `return an error when call to psp service fails`() = testApplication {
        application {
            install(ServerContentNegotiation) {
                jackson()
            }
            routing {
                post("/psp/payment") {
                    call.respond(status = HttpStatusCode.BadRequest, "some server error")
                }
            }
        }
        val httpClient = createClient {
            install(ClientContentNegotiation) {
                jackson()
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