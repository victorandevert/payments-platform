package com.ecommerce.payments.infrastructure

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import com.ecommerce.payments.domain.FraudClient
import com.ecommerce.payments.domain.FraudError
import com.ecommerce.payments.domain.FraudResponse
import com.ecommerce.payments.domain.Payment
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable

class FraudRestClient(private val client: HttpClient, private val url: String) : FraudClient {
    override fun evaluate(payment: Payment): Either<FraudError, FraudResponse> {
        var response: Either<FraudError, FraudResponse>
        runBlocking {
            val fraudScoreCall: Deferred<HttpResponse> = async {
                client.post("$url/fraud/evaluation"){
                    contentType(ContentType.Application.Json)
                    setBody(createRequestFrom(payment))
                }
            }
            val httpResponse = fraudScoreCall.await()
            response = when (OK) {
                httpResponse.status -> Right(FraudResponse(httpResponse.body<FraudResponseDTO>().fraudScore))
                else -> Left(FraudError())
            }
        }
        return response

    }

    private fun createRequestFrom(payment: Payment): PaymentDTO {
        return PaymentDTO(
            saleId = payment.saleId.value,
            amount = payment.amount.toThreeExponentRepresentation(),
            currency = payment.amount.currentCurrency()
        )
    }
}
@Serializable
data class FraudResponseDTO(val fraudScore: Int)