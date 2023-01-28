package com.ecommerce.payments.domain

interface PspClient {
    fun payWith(payment: Payment): Reference
}

class Reference(val value: String)