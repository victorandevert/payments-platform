package com.ecommerce.payments.domain

import java.math.BigDecimal

class Payment(val saleId: SaleId, val amount: Amount){
    fun isItNecessaryToEvaluateFraud() = this.amount.isItNecessaryToEvaluateFraud()
}


class SaleId(val value: String)
class Amount private constructor(private val value: BigDecimal, private val currency: String) {
    companion object {
        fun from(value: String, currency: String): Amount {
            return Amount(BigDecimal(value).movePointLeft(3).stripTrailingZeros(), currency)
        }
    }

    fun toThreeExponentRepresentation(): String {
        return value.movePointRight(3).stripTrailingZeros().toPlainString()
    }

    private fun isEqualOrGreaterThan100EUR(): Boolean = when (BigDecimal(100).compareTo(value)) {
        1 -> false
        else -> true
    }
    private fun isEqualOrGreaterThan50USD(): Boolean = when (BigDecimal(50).compareTo(value)) {
        1 -> false
        else -> true
    }
    private fun isEqualOrGreaterThan150GBP(): Boolean = when (BigDecimal(150).compareTo(value)) {
        1 -> false
        else -> true
    }
    fun isItNecessaryToEvaluateFraud(): Boolean{
        return when(currency){
            "EUR"-> isEqualOrGreaterThan100EUR()
            "USD"-> isEqualOrGreaterThan50USD()
            "GBP"-> isEqualOrGreaterThan150GBP()
            else -> true
        }
    }

    fun currentCurrency(): String {
        return currency
    }
}
