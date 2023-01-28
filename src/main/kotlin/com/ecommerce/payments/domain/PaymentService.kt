package com.ecommerce.payments.domain

import java.util.*

class PaymentService(private val pspClient: PspClient, private val fraudClient: FraudClient) {

    fun makeA(payment: Payment): PaymentResult {
        val fraudResponse = fraudClient.evaluate(payment)
        if (fraudResponse.score<=5){
            val response = pspClient.payWith(payment)
            return PaymentResult(Optional.of(Reference(response.reference)), response.result, fraudResponse.score)
        }else{
            return PaymentResult("DENIED", fraudResponse.score)
        }

    }
}