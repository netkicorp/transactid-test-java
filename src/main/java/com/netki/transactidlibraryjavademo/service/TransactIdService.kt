package com.netki.transactidlibraryjavademo.service

import com.netki.TransactId
import com.netki.model.*
import com.netki.transactidlibraryjavademo.model.EncryptionKeys
import com.netki.transactidlibraryjavademo.model.Information
import com.netki.transactidlibraryjavademo.repo.TransactIdRepository
import com.netki.transactidlibraryjavademo.util.Certificates.VASP_CERTIFICATE
import com.netki.transactidlibraryjavademo.util.TestData.Attestations.ATTESTATIONS_REQUESTED
import com.netki.transactidlibraryjavademo.util.TestData.Beneficiaries.NO_PRIMARY_BENEFICIARY_PKI_X509SHA256
import com.netki.transactidlibraryjavademo.util.TestData.Beneficiaries.PRIMARY_BENEFICIARY_PKI_X509SHA256
import com.netki.transactidlibraryjavademo.util.TestData.Originators.NO_PRIMARY_ORIGINATOR_PKI_X509SHA256
import com.netki.transactidlibraryjavademo.util.TestData.Originators.PRIMARY_ORIGINATOR_PKI_X509SHA256
import com.netki.transactidlibraryjavademo.util.TestData.Output.OUTPUTS
import com.netki.transactidlibraryjavademo.util.TestData.Payment.PAYMENT
import com.netki.transactidlibraryjavademo.util.TestData.PkiData.PKI_DATA_SENDER_X509SHA256
import com.netki.transactidlibraryjavademo.util.TestData.Senders.SENDER_PKI_X509SHA256
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import javax.annotation.PostConstruct

/**
 * Make sure to change this URLs to test the async flow if needed.
 */
const val NOTIFICATION_URL = "http://localhost:8080/async/payment-request"
const val PAYMENT_URL = "http://localhost:8080/async/payment"

@Service
class TransactIdService {

    @Autowired
    private lateinit var encryptionKeys: EncryptionKeys

    @Autowired
    private lateinit var transactIdRepository: TransactIdRepository

    private lateinit var recipientParameters: RecipientParameters
    private lateinit var senderParameters: SenderParameters

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private var transactId = TransactId.getInstance("src/main/resources/certificates")
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

    fun getVaspCertificate() = Information(VASP_CERTIFICATE)

    fun sendProtocolMessage(addressId: String, protocolMessage: ByteArray): ByteArray? {
        val protocolMessageMetadata = transactId.getProtocolMessageMetadata(protocolMessage)
        val isEncrypted = protocolMessageMetadata.encrypted
        val messageType = protocolMessageMetadata.messageType
        val identifier = protocolMessageMetadata.identifier
        return when (messageType) {
            MessageType.INVOICE_REQUEST -> createPaymentRequest(isEncrypted, identifier)
            MessageType.PAYMENT_REQUEST -> createPayment(protocolMessage)
            MessageType.PAYMENT -> createPaymentAck(isEncrypted, identifier)
            MessageType.PAYMENT_ACK -> null
            MessageType.UNKNOWN_MESSAGE_TYPE -> throw IllegalArgumentException("UNKNOWN_MESSAGE_TYPE")
        }
    }

    fun sendProtocolMessageAsync(addressId: String, protocolMessage: ByteArray): ByteArray? {
        val protocolMessageMetadata = transactId.getProtocolMessageMetadata(protocolMessage)
        val isEncrypted = protocolMessageMetadata.encrypted
        val messageType = protocolMessageMetadata.messageType
        val identifier = protocolMessageMetadata.identifier
        return when (messageType) {
            MessageType.INVOICE_REQUEST -> {
                postInvoiceRequestAsync(protocolMessage)
                null
            }
            MessageType.PAYMENT_REQUEST -> {
                postPaymentRequestAsync(protocolMessage)
                null
            }
            MessageType.PAYMENT -> createPaymentAck(isEncrypted, identifier)
            MessageType.PAYMENT_ACK -> null
            MessageType.UNKNOWN_MESSAGE_TYPE -> throw IllegalArgumentException("UNKNOWN_MESSAGE_TYPE")
        }
    }

    fun sendInitialInvoiceRequest(url: String) {
        val invoiceRequest = getInitialInvoiceRequest()
        transactIdRepository.sendRequestNoResponse(invoiceRequest, url)
    }

    fun sendInitialInvoiceRequestEncrypted(url: String) {
        val invoiceRequestEncrypted = getInitialInvoiceRequestEncrypted()
        transactIdRepository.sendRequestNoResponse(invoiceRequestEncrypted, url)
    }

