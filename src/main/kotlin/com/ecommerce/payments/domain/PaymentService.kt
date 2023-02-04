package com.ecommerce.payments.domain

class PaymentService(private val pspClient: PspClient, private val fraudClient: FraudClient) {

    fun makeA(payment: Payment): PaymentResult {
        if (payment.isItNecessaryToEvaluateFraud()) {
            return fraudClient.evaluate(payment).fold(
                { PaymentResult("DENIED", -1) },
                {fraud ->
                    if (fraud.score <= 5) {
                        return pspClient.payWith(payment).fold(
                            { PaymentResult("DENIED", fraud.score) },
                            { PaymentResult(it.reference, it.result, fraud.score)})
                    } else PaymentResult("DENIED", fraud.score)
                })
        } else {
            return pspClient.payWith(payment).fold(
                {  PaymentResult("DENIED", -1) },
                {  PaymentResult(it.reference, it.result, 0)})
        }
    }
}