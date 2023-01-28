package com.ecommerce.payments.domain

interface FraudClient {
    fun evaluate(payment: Payment): FraudResponse
}

class FraudResponse(val score: Int)