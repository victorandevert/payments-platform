package com.ecommerce.payments.infrastructure

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.None
import arrow.core.Some
import com.ecommerce.payments.domain.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

class PspRestClient(private val client: HttpClient, private val url: String) : PspClient {
    override fun payWith(payment: Payment): Either<PspError, PspResponse> {
        var response : Either<PspError, PspResponse>
        runBlocking {
            val pspCall: Deferred<HttpResponse> = async {
                client.post("$url/psp/payment"){
                    contentType(ContentType.Application.Json)
                    setBody(createRequestFrom(payment))
                }
            }
            response = when (HttpStatusCode.OK) {
                pspCall.await().status -> Right(getResult(pspCall.await()))
                else -> Left(PspError())
            }
        }
        return response
    }

    private suspend fun getResult(httpResponse: HttpResponse): PspResponse {
        return if (httpResponse.body<PspResponseDTO>().result == "DENIED") {
            PspResponse(
                None,
                httpResponse.body<PspResponseDTO>().result
            )
        } else {
            PspResponse(
                Some(Reference(httpResponse.body<PspResponseDTO>().reference!!)),
                httpResponse.body<PspResponseDTO>().result
            )
        }
    }
}
private fun createRequestFrom(payment: Payment): PaymentDTO {
    return PaymentDTO(
        saleId = payment.saleId.value,
        amount = payment.amount.toThreeExponentRepresentation(),
        currency = payment.amount.currentCurrency()
    )
}

@Serializable
data class PspResponseDTO(val reference: String?, val result: String)