package com.zwendo.restrikt2.test.misc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class MiscTest {

    @Test
    fun `unnecessary file class is not generated when the file is empty`() {
        assertThrows<ClassNotFoundException> {
            Class.forName("com.zwendo.restrikt2.test.FileWithNoTopLevelDeclKt.kt")
        }
    }

    @Test
    fun `hfk with inner anonymous object does not throw`() {
        assertDoesNotThrow { hideFromKotlinWithAnonymousObject {  } }
    }

}
