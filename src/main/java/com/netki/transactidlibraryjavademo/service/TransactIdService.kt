package com.netki.transactidlibraryjavademo.service

import com.netki.TransactId
import com.netki.model.EncryptionParameters
import com.netki.model.MessageInformation
import com.netki.model.RecipientParameters
import com.netki.model.SenderParameters
import com.netki.transactidlibraryjavademo.model.EncryptionKeys
import com.netki.transactidlibraryjavademo.util.TestData.Attestations.ATTESTATIONS_REQUESTED
import com.netki.transactidlibraryjavademo.util.TestData.InvoiceRequest.INVOICE_REQUEST_DATA
import com.netki.transactidlibraryjavademo.util.TestData.Owners.NO_PRIMARY_OWNER_PKI_X509SHA256
import com.netki.transactidlibraryjavademo.util.TestData.Owners.PRIMARY_OWNER_PKI_X509SHA256
import com.netki.transactidlibraryjavademo.util.TestData.Payment.PAYMENT_PARAMETERS
import com.netki.transactidlibraryjavademo.util.TestData.PaymentRequest.PAYMENT_REQUEST_PARAMETERS
import com.netki.transactidlibraryjavademo.util.TestData.PkiData.PKI_DATA_SENDER_X509SHA256
import com.netki.transactidlibraryjavademo.util.TestData.Senders.SENDER_PKI_X509SHA256
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class TransactIdService {

    @Autowired
    private lateinit var encryptionKeys: EncryptionKeys
    private lateinit var recipientParameters: RecipientParameters
    private lateinit var senderParameters: SenderParameters

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private var transactId = TransactId.getInstance("src/main/resources/certificates")
    val ownerParameters = listOf(PRIMARY_OWNER_PKI_X509SHA256, NO_PRIMARY_OWNER_PKI_X509SHA256)
    val messageInformationEncrypted = MessageInformation(
        encryptMessage = true
    )

    @PostConstruct
    fun setUp() {
        recipientParameters = RecipientParameters(
            "VASP_1",
            "1234567890ABCD",
            EncryptionParameters(
                encryptionKeys.recipientPrivateKey,
                encryptionKeys.recipientPublicKey
            )
        )
        senderParameters = SenderParameters(
            PKI_DATA_SENDER_X509SHA256,
            EncryptionParameters(
                encryptionKeys.senderPrivateKey,
                encryptionKeys.senderPublicKey
            )
        )
    }

    fun getEncryptionKeys() = encryptionKeys

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

    fun getInitialInvoiceRequestEncrypted(): ByteArray {
        logger.info("Creating InvoiceRequest encrypted...")
        val invoiceRequest = transactId.createInvoiceRequest(
            INVOICE_REQUEST_DATA,
            ownerParameters,
            senderParameters,
            ATTESTATIONS_REQUESTED,
            recipientParameters,
            messageInformationEncrypted
        )
        logger.info("Returning InvoiceRequest...")
        return invoiceRequest
    }

    fun postInvoiceRequest(invoiceRequest: ByteArray): ByteArray {
        logger.info("InvoiceRequest received")
        logger.info(
            "InvoiceRequest valid? ${transactId.isInvoiceRequestValid(
                invoiceRequest,
                recipientParameters
            )}"
        )
        val invoiceRequestModel =
            transactId.parseInvoiceRequest(invoiceRequest, recipientParameters)
        logger.info("InvoiceRequest parsed: $invoiceRequestModel")

        return if (invoiceRequestModel.protocolMessageMetadata.encrypted) {
            logger.info("Creating PaymentRequest Encrypted...")
            logger.info("Returning PaymentRequest Encrypted...")
            transactId.createPaymentRequest(
                PAYMENT_REQUEST_PARAMETERS,
                ownerParameters,
                senderParameters,
                ATTESTATIONS_REQUESTED,
                1,
                messageInformationEncrypted,
                recipientParameters
            )
        } else {
            logger.info("Creating PaymentRequest...")
            logger.info("Returning PaymentRequest...")
            transactId.createPaymentRequest(
                PAYMENT_REQUEST_PARAMETERS,
                ownerParameters,
                SENDER_PKI_X509SHA256,
                ATTESTATIONS_REQUESTED,
                1
            )
        }
    }

    fun postPaymentRequest(paymentRequest: ByteArray): ByteArray {
        logger.info("PaymentRequest received")
        logger.info(
            "PaymentRequest valid? ${transactId.isPaymentRequestValid(
                paymentRequest,
                recipientParameters
            )}"
        )
        val paymentRequestModel =
            transactId.parsePaymentRequest(paymentRequest, recipientParameters)
        logger.info("PaymentRequest parsed: $paymentRequestModel")

        return if (paymentRequestModel.protocolMessageMetadata.encrypted) {
            logger.info("Creating Payment Encrypted...")
            logger.info("Returning Payment Encrypted...")
            transactId.createPayment(
                PAYMENT_PARAMETERS,
                ownerParameters,
                messageInformationEncrypted,
                senderParameters,
                recipientParameters
            )

        } else {
            logger.info("Creating Payment...")
            logger.info("Returning Payment...")
            transactId.createPayment(PAYMENT_PARAMETERS, ownerParameters)
        }
    }

    fun postPayment(payment: ByteArray): ByteArray {
        logger.info("Payment received")
        logger.info("Payment valid? ${transactId.isPaymentValid(payment, recipientParameters)}")
        val paymentModel = transactId.parsePayment(payment, recipientParameters)
        logger.info("Payment parsed: $paymentModel")

        return if (paymentModel.protocolMessageMetadata!!.encrypted) {
            logger.info("Creating PaymentAck Encrypted...")
            logger.info("Returning PaymentAck Encrypted...")
            transactId.createPaymentAck(
                paymentModel,
                "Payment successful",
                messageInformationEncrypted,
                senderParameters,
                recipientParameters
            )
        } else {
            logger.info("Creating PaymentAck...")
            logger.info("Returning PaymentAck...")
            transactId.createPaymentAck(paymentModel, "Payment successful")
        }
    }
}
