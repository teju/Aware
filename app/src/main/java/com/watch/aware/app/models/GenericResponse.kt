package com.watch.aware.app.models

data class GenericResponse(
    val message: String,
    val is_redirect: Boolean,
    val status: String
)