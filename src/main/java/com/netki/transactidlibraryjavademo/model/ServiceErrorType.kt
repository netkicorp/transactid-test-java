package com.netki.sapphire.model

enum class ServiceErrorType {
    ADDRESS_PROVIDER_ERROR,
    ADDRESS_PROVIDER_UNAUTHORIZED,
    CERTIFICATE_PROVIDER,
    CERTIFICATE_PROVIDER_UNAUTHORIZED,
    INVALID_CERTIFICATE_CHAIN,
    INVALID_CERTIFICATE,
    INVALID_OBJECT,
    INVALID_OWNERS,
    INVALID_PRIVATE_KEY,
    INVALID_SIGNATURE,
    KEY_MANAGEMENT_FETCH,
    KEY_MANAGEMENT_STORE,
    OBJECT_NOT_FOUND,
    INVALID_DATA,
    ENCRYPTION_ERROR,
    UNKNOWN
}
