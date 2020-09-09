package com.netki.transactidlibraryjavademo.service

import com.netki.TransactId
import com.netki.transactidlibraryjavademo.util.TestData.Attestations.ATTESTATIONS_REQUESTED
import com.netki.transactidlibraryjavademo.util.TestData.InvoiceRequest.INVOICE_REQUEST_DATA
import com.netki.transactidlibraryjavademo.util.TestData.Owners.NO_PRIMARY_OWNER_PKI_X509SHA256
import com.netki.transactidlibraryjavademo.util.TestData.Owners.PRIMARY_OWNER_PKI_X509SHA256
import com.netki.transactidlibraryjavademo.util.TestData.Payment.PAYMENT_PARAMETERS
import com.netki.transactidlibraryjavademo.util.TestData.PaymentRequest.PAYMENT_REQUEST_PARAMETERS
import com.netki.transactidlibraryjavademo.util.TestData.Senders.SENDER_PKI_X509SHA256
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TransactIdService {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private var transactId = TransactId.getInstance("src/main/resources/certificates")
    val ownerParameters = listOf(PRIMARY_OWNER_PKI_X509SHA256, NO_PRIMARY_OWNER_PKI_X509SHA256)

    fun getInitialInvoiceRequest(): ByteArray {
        logger.info("Creating InvoiceRequest...")
        val invoiceRequest = transactId.createInvoiceRequest(
            INVOICE_REQUEST_DATA,
            ownerParameters,
            SENDER_PKI_X509SHA256,
            ATTESTATIONS_REQUESTED
        )
        logger.info("Returning InvoiceRequest...")
        return invoiceRequest
    }

    fun postInvoiceRequest(invoiceRequest: ByteArray): ByteArray {
        logger.info("InvoiceRequest received")
        logger.info("InvoiceRequest valid? ${transactId.isInvoiceRequestValid(invoiceRequest)}")
        val invoiceRequestModel = transactId.parseInvoiceRequest(invoiceRequest)
        logger.info("InvoiceRequest parsed: $invoiceRequestModel")

        logger.info("Creating PaymentRequest...")
        val paymentRequest = transactId.createPaymentRequest(
            PAYMENT_REQUEST_PARAMETERS,
            ownerParameters,
            SENDER_PKI_X509SHA256,
            ATTESTATIONS_REQUESTED,
            1
        )
        logger.info("Returning PaymentRequest...")
        return paymentRequest
    }

    fun postPaymentRequest(paymentRequest: ByteArray): ByteArray {
        logger.info("PaymentRequest received")
        logger.info("PaymentRequest valid? ${transactId.isPaymentRequestValid(paymentRequest)}")
        val paymentRequestModel = transactId.parsePaymentRequest(paymentRequest)
        logger.info("PaymentRequest parsed: $paymentRequestModel")

        logger.info("Creating Payment...")
        val payment = transactId.createPayment(PAYMENT_PARAMETERS, ownerParameters)
        logger.info("Returning Payment...")
        return payment
    }

    fun postPayment(payment: ByteArray): ByteArray {
        logger.info("Payment received")
        logger.info("Payment valid? ${transactId.isPaymentValid(payment)}")
        val paymentModel = transactId.parsePayment(payment)
        logger.info("Payment parsed: $paymentModel")

        logger.info("Creating PaymentAck...")
        val paymentAck = transactId.createPaymentAck(paymentModel, "Payment successful")
        logger.info("Returning PaymentAck...")
        return paymentAck
    }
}
