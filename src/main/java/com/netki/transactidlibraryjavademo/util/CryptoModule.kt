package com.netki.transactidlibraryjavademo.util

import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.Certificate

object CryptoModule {

    /**
     * Transform PublicKey to String in PEM format.
     *
     * @param publicKey to transform.
     * @return String in PEM format.
     */
    fun objectToPublicKeyPem(publicKey: PublicKey) = objectToPemString(publicKey)

    /**
     * Transform PrivateKey to String in PEM format.
     *
     * @param privateKey to transform.
     * @return String in PEM format.
     */
    fun objectToPrivateKeyPem(privateKey: PrivateKey) = objectToPemString(privateKey)

    private fun objectToPemString(objectToParse: Any): String {
        val stringWriter = StringWriter()
        val pemWriter = PemWriter(stringWriter)
        when (objectToParse) {
            is PrivateKey -> pemWriter.writeObject(PemObject("PRIVATE KEY", objectToParse.encoded))
            is PublicKey -> pemWriter.writeObject(PemObject("PUBLIC KEY", objectToParse.encoded))
            is Certificate -> pemWriter.writeObject(PemObject("CERTIFICATE", objectToParse.encoded))
        }
        pemWriter.flush()
        pemWriter.close()
        return stringWriter.toString()
    }
}
