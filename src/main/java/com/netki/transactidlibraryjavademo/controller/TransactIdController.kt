package com.netki.transactidlibraryjavademo.controller

import com.netki.transactidlibraryjavademo.model.EncryptionKeys
import com.netki.transactidlibraryjavademo.service.TransactIdService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "TransactId", description = "The TransactId API")
class TransactIdController {

    @Autowired
    private lateinit var transactIdService: TransactIdService

    @Operation(
        summary = "Get Encryption keys",
        description = "Get the set of keys for the sender/recipient, this is needed if you want to generate encrypted messages"
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/encryption/keys"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getEncryptionKeys(): ResponseEntity<EncryptionKeys> =
        ResponseEntity.ok(transactIdService.getEncryptionKeys())

    @Operation(
        summary = "Get invoiceRequest binary",
        description = "Request receive back an invoiceRequest binary so that you can test parsing things"
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/initial-invoice-request"],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun getInitialInvoiceRequest() = ResponseEntity(
        transactIdService.getInitialInvoiceRequest(),
        HttpStatus.CREATED
    )

    @Operation(
        summary = "Get invoiceRequest binary encrypted",
        description = "Request receive back an invoiceRequest binary encrypted so that you can test parsing things"
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/initial-invoice-request-encrypted"],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun getInitialInvoiceRequestEncrypted() = ResponseEntity(
        transactIdService.getInitialInvoiceRequestEncrypted(),
        HttpStatus.CREATED
    )

    @Operation(
        summary = "Post invoiceRequest binary",
        description = "Send invoiceRequest binary to this endpoint and receive a paymentRequest binary in return."
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/invoice-request"],
        consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun postInvoiceRequest(
        @Parameter(description = "Binary containing invoiceRequest")
        @RequestBody invoiceRequest: ByteArray
    ): ResponseEntity<Any> {
        return try {
            val paymentRequest = transactIdService.postInvoiceRequest(invoiceRequest)
            ResponseEntity.ok(paymentRequest)
        } catch (exception: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.message)
        }
    }

    @Operation(
        summary = "Post paymentRequest binary",
        description = "Send a paymentRequest binary to this endpoint and receive a payment binary in return."
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/payment-request"],
        consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun postPaymentRequest(
        @Parameter(description = "Binary containing paymentRequest")
        @RequestBody paymentRequest: ByteArray
    ): ResponseEntity<Any> {
        return try {
            val payment = transactIdService.postPaymentRequest(paymentRequest)
            ResponseEntity.ok(payment)
        } catch (exception: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.message)
        }
    }

    @Operation(
        summary = "Post payment binary",
        description = "Send a payment binary to this endpoint and receive a paymentAck binary in return."
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/payment"],
        consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun postPayment(
        @Parameter(description = "Binary containing payment")
        @RequestBody payment: ByteArray
    ): ResponseEntity<Any> {
        return try {
            val paymentAck = transactIdService.postPayment(payment)
            ResponseEntity.ok(paymentAck)
        } catch (exception: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.message)
        }
    }
}
