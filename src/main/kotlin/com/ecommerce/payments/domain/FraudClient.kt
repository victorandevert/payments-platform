package com.ecommerce.payments.domain

import arrow.core.Either

interface FraudClient {
    suspend fun evaluate(payment: Payment): Either<FraudError, FraudResponse>
}

class FraudResponse(val score: Int)
class FraudError
