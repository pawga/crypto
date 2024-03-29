package com.pawga.exceptions

import io.micronaut.http.HttpStatus
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ErrorResponse(
    var httpStatus: HttpStatus? = null,
    var exception: String? = null,
    var message: String? = null,
)
