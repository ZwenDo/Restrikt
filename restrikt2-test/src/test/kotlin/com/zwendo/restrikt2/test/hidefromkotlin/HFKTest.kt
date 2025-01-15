package com.zwendo.restrikt2.test.hidefromkotlin

import com.zwendo.restrikt2.test.assertNotNullAnd
import com.zwendo.restrikt2.test.packageprivate.PackagePrivateClass
import java.lang.reflect.AnnotatedElement
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.companionObject
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

class HFKTest {

    @Test
    fun `Not annotated property is not hidden`() {
        assertFalse(visiblePropertyAccessor.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated property is hidden and message is correctly applied`() {
        assertTrue(invisiblePropertyAccessor.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated getter is hidden while other functions stay visible`() {
        val property = invisiblePropertyGetterAccessor
        assertFalse(property.isHiddenFromKotlin)
        assertTrue(property.getter.isHiddenFromKotlin)
        assertFalse(property.setter.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated setter is hidden while other functions stay visible`() {
        val property = invisiblePropertySetterAccessor
        assertFalse(property.isHiddenFromKotlin)
        assertFalse(property.getter.isHiddenFromKotlin)
        assertTrue(property.setter.isHiddenFromKotlin)
    }

    @Test
    fun `Not annotated method is not hidden`() {
        assertFalse(visibleFunctionAccessor.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated method is hidden and message is correctly applied`() {
        assertTrue(invisibleFunctionAccessor.isHiddenFromKotlin)
    }

    @Test
    fun `Not annotated constructor is not hidden`() {
        val constructor = VisibleClass::class.constructors.first {
            it.parameters.isEmpty()
        }

        constructor.assertNotNullAnd {
            assertFalse(isHiddenFromKotlin)
        }
    }

    @Test
    fun `Annotated constructor is hidden and message is correctly applied`() {
        val constructor = VisibleClass::class.constructors.first {
            it.parameters.isNotEmpty() && it.parameters[0].name == "invisible"
        }

        constructor.assertNotNullAnd {
            assertTrue(isHiddenFromKotlin)
        }
    }

    @Test
    fun `Not annotated class is not hidden`() {
        val clazz = visibleClassAccessor
        assertFalse(clazz.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated class is hidden and message is correctly applied`() {
        val clazz = invisibleClassAccessor
        assertTrue(clazz.isHiddenFromKotlin)
    }

    @Test
    fun `Not annotated annotation is not hidden`() {
        val annotation = visibleAnnotationAccessor
        assertFalse(annotation.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated annotation is hidden and message is correctly applied`() {
        assertTrue(invisibleAnnotationAccessor.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated element is not hidden if it has private visibility`() {
        val element = VisibleClass::class.java.declaredMethods.first {
            it.name == "privateFunction"
        }

        assertFalse(element.kotlinFunction!!.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated class function is not hidden`() {
        val method = InvisibleClass::publicFunction
        assertFalse(method.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated class constructor is not hidden`() {
        val constructor = InvisibleClass::class.constructors.first { it.parameters.isEmpty() }
        assertFalse(constructor.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated class property getter is not hidden`() {
        val property = InvisibleClass::publicProperty.getter
        assertFalse(property.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated class property setter is not hidden`() {
        val property = InvisibleClass::publicProperty.setter
        assertFalse(property.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated class companion object is not hidden`() {
        val companion = InvisibleClass::class.companionObject!!
        assertFalse(companion.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated class inner class is not hidden`() {
        val innerClass = InvisibleClass.InnerClass::class
        assertFalse(innerClass.isHiddenFromKotlin)
    }

    private val KCallable<*>.isHiddenFromKotlin: Boolean
        get() = visibility == KVisibility.INTERNAL

    private val KClass<*>.isHiddenFromKotlin: Boolean
        get() = visibility == KVisibility.INTERNAL

}
