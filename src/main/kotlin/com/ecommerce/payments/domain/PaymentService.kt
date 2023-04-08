package com.ecommerce.payments.domain

class PaymentService(private val pspClient: PspClient, private val fraudClient: FraudClient) {

    suspend fun makeA(payment: Payment): PaymentResult {
        return when {
            payment.isItNecessaryToEvaluateFraud() -> fraudClient.evaluate(payment).fold(
                    {PaymentResult.denied()},
                    {fraud ->
                        if (fraud.score <= 5) {
                            return pspClient.payWith(payment).fold(
                                { PaymentResult.denied(fraud.score) },
                                { PaymentResult.accepted(it, fraud.score)})
                        } else return PaymentResult.denied(fraud.score)
                    })
                else -> return pspClient.payWith(payment).fold(
                    {  PaymentResult.denied() },
                    {  PaymentResult.accepted(it)})
        }
    }
}