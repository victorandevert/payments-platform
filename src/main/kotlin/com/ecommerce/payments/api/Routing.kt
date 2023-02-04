package com.ecommerce.payments.api

import com.ecommerce.payments.domain.Amount
import com.ecommerce.payments.domain.Payment
import com.ecommerce.payments.domain.PaymentService
import com.ecommerce.payments.domain.SaleId
import com.fasterxml.jackson.annotation.JsonInclude
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(paymentService: PaymentService) {
    routing {
        post("/ecommerce/payment") {
            val requestBody = call.receive<PaymentRequestDTO>()
            val response = paymentService.makeA(Payment(
                saleId = SaleId(requestBody.saleId), amount = Amount.from(requestBody.amount,requestBody.currency)
            ))
            response.reference.fold(
                { call.respond(status = OK, PaymentResponseDTO(reference = null, result = response.result, response.fraudScore)) },
                { call.respond(status = OK, PaymentResponseDTO(reference = it.value, result = response.result, response.fraudScore)) }
            )
        }
    }
}

data class PaymentRequestDTO(val saleId: String, val amount: String, val currency: String)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentResponseDTO(val reference: String?, val result: String, val fraudScore: Int?)
