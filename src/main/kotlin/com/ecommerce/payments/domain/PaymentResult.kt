package com.ecommerce.payments.domain

class PaymentResult(val reference: Reference, val result: String, val fraudScore: Int)