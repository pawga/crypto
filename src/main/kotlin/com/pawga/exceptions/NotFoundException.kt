package com.pawga.exceptions

import java.lang.RuntimeException

class NotFoundException : RuntimeException {
    constructor() : super()

    constructor(message: String) : super(message)
}
