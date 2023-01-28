package com.ecommerce.payments.domain

class Payment(val saleId: SaleId, val amount: Amount)


class SaleId (val value: String)
class Amount (val value: String, val currency: String)
