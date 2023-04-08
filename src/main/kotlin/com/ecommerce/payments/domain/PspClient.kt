package com.ecommerce.payments.domain

import arrow.core.Either
import arrow.core.Option


interface PspClient {
    suspend fun payWith(payment: Payment): Either<PspError, PspResponse>
}

class PspResponse(val reference: Option<Reference>, val result: String)
class PspError
