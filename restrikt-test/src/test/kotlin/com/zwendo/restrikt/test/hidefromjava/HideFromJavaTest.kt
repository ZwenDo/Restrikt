package com.zwendo.restrikt.test.hidefromjava

import com.zwendo.restrikt.annotation.HideFromJava
import com.zwendo.restrikt.test.assertNotNullAnd
import com.zwendo.restrikt.test.hidefromkotlin.visibleFunction
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaSetter

class HideFromJavaTest {

    @Test
    fun `Not annotated class is not hidden`() {
        assertFalse(VisibleClass::class.java.isSynthetic)
    }

    @Test
    fun `Annotated class has its class members hidden transitively`() {
        val invisibleMethod = InvisibleClass::invisibleFunctionDueToClassAnnotation.javaMethod
        invisibleMethod.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

    @Test
    fun `Annotated class hiding also applies to inner classes`() {
        val invisibleMethod = InvisibleClass.NestedClass::nestedClassFunction.javaMethod
        invisibleMethod.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

    @Test
    fun `Not annotated property is not hidden`() {
        val property = ::visibleProperty

        property.javaField.assertNotNullAnd {
            assertFalse(isSynthetic)
        }

        property.javaGetter.assertNotNullAnd {
            assertFalse(isSynthetic)
        }

        property.javaSetter.assertNotNullAnd {
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Annotated property is hidden`() {
        val property = ::invisibleProperty

        property.javaGetter.assertNotNullAnd {
            assertTrue(isSynthetic)
        }

        property.javaSetter.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

    @Test
    fun `Annotated getter is hidden while other functions stay visible`() {
        val property = ::invisiblePropertyGetter

        property.javaField.assertNotNullAnd {
            assertFalse(isSynthetic)
        }

        property.javaGetter.assertNotNullAnd {
            assertTrue(isSynthetic)
        }

        property.javaSetter.assertNotNullAnd {
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Annotated setter is hidden while other functions stay visible`() {
        val property = ::invisiblePropertySetter

        property.javaField.assertNotNullAnd {
            assertFalse(isSynthetic)
        }

        property.javaGetter.assertNotNullAnd {
            assertFalse(isSynthetic)
        }

        property.javaSetter.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

    @Test
    fun `Not annotated method is not hidden`() {
        val method = ::visibleFunction

        method.javaMethod.assertNotNullAnd {
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Annotated method is hidden`() {
        val method = ::invisibleFunction

        method.javaMethod.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

    @Test
    fun `Not annotated constructor is not hidden`() {
        VisibleConstructorClass::class.primaryConstructor.assertNotNullAnd {
            javaConstructor.assertNotNullAnd {
                assertFalse(isSynthetic)
            }
        }
    }

    @Test
    fun `Annotated constructor is hidden`() {
        InvisibleConstructorClass::class.primaryConstructor.assertNotNullAnd {
            javaConstructor.assertNotNullAnd {
                assertTrue(isSynthetic)
            }
        }
    }

    @Test
    fun `Custom runtime retention is applied correctly`() {
        val method = ::functionWithCustomRuntimeRetention

        method.javaMethod.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
        assertTrue(method.hasHideFromJava)
    }

    @Test
    fun `Custom binary retention is applied correctly`() {
        val method = ::functionWithCustomBinaryRetention

        method.javaMethod.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
        assertFalse(method.hasHideFromJava)
    }

    @Test
    fun `Custom source retention is applied correctly`() {
        val method = ::functionWithCustomSourceRetention

        method.javaMethod.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
        assertFalse(method.hasHideFromJava)
    }

    @Test
    fun `Annotated file has all its top-level members hidden transitively`() {
        val method = ::invisibleFunctionDueToFileAnnotation

        method.javaMethod.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

    private val KAnnotatedElement.hasHideFromJava: Boolean
        get() = annotations.any { it is HideFromJava }

}
