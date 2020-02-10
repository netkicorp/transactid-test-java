package com.netki.transactidlibraryjavademo.util

import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.StreamUtils
import java.io.IOException
import java.nio.charset.Charset

class ClientHttpLoggingInterceptor : ClientHttpRequestInterceptor {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logRequest(request, body)
        val response = execution.execute(request, body)
        logResponse(response)
        return response
    }

    @Throws(IOException::class)
    private fun logRequest(request: HttpRequest, body: ByteArray) {
        if (log.isDebugEnabled) {
            log.debug("=========================== request begin ================================================")
            log.debug("URI         : {}", request.uri)
            log.debug("Method      : {}", request.method)
            log.debug("Headers     : {}", request.headers)
            log.debug("Request body: {}", String(body, Charsets.UTF_8))
            log.debug("========================== request end ================================================")
        }
    }

    @Throws(IOException::class)
    private fun logResponse(response: ClientHttpResponse) {
        if (log.isDebugEnabled) {
            log.debug("============================ response begin ==========================================")
            log.debug("Status code  : {}", response.statusCode)
            log.debug("Status text  : {}", response.statusText)
            log.debug("Headers      : {}", response.headers)
            log.debug("Response body: {}", StreamUtils.copyToString(response.body, Charset.defaultCharset()))
            log.debug("======================= response end =================================================")
        }
    }
}
