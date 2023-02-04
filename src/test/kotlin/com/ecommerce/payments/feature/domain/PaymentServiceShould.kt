package com.ecommerce.payments.feature.domain

import arrow.core.Either
import arrow.core.Either.Right
import arrow.core.Some
import com.ecommerce.payments.domain.*
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class PaymentServiceShould {

    @Test
    fun `call PSP client to make a payment when fraud is below 6`() {
        val payment = Payment(SaleId("SALE123"), Amount.from("100000", "EUR"))
        val pspClient = mockk<PspClient> {
            every { payWith(payment) } returns Right(PspResponse(Some(Reference("12222-2222-222")), "ACCEPTED"))
        }
        val fraudClient = mockk<FraudClient> {
            every { evaluate(payment) } returns Right(FraudResponse(5))
        }

        val paymentService = PaymentService(pspClient, fraudClient)

        val response: PaymentResult = paymentService.makeA(payment)

        response.reference.map {
            assertThat(it.value).isEqualTo("12222-2222-222")
        }

    }

    @Test
    fun `don't make a payment when fraud is higher than 5`() {
        val payment = Payment(SaleId("SALE123"), Amount.from("100000", "EUR"))
        val pspClient = mockk<PspClient>(relaxed = true)
        val fraudClient = mockk<FraudClient> {
            every { evaluate(payment) } returns Right(FraudResponse(8))
        }

        val paymentService = PaymentService(pspClient, fraudClient)

        val response: PaymentResult = paymentService.makeA(payment)

        verify { listOf(pspClient) wasNot Called }
        assertThat(response.result).isEqualTo("DENIED")
    }

    @Test
    fun `don't evaluate fraud when amount is below 100 EUR`() {
        val payment = Payment(SaleId("SALE123"), Amount.from("20000", "EUR"))
        val pspClient = mockk<PspClient> {
            every { payWith(payment) } returns Right(PspResponse(Some(Reference("12222-2222-222")), "ACCEPTED"))
        }
        val fraudClient = mockk<FraudClient>(relaxed = true)

        val paymentService = PaymentService(pspClient, fraudClient)

        val response: PaymentResult = paymentService.makeA(payment)

        verify { listOf(fraudClient) wasNot Called }
        assertThat(response.result).isEqualTo("ACCEPTED")
    }
}