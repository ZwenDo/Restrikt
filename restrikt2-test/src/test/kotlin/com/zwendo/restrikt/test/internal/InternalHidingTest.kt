package com.zwendo.restrikt.test.internal

import com.zwendo.restrikt.test.assertNotNullAnd
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaSetter

class InternalHidingTest {

    @Test
    fun `Internal property field and methods are hidden`() {
        val instance = InternalTestClass()
        val property = instance::internalProperty

        property.javaField.assertNotNullAnd {
            assertTrue(isSynthetic)
        }

        property.javaGetter.assertNotNullAnd {
            assertTrue(isSynthetic)
        }

        property.javaSetter.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

    @Test
    fun `Internal method is hidden`() {
        val instance = InternalTestClass()

        instance::internalFunction.javaMethod.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

    @Test
    fun `Internal constructor is hidden`() {
        InternalTestClass::class.primaryConstructor.assertNotNullAnd {
            this.javaConstructor.assertNotNullAnd {
                assertTrue(isSynthetic)
            }
        }
    }

    @Test
    fun `Top-level internal property is hidden`() {
        val property = ::internalProperty

        property.javaField.assertNotNullAnd {
            assertTrue(isSynthetic)
        }

        property.javaGetter.assertNotNullAnd {
            assertTrue(isSynthetic)
        }

        property.javaSetter.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

    @Test
    fun `Top-level internal function is hidden`() {
        val function = ::internalFunction
        function.javaMethod.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

    @Test
    fun `Private class is not hidden`() {
        assertFalse(privateClassAccessor.java.isSynthetic)
    }

    @Test
    fun `Private companion object is not hidden`() {
        privateClassAccessor.companionObject.assertNotNullAnd {
            assertFalse(java.isSynthetic)
        }
    }

    @Test
    fun `Private nested class is not hidden`() {
        val instance = InternalTestClass()
        assertFalse(instance.nestedPrivateClassAccessor.java.isSynthetic)
    }

    @Test
    fun `Private property field is not hidden`() { // no getter and setter as property is private
        val instance = InternalTestClass()
        val property = instance.privatePropertyAccessor

        property.javaField.assertNotNullAnd {
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Private method is not hidden`() {
        val instance = InternalTestClass()

        instance.privateFunctionAccessor.javaMethod.assertNotNullAnd {
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Private annotation is not hidden`() {
        assertFalse(privateAnnotationAccessor.java.isSynthetic)
    }

    @Test
    fun `Top-level private property is not hidden`() {
        val property = privatePropertyAccessor

        property.javaField.assertNotNullAnd {
            assertFalse(isSynthetic)
        }

    }

    @Test
    fun `Top-level private method is not hidden`() {
        privateFunctionAccessor.javaMethod.assertNotNullAnd {
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Public class is not hidden`() {
        assertFalse(PublicClass::class.java.isSynthetic)
    }

    @Test
    fun `Public companion object is not hidden`() {
        assertFalse(PublicClass.Companion::class.java.isSynthetic)
    }

    @Test
    fun `Public nested class is not hidden`() {
        assertFalse(InternalTestClass.NestedPublicClass::class.java.isSynthetic)
    }

    @Test
    fun `Public property field and methods are not hidden`() {
        val instance = InternalTestClass()
        val property = instance::publicProperty

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
    fun `Public method is not hidden`() {
        val instance = InternalTestClass()

        instance::publicFunction.javaMethod.assertNotNullAnd {
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Public annotation is not hidden`() {
        assertFalse(PublicAnnotation::class.java.isSynthetic)
    }

    @Test
    fun `Top-level public property field and methods are not hidden`() {
        val property = ::publicProperty

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
    fun `Top-level public method is not hidden`() {
        ::publicFunction.javaMethod.assertNotNullAnd {
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Protected property field and methods are not hidden`() {
        val instance = InternalTestClass()
        val property = instance.protectedPropertyAccessor

        property.javaField.assertNotNullAnd {
            assertFalse(isSynthetic)
        }

        property.javaGetter.assertNotNullAnd {
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Protected method is not hidden`() {
        val instance = InternalTestClass()

        instance.protectedFunctionAccessor.javaMethod.assertNotNullAnd {
            assertFalse(isSynthetic)
        }

    }

    @Test
    fun `Protected nested class is not hidden`() {
        val instance = InternalTestClass()
        assertFalse(instance.nestedProtectedClassAccessor.java.isSynthetic)
    }

    @Test
    fun `Internal class members are hidden`() {
        val method = InternalClass::publicFunction

        method.javaMethod.assertNotNullAnd {
            assertTrue(isSynthetic)
        }
    }

}
