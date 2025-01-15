package com.zwendo.restrikt2.test.packageprivate

import com.zwendo.restrikt2.test.assertNotNullAnd
import com.zwendo.restrikt2.test.misc.Bar
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.KVisibility
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
        val property = ::visiblePropertyPP

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
            assertFalse(modifiers.isPackagePrivate)
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

    @Test
    fun `Annotated element ignores HFJ annotation`() {
        ::ppFunctionWithHfj.javaMethod.assertNotNullAnd {
            assertTrue(modifiers.isPackagePrivate)
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Annotated element ignores internal visibility`() {
        ::packagePrivateFunctionWithInternal.javaMethod.assertNotNullAnd {
            assertTrue(modifiers.isPackagePrivate)
            assertFalse(isSynthetic)
        }
    }

    @Test
    fun `Annotated element is not made package private if it is private`() {
        val clazz = Foo::class.java
        val method = clazz.getDeclaredMethod("privateFunction")
        assertEquals(Modifier.PRIVATE, method.modifiers and Modifier.PRIVATE)
    }

    @Test
    fun `Annotated class functions are not package private`() {
        val method = PackagePrivateClass::publicFunction.javaMethod!!
        assertEquals(Modifier.PUBLIC, method.modifiers and Modifier.PUBLIC)
    }

    @Test
    fun `Annotated class constructor is not package private`() {
        val constructor = PackagePrivateClass::class.java.constructors.first { it.parameters.isEmpty() }
        assertEquals(Modifier.PUBLIC, constructor.modifiers and Modifier.PUBLIC)
    }

    @Test
    fun `Annotated class property getter is not package private`() {
        val property = PackagePrivateClass::publicProperty
        assertEquals(Modifier.PUBLIC, property.javaGetter!!.modifiers and Modifier.PUBLIC)
    }

    @Test
    fun `Annotated class property setter is not package private`() {
        val property = PackagePrivateClass::publicProperty
        assertEquals(Modifier.PUBLIC, property.javaSetter!!.modifiers and Modifier.PUBLIC)
    }

    @Test
    fun `Annotated class companion object is not package private`() {
        val companion = PackagePrivateClass.Companion::class.java
        assertEquals(Modifier.PUBLIC, companion.modifiers and Modifier.PUBLIC)
    }

    @Test
    fun `Annotated class inner class is not package private`() {
        val innerClass = PackagePrivateClass.InnerClass::class.java
        assertEquals(Modifier.PUBLIC, innerClass.modifiers and Modifier.PUBLIC)
    }

    private val Int.isPackagePrivate
        get() = this and (Modifier.PRIVATE xor Modifier.PROTECTED xor Modifier.PUBLIC) == 0

}
