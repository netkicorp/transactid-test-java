package com.netki.transactidlibraryjavademo.service

import com.netki.TransactId
import com.netki.model.*
import com.netki.transactidlibraryjavademo.repo.TransactIdRepository
import com.netki.transactidlibraryjavademo.util.TestData
import lombok.extern.slf4j.Slf4j
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.security.Security
import java.sql.Timestamp


@Service
@Slf4j
class TransactIdService {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    val ownerParameters = listOf(
        OwnerParameters(
            isPrimaryForTransaction = true,
            pkiDataParametersSets = listOf(
                PkiDataParameters(
                    attestation = "identification",
                    privateKeyPem = TestData.CLIENT_PRIVATE_KEY,
                    certificatePem = TestData.CLIENT_CERTIFICATE,
                    type = PkiType.X509SHA256
                )
            )
        )
    )

    val senderParameters = SenderParameters(
        PkiDataParameters(
            attestation = "identification",
            privateKeyPem = TestData.SENDER_PRIVATE_KEY,
            certificatePem = TestData.SENDER_CERTIFICATE,
            type = PkiType.X509SHA256
        )
    )

    fun getInitialInvoiceRequest(): ByteArray = createTestInvoice()

    private fun createTestInvoice(): ByteArray {
        val invoiceRequestParameters = InvoiceRequestParameters(
            amount = 1000,
            memo = "Payment for service",
            notificationUrl = "www.notification.com/notify"
        )

        logger.info("Creating InvoiceRequest")
        return TransactId.createInvoiceRequest(invoiceRequestParameters, ownerParameters, senderParameters)
    }

    fun postInvoiceRequest(invoiceRequest: ByteArray): ByteArray {
        logger.info("InvoiceRequest received")
        logger.info("InvoiceRequest valid? ${TransactId.isInvoiceRequestValid(invoiceRequest)}")
        val invoiceRequestModel = TransactId.parseInvoiceRequest(invoiceRequest)
        logger.info("InvoiceRequest parsed: $invoiceRequestModel")
        val paymentDetails = PaymentParameters(
            network = "main",
            outputs = arrayListOf(
                Output(100, "Script"),
                Output(200, "Script")
            ),
            time = Timestamp(System.currentTimeMillis()),
            expires = Timestamp(System.currentTimeMillis() + 1000),
            memo = invoiceRequestModel.memo,
            paymentUrl = invoiceRequestModel.notificationUrl,
            merchantData = "Merchant data"
        )

        logger.info("Creating PaymentRequest")
        return TransactId.createPaymentRequest(paymentDetails, ownerParameters, senderParameters, 1)
    }

    fun postPaymentRequest(paymentRequest: ByteArray): ByteArray {
        logger.info("PaymentRequest received")
        logger.info("PaymentRequest valid? ${TransactId.isPaymentRequestValid(paymentRequest)}")
        val paymentRequestModel = TransactId.parsePaymentRequest(paymentRequest)
        logger.info("PaymentRequest parsed: $paymentRequestModel")
        val paymentDetails = paymentRequestModel.paymentParameters
        val paymentModel = Payment(
            merchantData = paymentDetails.merchantData,
            transactions = arrayListOf(
                "transaction1".toByteArray(),
                "transaction2".toByteArray()
            ),
            outputs = paymentDetails.outputs,
            memo = paymentDetails.memo
        )

        logger.info("Creating Payment")
        return TransactId.createPayment(paymentModel)
    }

    fun postPayment(payment: ByteArray): ByteArray {
        logger.info("Payment received")
        logger.info("Payment valid? ${TransactId.isPaymentValid(payment)}")
        val paymentModel = TransactId.parsePayment(payment)
        logger.info("Payment parsed: $paymentModel")

        logger.info("Creating PaymentAck")
        return TransactId.createPaymentAck(paymentModel, "Payment successful")
    }
}
