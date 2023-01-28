package com.ecommerce.payments.domain

interface PspClient {
    fun payWith(payment: Payment): PspResponse
}

class PspResponse(val reference: String, val result: String)
