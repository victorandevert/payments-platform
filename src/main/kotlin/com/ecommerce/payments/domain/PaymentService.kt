package com.ecommerce.payments.domain

import java.util.*

class PaymentService(private val pspClient: PspClient, private val fraudClient: FraudClient) {

    fun makeA(payment: Payment): PaymentResult {
        if (isItNecessaryToEvaluateFraud(payment)) {
            return fraudClient.evaluate(payment).fold(
                { PaymentResult("DENIED", -1) },
                {fraud ->
                    if (fraud.score <= 5) {
                        return pspClient.payWith(payment).fold(
                            { PaymentResult("DENIED", fraud.score) },
                            { PaymentResult(Optional.of(Reference(it.reference.value)), it.result, fraud.score)})
                    } else PaymentResult("DENIED", fraud.score)
                })
        } else {
            return pspClient.payWith(payment).fold(
                {  PaymentResult("DENIED", -1) },
                {  PaymentResult(Optional.of(Reference(it.reference.value)), it.result, 0)})
        }
    }

    private fun isItNecessaryToEvaluateFraud(payment: Payment): Boolean {
        return payment.amount.isEqualOrGreaterThan100EUR()
    }
}