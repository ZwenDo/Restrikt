package com.zwendo.restrikt.test.packageprivate

import com.zwendo.restrikt.test.assertNotNullAnd
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaSetter

class PPTest {

    @Test
    fun `Not annotated class is not hidden`() {
        assertFalse(VisibleClass::class.java.modifiers.isPackagePrivate)
    }

    @Test
    fun `Annotated class is hidden`() {
        assertTrue(PackagePrivateClass::class.java.modifiers.isPackagePrivate)
    }

    @Test
    fun `Not annotated function is not hidden`() {
        ::visibleFunction.javaMethod.assertNotNullAnd {
            assertFalse(modifiers.isPackagePrivate)
        }
    }

    @Test
    fun `Annotated function is hidden`() {
        ::packagePrivateFunction.javaMethod.assertNotNullAnd {
            assertTrue(modifiers.isPackagePrivate)
        }
    }

    @Test
    fun `Not annotated property is not hidden`() {
        val property = ::visibleProperty

        property.javaField.assertNotNullAnd {
            assertFalse(modifiers.isPackagePrivate)
        }

        property.getter.javaMethod.assertNotNullAnd {
            assertFalse(modifiers.isPackagePrivate)
        }

    }

    @Test
    fun `Annotated property is hidden`() {
        val property = ::packagePrivateProperty

        property.javaField.assertNotNullAnd {
            assertTrue(modifiers.isPackagePrivate)
        }

        property.javaGetter.assertNotNullAnd {
            assertTrue(modifiers.isPackagePrivate)
        }

        property.javaSetter.assertNotNullAnd {
            assertTrue(modifiers.isPackagePrivate)
        }
    }

    @Test
    fun `Annotated property getter is hidden while setter and field stay visible`() {
        val property = ::propertyWithHiddenGetter

        property.javaField.assertNotNullAnd {
            assertFalse(modifiers.isPackagePrivate)
        }

        property.javaGetter.assertNotNullAnd {
            assertTrue(modifiers.isPackagePrivate)
        }

        property.javaSetter.assertNotNullAnd {
            assertFalse(modifiers.isPackagePrivate)
        }
    }

    @Test
    fun `Annotated property setter is hidden while getter and field stay visible`() {
        val property = ::propertyWithHiddenSetter

        property.javaField.assertNotNullAnd {
            assertFalse(modifiers.isPackagePrivate)
        }

        property.javaGetter.assertNotNullAnd {
            assertFalse(modifiers.isPackagePrivate)
        }

        property.javaSetter.assertNotNullAnd {
            assertTrue(modifiers.isPackagePrivate)
        }
    }

    @Test
    fun `Annotated property field is hidden while functions stay visible`() {
        val property = ::propertyWithHiddenField

        property.javaField.assertNotNullAnd {
            assertTrue(modifiers.isPackagePrivate)
        }

        property.javaGetter.assertNotNullAnd {
            assertFalse(modifiers.isPackagePrivate)
        }

        property.javaSetter.assertNotNullAnd {
            assertFalse(modifiers.isPackagePrivate)
        }
    }

    @Test
    fun `Not annotated constructor is not hidden`() {
        val clazz = DummyClass::class.java
        val constructor: Constructor<*>? = clazz.getConstructor(Int::class.java)
        constructor.assertNotNullAnd {
            assertFalse(modifiers.isPackagePrivate)
        }
    }

    @Test
    fun `Annotated constructor is hidden`() {
        DummyClass::class.primaryConstructor.assertNotNullAnd {
            javaConstructor.assertNotNullAnd {
                assertTrue(modifiers.isPackagePrivate)
            }
        }
    }

    private val Int.isPackagePrivate
        get() = this and (Modifier.PRIVATE xor Modifier.PROTECTED xor Modifier.PUBLIC) == 0

}
