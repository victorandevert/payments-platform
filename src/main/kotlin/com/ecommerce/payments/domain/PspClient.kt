package com.ecommerce.payments.domain

import arrow.core.Either


interface PspClient {
    fun payWith(payment: Payment): Either<PspError, PspResponse>
}

class PspResponse(val reference: Reference, val result: String)
class PspError
