package com.ecommerce.payments.api

import com.ecommerce.payments.domain.*
import com.fasterxml.jackson.annotation.JsonInclude
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

fun Application.configureRouting(paymentService: PaymentService) {
    routing {
        post("/ecommerce/payment") {
            val requestBody = call.receive<PaymentRequestDTO>()
            val payment: Deferred<PaymentResult> = async(Dispatchers.Default){
                paymentService.makeA(Payment(
                    saleId = SaleId(requestBody.saleId), amount = Amount.from(requestBody.amount,requestBody.currency)
                ))
            }
            val response = payment.await()
            response.reference.fold(
                { call.respond(status = HttpStatusCode.OK, PaymentResponseDTO(reference = null, result = response.result, response.fraudScore)) },
                { call.respond(status = HttpStatusCode.OK, PaymentResponseDTO(reference = it.value, result = response.result, response.fraudScore)) }
            )
        }
    }
}

data class PaymentRequestDTO(val saleId: String, val amount: String, val currency: String)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentResponseDTO(val reference: String?, val result: String, val fraudScore: Int?)
