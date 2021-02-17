package com.netki.transactidlibraryjavademo.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Information(
    @JsonProperty("vasp_certificate")
    val vaspCertificate: String
)
