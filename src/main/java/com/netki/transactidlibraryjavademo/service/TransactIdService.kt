package com.netki.transactidlibraryjavademo.service

import com.netki.TransactId
import com.netki.model.*
import com.netki.transactidlibraryjavademo.model.EncryptionKeys
import com.netki.transactidlibraryjavademo.util.TestData.Attestations.ATTESTATIONS_REQUESTED
import com.netki.transactidlibraryjavademo.util.TestData.Beneficiaries.NO_PRIMARY_BENEFICIARY_PKI_NONE
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

@Service
class TransactIdService {

    @Autowired
    private lateinit var encryptionKeys: EncryptionKeys
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
            memo = "memo",
            notificationUrl = "notificationUrl",
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
            memo = "memo",
            notificationUrl = "notificationUrl",
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
            val beneficiaries = listOf(
                PRIMARY_BENEFICIARY_PKI_X509SHA256,
                NO_PRIMARY_BENEFICIARY_PKI_X509SHA256
            )
            val paymentRequestParameters = PaymentRequestParameters(
                network = "main",
                beneficiariesAddresses = OUTPUTS,
                time = Timestamp(System.currentTimeMillis()),
                expires = Timestamp(System.currentTimeMillis()),
                memo = "memo",
                paymentUrl = "www.payment.url/test",
                merchantData = "merchant data",
                beneficiaryParameters = beneficiaries,
                senderParameters = senderParameters,
                attestationsRequested = ATTESTATIONS_REQUESTED,
                messageInformation = messageInformationEncrypted,
                recipientParameters = recipientParameters
            )
            transactId.createPaymentRequest(paymentRequestParameters)
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
                memo = "memo",
                paymentUrl = "www.payment.url/test",
                merchantData = "merchant data",
                beneficiaryParameters = beneficiaries,
                senderParameters = sender,
                attestationsRequested = ATTESTATIONS_REQUESTED
            )
            transactId.createPaymentRequest(paymentRequestParameters)
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
                memo = "memo",
                originatorParameters = originators,
                beneficiaryParameters = beneficiaries,
                messageInformation = messageInformationEncrypted,
                senderParameters = senderParameters,
                recipientParameters = recipientParameters
            )
            transactId.createPayment(paymentParameters)

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
                memo = "memo",
                originatorParameters = originators,
                beneficiaryParameters = beneficiaries
            )
            transactId.createPayment(paymentParameters)
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
            val paymentAckParameters = PaymentAckParameters(
                payment = PAYMENT,
                memo = "memo ack",
                messageInformation = messageInformationEncrypted,
                senderParameters = senderParameters,
                recipientParameters = recipientParameters
            )
            transactId.createPaymentAck(paymentAckParameters)
        } else {
            logger.info("Creating PaymentAck...")
            logger.info("Returning PaymentAck...")
            val paymentAckParameters = PaymentAckParameters(
                payment = PAYMENT,
                memo = "memo ack"
            )
            transactId.createPaymentAck(paymentAckParameters)
        }
    }
}
