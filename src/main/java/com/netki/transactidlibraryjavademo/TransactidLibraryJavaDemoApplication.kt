package com.netki.transactidlibraryjavademo

import com.netki.transactidlibraryjavademo.model.EncryptionKeys
import com.netki.transactidlibraryjavademo.util.CryptoModule
import com.netki.transactidlibraryjavademo.util.KeyGenerator.Keys.generateKeyPairECDSA
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor


@SpringBootApplication
@Configuration
@EnableAsync
class TransactidLibraryJavaDemoApplication {

    @Bean
    fun getEncryptionKeys(): EncryptionKeys {
        val senderKeys = generateKeyPairECDSA()
        val recipientKeys = generateKeyPairECDSA()
        return EncryptionKeys(
            CryptoModule.objectToPrivateKeyPem(senderKeys.private),
            CryptoModule.objectToPublicKeyPem(senderKeys.public),
            CryptoModule.objectToPrivateKeyPem(recipientKeys.private),
            CryptoModule.objectToPublicKeyPem(recipientKeys.public)
        )
    }

    @Bean(name = ["threadPoolTaskExecutor"])
    fun threadPoolTaskExecutor(): Executor {
        return ThreadPoolTaskExecutor()
    }
}

fun main(args: Array<String>) {
    runApplication<TransactidLibraryJavaDemoApplication>(*args)
}
