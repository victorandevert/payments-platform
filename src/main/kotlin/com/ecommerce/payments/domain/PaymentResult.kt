package com.ecommerce.payments.domain

import java.util.*

class PaymentResult(val reference: Optional<Reference>, val result: String, val fraudScore: Int){
    constructor(result: String, fraudScore: Int): this(Optional.empty(), result, fraudScore)
}
class Reference(val value: String)