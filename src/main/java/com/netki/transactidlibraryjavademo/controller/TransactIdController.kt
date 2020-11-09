package com.netki.transactidlibraryjavademo.controller

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
@Tag(name = "TransactId", description = "The TransactId API")
class TransactIdController {

    @Autowired
    private lateinit var transactIdService: TransactIdService

    @Operation(
        summary = "Get invoiceRequest binary",
        description = "Returns an invoiceRequest binary so that you can start testing your flow"
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/initial-invoice-request"],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    @ApiResponse(
        responseCode = "201",
        description = "InvoiceRequest binary.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                schema = Schema(implementation = ByteArray::class)
            )
        ]
    )
    fun getInitialInvoiceRequest() = ResponseEntity(
        transactIdService.getInitialInvoiceRequest(),
        HttpStatus.CREATED
    )

    @Operation(
        summary = "Get invoiceRequest binary encrypted",
        description = "Returns an invoiceRequest binary encrypted so that you can start testing your flow"
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/initial-invoice-request-encrypted"],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    @ApiResponse(
        responseCode = "201",
        description = "InvoiceRequest binary.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                schema = Schema(implementation = ByteArray::class)
            )
        ]
    )
    fun getInitialInvoiceRequestEncrypted() = ResponseEntity(
        transactIdService.getInitialInvoiceRequestEncrypted(),
        HttpStatus.CREATED
    )

    @Operation(
        summary = "Post an invoiceRequest binary",
        description = "This endpoint receives an invoiceRequest binary and gives a paymentRequest binary in return."
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/invoice-request"],
        consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    @ApiResponse(
        responseCode = "200",
        description = "PaymentRequest binary.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                schema = Schema(implementation = ByteArray::class)
            )
        ]
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
        summary = "Post a paymentRequest binary.",
        description = "This endpoint receives a paymentRequest binary and gives a payment binary in return."
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/payment-request"],
        consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    @ApiResponse(
        responseCode = "200",
        description = "Payment binary.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                schema = Schema(implementation = ByteArray::class)
            )
        ]
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
        summary = "Post a payment binary",
        description = "This endpoint receives a payment binary and gives a paymentAck binary in return."
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/payment"],
        consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
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
