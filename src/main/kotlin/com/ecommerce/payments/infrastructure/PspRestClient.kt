package com.ecommerce.payments.infrastructure

import arrow.core.Either
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

class PspRestClient(private val client: HttpClient) : PspClient {
    override fun payWith(payment: Payment): Either<PspError, PspResponse> {
        var response : Either<PspError, PspResponse>
        runBlocking {
            val pspCall: Deferred<HttpResponse> = async {
                client.post("http://localhost:80/psp/payment"){
                    contentType(ContentType.Application.Json)
                    setBody(createRequestFrom(payment))
                }
            }
            val httpResponse = pspCall.await()
            response = when (HttpStatusCode.OK) {
                httpResponse.status -> Either.right(
                    PspResponse(Reference(httpResponse.body<PspResponseDTO>().reference),
                        httpResponse.body<PspResponseDTO>().result))
                else -> Either.left(PspError())
            }
        }
        return response
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
data class PspResponseDTO(val reference: String, val result: String)