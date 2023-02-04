package com.ecommerce.payments.domain

import arrow.core.None
import arrow.core.Option


class PaymentResult(val reference: Option<Reference>, val result: String, val fraudScore: Int){
    constructor(result: String, fraudScore: Int): this(None, result, fraudScore)
}
class Reference(val value: String)