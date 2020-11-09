package com.netki.transactidlibraryjavademo.controller

import com.netki.transactidlibraryjavademo.model.EncryptionKeys
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
@Tag(name = "Utils", description = "Utilities to test TransactId")
@RequestMapping("/utils")
class UtilController {

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
    @ApiResponse(
        responseCode = "200",
        description = "Encryption keys.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = EncryptionKeys::class)
            )
        ]
    )
    fun getEncryptionKeys(): ResponseEntity<EncryptionKeys> =
        ResponseEntity.ok(transactIdService.getEncryptionKeys())
}
