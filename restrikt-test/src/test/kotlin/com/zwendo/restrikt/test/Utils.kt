package com.zwendo.restrikt.test

import org.junit.jupiter.api.Assertions

inline fun <T> T?.assertNotNullAnd(block: T.() -> Unit) {
    Assertions.assertNotNull(this)
    this!!.block()
}
