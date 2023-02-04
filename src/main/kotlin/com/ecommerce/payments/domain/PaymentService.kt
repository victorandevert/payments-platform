package com.ecommerce.payments.domain

class PaymentService(private val pspClient: PspClient, private val fraudClient: FraudClient) {

    fun makeA(payment: Payment): PaymentResult {
        if (payment.isItNecessaryToEvaluateFraud()) {
            return fraudClient.evaluate(payment).fold(
                { PaymentResult.denied()},
                {fraud ->
                    if (fraud.score <= 5) {
                        return pspClient.payWith(payment).fold(
                            { PaymentResult.denied(fraud.score) },
                            { PaymentResult.accepted(it, fraud.score)})
                    } else PaymentResult.denied(fraud.score)
                })
        } else {
            return pspClient.payWith(payment).fold(
                {  PaymentResult.denied() },
                {  PaymentResult.accepted(it)})
        }
    }
}