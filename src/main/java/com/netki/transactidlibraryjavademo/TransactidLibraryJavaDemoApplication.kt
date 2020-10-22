package com.netki.transactidlibraryjavademo

import com.netki.transactidlibraryjavademo.model.EncryptionKeys
import com.netki.transactidlibraryjavademo.util.CryptoModule
import com.netki.transactidlibraryjavademo.util.KeyGenerator.Keys.generateKeyPairECDSA
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.security.AlgorithmParameters
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec

@SpringBootApplication
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
}

fun main(args: Array<String>) {
    runApplication<TransactidLibraryJavaDemoApplication>(*args)
}
