package com.netki.transactidlibraryjavademo.service

import com.netki.TransactId
import com.netki.model.*
import com.netki.security.CryptoModule
import com.netki.transactidlibraryjavademo.repo.TransactIdRepository
import com.netki.transactidlibraryjavademo.util.KeyGenerator

import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
@Slf4j
class TransactIdService {

    val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var transactIdRepository: TransactIdRepository

    val keyPair = KeyGenerator.Keys.generateKeyPair()
    val certificate = KeyGenerator.Keys.generateCertificate(keyPair, KeyGenerator.Keys.HASH_ALGORITHM, "test")
    val keyPairPemX509 = KeyPairPem(
        CryptoModule.objectToPrivateKeyPem(keyPair.private),
        CryptoModule.objectToCertificatePem(certificate),
        PkiType.X509SHA256
    )

    fun getInitialInvoiceRequest(): ByteArray = createTestInvoice()

    fun postInitialInvoiceRequest(url: String) {
        val invoiceRequest = createTestInvoice()
        transactIdRepository.postInvoiceRequest(invoiceRequest, url)
    }

    private fun createTestInvoice(): ByteArray {
        val invoiceRequestParameters = InvoiceRequestParameters(
            amount = 1000,
            memo = "Invoice request for service",
            notificationUrl = "www.notification.com/notify"
        )

        return TransactId.createInvoiceRequest(invoiceRequestParameters, keyPairPemX509)
    }

    fun postInvoiceRequest(invoiceRequest: ByteArray): ByteArray {
        require(TransactId.isInvoiceRequestValid(invoiceRequest)) { "Invoice request not valid" }
        val invoiceRequestModel = TransactId.parseInvoiceRequest(invoiceRequest)
        val paymentDetails = PaymentDetails(
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

        return TransactId.createPaymentRequest(paymentDetails, keyPairPemX509, 1)
    }

    fun postPaymentRequest(paymentRequest: ByteArray): ByteArray {
        require(TransactId.isPaymentRequestValid(paymentRequest)) { "Payment request not valid" }
        val paymentRequestModel = TransactId.parsePaymentRequest(paymentRequest)
        val paymentDetails = paymentRequestModel.paymentDetails
        val paymentModel = Payment(
            merchantData = paymentDetails.merchantData,
            transactions = arrayListOf(
                "transaction1".toByteArray(),
                "transaction2".toByteArray()
            ),
            outputs = paymentDetails.outputs,
            memo = paymentDetails.memo
        )

        return TransactId.createPayment(paymentModel)
    }

    fun postPayment(payment: ByteArray): ByteArray {
        require(TransactId.isPaymentValid(payment)) { "Payment not valid" }
        val paymentModel = TransactId.parsePayment(payment)

        return TransactId.createPaymentAck(paymentModel, "Payment successful")
    }
}
