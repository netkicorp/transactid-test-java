package com.netki.transactidlibraryjavademo.repo

import com.netki.transactidlibraryjavademo.util.ClientHttpLoggingInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class TransactIdRepository {

    @Autowired
    private lateinit var restTemplateBuilder: RestTemplateBuilder
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val restTemplate by lazy {
        val restTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(5000))
            .setReadTimeout(Duration.ofMillis(5000))
            .build()
        val interceptors = ArrayList<ClientHttpRequestInterceptor>()
        interceptors.add(ClientHttpLoggingInterceptor())
        restTemplate.interceptors = interceptors
        restTemplate.requestFactory =
            BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
        restTemplate
    }

    @Async("threadPoolTaskExecutor")
    fun sendRequestNoResponse(message: ByteArray, url: String) {
        Thread.sleep(5000)
        logger.info("Sending message to $url")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        headers.set("accept", "application/octet-stream")

        restTemplate.postForLocation(url, HttpEntity(message, headers))
        logger.info("Message successfully sent")
    }

}
