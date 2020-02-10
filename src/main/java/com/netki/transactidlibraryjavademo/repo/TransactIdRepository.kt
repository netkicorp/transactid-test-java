package com.netki.transactidlibraryjavademo.repo

import com.netki.transactidlibraryjavademo.util.ClientHttpLoggingInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class TransactIdRepository {

    @Autowired
    private lateinit var restTemplateBuilder: RestTemplateBuilder
    private val url = "http://localhost:8081/"
    private val token = "e08dca199281279be75c1272b4533898406c93c8"

    private val restTemplate by lazy {
        val restTemplate = restTemplateBuilder
            .rootUri(url)
            .setConnectTimeout(Duration.ofMillis(5000))
            .setReadTimeout(Duration.ofMillis(5000))
            .build()
        val interceptors = ArrayList<ClientHttpRequestInterceptor>()
        interceptors.add(ClientHttpLoggingInterceptor())
        restTemplate.interceptors = interceptors
        restTemplate.requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
        restTemplate
    }

    fun postInvoiceRequest(invoiceRequest: ByteArray, url: String) {
        sendRequestNoResponse(invoiceRequest, url)
    }

    fun sendInvoiceRequest(invoiceRequest: ByteArray, walletAddress: String): ByteArray {
        val urlInvoiceRequest = "/bip/$walletAddress/invoice-requests/"
        return sendRequest(urlInvoiceRequest, invoiceRequest)!!
    }

    fun sendPayment(payment: ByteArray, walletAddress: String): ByteArray {
        val urlPayment = "/bip/$walletAddress/record-payment/"
        return sendRequest(urlPayment, payment)!!
    }

    private fun sendRequest(url: String, message: ByteArray): ByteArray? {
        val headers = HttpHeaders()
        headers.set("Authorization", token)
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        headers.set("accept", "application/octet-stream")

        return restTemplate.exchange(url, HttpMethod.POST, HttpEntity(message, headers), ByteArray::class.java).body
    }

    private fun sendRequestNoResponse(message: ByteArray, url: String) {
        val headers = HttpHeaders()
        headers.set("Authorization", token)
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        headers.set("accept", "application/octet-stream")

        restTemplate.postForLocation(url, HttpEntity(message, headers))
    }

}
