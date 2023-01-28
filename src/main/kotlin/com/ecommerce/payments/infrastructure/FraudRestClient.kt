package com.ecommerce.payments.infrastructure

import com.ecommerce.payments.domain.FraudClient
import com.ecommerce.payments.domain.FraudResponse
import com.ecommerce.payments.domain.Payment
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

class FraudRestClient(private val client: HttpClient) : FraudClient {
    override fun evaluate(payment: Payment): FraudResponse {
        var response: FraudResponseDTO
        runBlocking {
            response = try {
                client.post("http://localhost:80/fraud/evaluation"){
                    contentType(ContentType.Application.Json)
                    setBody(createRequestFrom(payment))
                }.body()
            }catch (e: Exception){
                FraudResponseDTO(8)
            }
        }
        return FraudResponse(response.fraudScore)
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