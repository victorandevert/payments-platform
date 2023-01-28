package com.ecommerce.payments.infrastructure

import kotlinx.serialization.Serializable

@Serializable
data class PaymentDTO(val saleId: String, val amount: String, val currency: String)