package com.zwendo.restrikt2.test.privateconstructor

import java.lang.reflect.Modifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PrivateConstructorTest {

    @Test
    fun `Top-level class has private constructor`() {
        val clazz = Class.forName("$PACKAGE_NAME.$TOP_LEVEL_CLASS_NAME")

        val constructors = clazz.declaredConstructors
        assertEquals(1, constructors.size)

        val constructor = constructors[0]
        assertTrue(Modifier.isPrivate(constructor.modifiers))
    }

}

private const val PACKAGE_NAME = "com.zwendo.restrikt2.test.privateconstructor"

private const val TOP_LEVEL_CLASS_NAME = "PrivateConstructorKt"
