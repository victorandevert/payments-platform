package com.ecommerce.payments.domain

import arrow.core.None
import arrow.core.Option


class PaymentResult private constructor(val reference: Option<Reference>, val result: String, val fraudScore: Int?){
    companion object {
        fun denied(fraudScore: Int? = null): PaymentResult {
            return PaymentResult(None,"DENIED", fraudScore)
        }
        fun accepted(pspResponse: PspResponse, fraudScore: Int? = null): PaymentResult {
            return PaymentResult(pspResponse.reference, pspResponse.result, fraudScore)
        }
    }
}
class Reference(val value: String)