    fun getInitialInvoiceRequest(): ByteArray {
        logger.info("Creating InvoiceRequest...")
        val originators = listOf(
            PRIMARY_ORIGINATOR_PKI_X509SHA256,
            NO_PRIMARY_ORIGINATOR_PKI_X509SHA256
        )
        val beneficiaries = listOf(
            PRIMARY_BENEFICIARY_PKI_X509SHA256
        )
        val sender = SENDER_PKI_X509SHA256
        val invoiceRequestParameters = InvoiceRequestParameters(
            amount = 1000,
            memo = "InvoiceRequest Memo",
            notificationUrl = NOTIFICATION_URL,
            originatorsAddresses = OUTPUTS,
            originatorParameters = originators,
            beneficiaryParameters = beneficiaries,
            senderParameters = sender,
            attestationsRequested = ATTESTATIONS_REQUESTED
        )

        val invoiceRequest = transactId.createInvoiceRequest(invoiceRequestParameters)
        logger.info("Returning InvoiceRequest...")
        return invoiceRequest
    }

    fun getInitialInvoiceRequestEncrypted(): ByteArray {
        logger.info("Creating InvoiceRequest encrypted...")
        val originators = listOf(
            PRIMARY_ORIGINATOR_PKI_X509SHA256,
            NO_PRIMARY_ORIGINATOR_PKI_X509SHA256
        )
        val beneficiaries = listOf(
            PRIMARY_BENEFICIARY_PKI_X509SHA256
        )
        val invoiceRequestParameters = InvoiceRequestParameters(
            amount = 1000,
            memo = "InvoiceRequest Encrypted Memo",
            notificationUrl = NOTIFICATION_URL,
            originatorsAddresses = OUTPUTS,
            originatorParameters = originators,
            beneficiaryParameters = beneficiaries,
            senderParameters = senderParameters,
            attestationsRequested = ATTESTATIONS_REQUESTED,
            recipientParameters = recipientParameters,
            messageInformation = messageInformationEncrypted
        )

        val invoiceRequest = transactId.createInvoiceRequest(invoiceRequestParameters)
        logger.info("Returning InvoiceRequest...")
        return invoiceRequest
    }

