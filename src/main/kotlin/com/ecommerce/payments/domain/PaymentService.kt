package com.ecommerce.payments.domain

import java.util.*

class PaymentService(private val pspClient: PspClient, private val fraudClient: FraudClient) {

    fun makeA(payment: Payment): PaymentResult {
        val fraudResponse: FraudResponse
        if (isItNecessaryToEvaluateFraud(payment)) {
            fraudResponse = fraudClient.evaluate(payment)
            if (fraudResponse.score<=5){
                val response = pspClient.payWith(payment)
                return PaymentResult(Optional.of(Reference(response.reference)), response.result, fraudResponse.score)
            }else{
                return PaymentResult("DENIED", fraudResponse.score)
            }
        }else{
            val response = pspClient.payWith(payment)
            return PaymentResult(Optional.of(Reference(response.reference)), response.result, 0)
        }

    }

    private fun isItNecessaryToEvaluateFraud(payment: Payment): Boolean {
        return payment.amount.isEqualOrGreaterThan100EUR()
    }
}