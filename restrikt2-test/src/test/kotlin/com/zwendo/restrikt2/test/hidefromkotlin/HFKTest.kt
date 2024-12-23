package com.zwendo.restrikt2.test.hidefromkotlin

import com.zwendo.restrikt2.test.assertNotNullAnd
import java.lang.reflect.AnnotatedElement
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.javaField

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


    private val KCallable<*>.isHiddenFromKotlin: Boolean
        get() = visibility == KVisibility.INTERNAL

    private val KClass<*>.isHiddenFromKotlin: Boolean
        get() = visibility == KVisibility.INTERNAL

}
