package com.ecommerce.payments.feature.domain

import com.ecommerce.payments.domain.*
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class PaymentServiceShould {

    @Test
    fun `call a PSP to make a payment`() {
        val payment = Payment(SaleId("SALE123"), Amount("100000", "EUR"))
        val pspClient = mockk<PspClient> {
            every { payWith(payment) } returns Reference("12222-2222-222")
        }

        val paymentService = PaymentService(pspClient)

        val response: PaymentResult = paymentService.makeA(payment)

        assertThat(response.reference.value).isEqualTo("12222-2222-222")
    }
}