    fun postInvoiceRequest(invoiceRequest: ByteArray): ByteArray {
        logger.info("InvoiceRequest received")
        logger.info(
            "InvoiceRequest valid? ${
                transactId.isInvoiceRequestValid(
                    invoiceRequest,
                    recipientParameters
                )
            }"
        )
        val invoiceRequestModel =
            transactId.parseInvoiceRequest(invoiceRequest, recipientParameters)
        logger.info("InvoiceRequest parsed: $invoiceRequestModel")
        return createPaymentRequest(
            invoiceRequestModel.protocolMessageMetadata.encrypted,
            invoiceRequestModel.protocolMessageMetadata.identifier
        )
    }

    fun postInvoiceRequestAsync(invoiceRequest: ByteArray) {
        logger.info("InvoiceRequest received")
        logger.info(
            "InvoiceRequest valid? ${
                transactId.isInvoiceRequestValid(
                    invoiceRequest,
                    recipientParameters
                )
            }"
        )
        val invoiceRequestModel =
            transactId.parseInvoiceRequest(invoiceRequest, recipientParameters)
        logger.info("InvoiceRequest parsed: $invoiceRequestModel")

        val notificationUrl = invoiceRequestModel.notificationUrl
        if (notificationUrl == null || notificationUrl.isNullOrBlank() || notificationUrl.isNullOrEmpty()) {
            throw IllegalArgumentException("Missing notificationUrl to send the async response")
        } else {
            val paymentRequest =
                createPaymentRequest(
                    invoiceRequestModel.protocolMessageMetadata.encrypted,
                    invoiceRequestModel.protocolMessageMetadata.identifier
                )
            transactIdRepository.sendRequestNoResponse(paymentRequest, notificationUrl)
        }
    }

    private fun createPaymentRequest(encrypted: Boolean, identifier: String): ByteArray {
        return if (encrypted) {
            logger.info("Creating PaymentRequest Encrypted...")
            logger.info("Returning PaymentRequest Encrypted...")
            val beneficiaries = listOf(
                PRIMARY_BENEFICIARY_PKI_X509SHA256,
                NO_PRIMARY_BENEFICIARY_PKI_X509SHA256
            )
            val paymentRequestParameters = PaymentRequestParameters(
                network = "main",
                beneficiariesAddresses = OUTPUTS,
                time = Timestamp(System.currentTimeMillis()),
                expires = Timestamp(System.currentTimeMillis()),
                memo = "PaymentRequest Encrypted Memo",
                paymentUrl = PAYMENT_URL,
                merchantData = "merchant data",
                beneficiaryParameters = beneficiaries,
                senderParameters = senderParameters,
                attestationsRequested = ATTESTATIONS_REQUESTED,
                messageInformation = messageInformationEncrypted,
                recipientParameters = recipientParameters
            )
            transactId.createPaymentRequest(paymentRequestParameters, identifier)
        } else {
            logger.info("Creating PaymentRequest...")
            logger.info("Returning PaymentRequest...")
            val beneficiaries = listOf(
                PRIMARY_BENEFICIARY_PKI_X509SHA256,
                NO_PRIMARY_BENEFICIARY_PKI_X509SHA256
            )
            val sender = SENDER_PKI_X509SHA256
            val paymentRequestParameters = PaymentRequestParameters(
                network = "main",
                beneficiariesAddresses = OUTPUTS,
                time = Timestamp(System.currentTimeMillis()),
                expires = Timestamp(System.currentTimeMillis()),
                memo = "PaymentRequest Memo",
                paymentUrl = PAYMENT_URL,
                merchantData = "merchant data",
                beneficiaryParameters = beneficiaries,
                senderParameters = sender,
                attestationsRequested = ATTESTATIONS_REQUESTED
            )
            transactId.createPaymentRequest(paymentRequestParameters, identifier)
        }
    }

    fun postPaymentRequest(paymentRequest: ByteArray): ByteArray {
        logger.info("PaymentRequest received")
        logger.info(
            "PaymentRequest valid? ${
                transactId.isPaymentRequestValid(
                    paymentRequest,
                    recipientParameters
                )
            }"
        )
        return createPayment(paymentRequest)
    }

    fun postPaymentRequestAsync(paymentRequest: ByteArray) {
        logger.info("PaymentRequest received")
        logger.info(
            "PaymentRequest valid? ${
                transactId.isPaymentRequestValid(
                    paymentRequest,
                    recipientParameters
                )
            }"
        )
        val paymentRequestModel =
            transactId.parsePaymentRequest(paymentRequest, recipientParameters)

        val paymentUrl = paymentRequestModel.paymentUrl
        if (paymentUrl == null || paymentUrl.isNullOrBlank() || paymentUrl.isNullOrEmpty()) {
            throw IllegalArgumentException("Missing paymentUrl to send the async response")
        } else {
            val payment = createPayment(paymentRequest)
            transactIdRepository.sendRequestNoResponse(payment, paymentUrl)
        }
    }

    fun createPayment(paymentRequest: ByteArray): ByteArray {
        val paymentRequestModel =
            transactId.parsePaymentRequest(paymentRequest, recipientParameters)
        logger.info("PaymentRequest parsed: $paymentRequestModel")

        return if (paymentRequestModel.protocolMessageMetadata.encrypted) {
            logger.info("Creating Payment Encrypted...")
            logger.info("Returning Payment Encrypted...")
            val originators = listOf(
                PRIMARY_ORIGINATOR_PKI_X509SHA256,
                NO_PRIMARY_ORIGINATOR_PKI_X509SHA256
            )
            val beneficiaries = listOf(
                PRIMARY_BENEFICIARY_PKI_X509SHA256
            )
            val paymentParameters = PaymentParameters(
                merchantData = "merchant data",
                transactions = arrayListOf(
                    "transaction1".toByteArray(),
                    "transaction2".toByteArray()
                ),
                outputs = OUTPUTS,
                memo = "Payment Encrypted Memo",
                originatorParameters = originators,
                beneficiaryParameters = beneficiaries,
                messageInformation = messageInformationEncrypted,
                senderParameters = senderParameters,
                recipientParameters = recipientParameters
            )
            transactId.createPayment(
                paymentParameters,
                paymentRequestModel.protocolMessageMetadata.identifier
            )

        } else {
            logger.info("Creating Payment...")
            logger.info("Returning Payment...")
            val originators = listOf(
                PRIMARY_ORIGINATOR_PKI_X509SHA256,
                NO_PRIMARY_ORIGINATOR_PKI_X509SHA256
            )
            val beneficiaries = listOf(
                PRIMARY_BENEFICIARY_PKI_X509SHA256
            )
            val paymentParameters = PaymentParameters(
                merchantData = "merchant data",
                transactions = arrayListOf(
                    "transaction1".toByteArray(),
                    "transaction2".toByteArray()
                ),
                outputs = OUTPUTS,
                memo = "Payment Memo",
                originatorParameters = originators,
                beneficiaryParameters = beneficiaries
            )
            transactId.createPayment(
                paymentParameters,
                paymentRequestModel.protocolMessageMetadata.identifier
            )
        }
    }

    fun postPayment(payment: ByteArray): ByteArray {
        logger.info("Payment received")
        logger.info("Payment valid? ${transactId.isPaymentValid(payment, recipientParameters)}")
        val paymentModel = transactId.parsePayment(payment, recipientParameters)
        logger.info("Payment parsed: $paymentModel")

        return createPaymentAck(
            paymentModel.protocolMessageMetadata!!.encrypted,
            paymentModel.protocolMessageMetadata!!.identifier
        )
    }

    private fun createPaymentAck(encrypted: Boolean, identifier: String): ByteArray {
        return if (encrypted) {
            logger.info("Creating PaymentAck Encrypted...")
            logger.info("Returning PaymentAck Encrypted...")
            val paymentAckParameters = PaymentAckParameters(
                payment = PAYMENT,
                memo = "PaymentAck Encrypted Memo",
                messageInformation = messageInformationEncrypted,
                senderParameters = senderParameters,
                recipientParameters = recipientParameters
            )
            transactId.createPaymentAck(paymentAckParameters, identifier)
        } else {
            logger.info("Creating PaymentAck...")
            logger.info("Returning PaymentAck...")
            val paymentAckParameters = PaymentAckParameters(
                payment = PAYMENT,
                memo = "PaymentAck Memo"
            )
            transactId.createPaymentAck(paymentAckParameters, identifier)
        }
    }
}
