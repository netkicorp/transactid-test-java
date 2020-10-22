package com.netki.transactidlibraryjavademo.model

data class EncryptionKeys(
    val senderPrivateKey: String,
    val senderPublicKey: String,
    val recipientPrivateKey: String,
    val recipientPublicKey: String
)
