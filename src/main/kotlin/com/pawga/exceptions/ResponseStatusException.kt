package com.pawga.exceptions

import io.micronaut.http.HttpStatus

/**
 * Created by pawga777
 */
class ResponseStatusException(
    val status: HttpStatus,
    val reason: String,
) : RuntimeException()
