package com.netki.transactidlibraryjavademo.controller

import com.netki.transactidlibraryjavademo.service.TransactIdService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "Bip75", description = "BIP75 API")
class Bip75Controller {

    @Autowired
    private lateinit var transactIdService: TransactIdService

    @Operation(
        summary = "Post protocol message binary",
        description = "Post a protocol message binary, could be encrypted or not"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "Protocol message accepted and stored."
            ),
            ApiResponse(
                responseCode = "200",
                description = "Returns the correspondent response for the protocol message.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        schema = Schema(implementation = ByteArray::class)
                    )
                ]
            )
        ]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/addresses/{address_id}/messages"],
        consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun sendProtocolMessage(
        @Parameter(description = "Unique identifier for address.")
        @PathVariable("address_id") addressId: String,
        @Parameter(description = "Set to true if you want to do this flow async, false otherwise.")
        @RequestParam async: Boolean = false,
        @Parameter(description = "Binary containing the protocol message")
        @RequestBody protocolMessage: ByteArray
    ): ResponseEntity<Any> {
        val messageResponse = when (async) {
            true -> transactIdService.sendProtocolMessageAsync(addressId, protocolMessage)
            false -> transactIdService.sendProtocolMessage(addressId, protocolMessage)
        }
        return if (messageResponse != null) {
            ResponseEntity.ok(messageResponse)
        } else {
            ResponseEntity(HttpStatus.ACCEPTED)
        }
    }
}
