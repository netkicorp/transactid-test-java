package com.netki.transactidlibraryjavademo.controller

import com.netki.transactidlibraryjavademo.model.InvoiceRequestUrl
import com.netki.transactidlibraryjavademo.service.TransactIdService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
@Tag(name = "TransactId-Async", description = "The TransactId API async")
@RequestMapping("/async")
class TransactIdAsyncController {

    @Autowired
    private lateinit var transactIdService: TransactIdService

    @Operation(
        summary = "Send invoiceRequest binary",
        description = "Send an invoiceRequest binary to an specific URL"
    )
    @ApiResponse(
        responseCode = "202",
        description = "Indicates that the request has been accepted for processing.",
        content = [Content()]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/initial-invoice-request"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun sendInitialInvoiceRequest(
        @Parameter(description = "Url where the InvoiceRequest will be send")
        @RequestBody invoiceRequestUrl: InvoiceRequestUrl
    ): ResponseEntity<Any> {
        transactIdService.sendInitialInvoiceRequest(invoiceRequestUrl.url)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @Operation(
        summary = "Send invoiceRequest binary encrypted",
        description = "Send an invoiceRequest binary encrypted to an specific URL"
    )
    @ApiResponse(
        responseCode = "202",
        description = "Indicates that the request has been accepted for processing.",
        content = [Content()]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/initial-invoice-request-encrypted"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun sendInitialInvoiceRequestEncrypted(
        @Parameter(description = "Url where the InvoiceRequest will be send")
        @RequestBody invoiceRequestUrl: InvoiceRequestUrl
    ): ResponseEntity<Any> {
        transactIdService.sendInitialInvoiceRequestEncrypted(invoiceRequestUrl.url)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @Operation(
        summary = "Post an invoiceRequest binary with no sync response",
        description = "This endpoint receives an invoiceRequest binary and gives a 202 in return. Asynchronous you will receive a PaymentRequest binary in your NotificationUrl defined in your invoiceRequest."
    )
    @ApiResponse(
        responseCode = "202",
        description = "Indicates that the request has been accepted for processing.",
        content = [Content()]
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
            transactIdService.postInvoiceRequestAsync(invoiceRequest)
            ResponseEntity(HttpStatus.ACCEPTED)
        } catch (exception: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.message)
        }
    }

    @Operation(
        summary = "Post a paymentRequest binary with no sync response.",
        description = "This endpoint receives a paymentRequest binary and gives a 202 in return. Asynchronous you will receive a Payment binary in your PaymentUrl defined in your paymentRequest."
    )
    @ApiResponse(
        responseCode = "202",
        description = "Indicates that the request has been accepted for processing.",
        content = [Content()]
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
            transactIdService.postPaymentRequestAsync(paymentRequest)
            ResponseEntity(HttpStatus.ACCEPTED)
        } catch (exception: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.message)
        }
    }

    @Operation(
        summary = "Post a payment binary",
        description = "This endpoint receives a payment binary and gives a paymentAck binary in return."
    )
    @ApiResponse(
        responseCode = "200",
        description = "PaymentAck binary.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                schema = Schema(implementation = ByteArray::class)
            )
        ]
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
