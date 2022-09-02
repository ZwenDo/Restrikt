package com.zwendo.restrikt.test.hidefromkotlin

import com.zwendo.restrikt.test.assertNotNullAnd
import java.lang.reflect.AnnotatedElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import restrikt_test.BuildConfig
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.jvm.javaField

class HideFromKotlinTest {

    @Test
    fun `Not annotated property is not hidden`() {
        assertFalse(visiblePropertyAccessor.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated property is hidden and default message is correctly applied`() {
        val property = invisiblePropertyDefaultMessageAccessor
        assertTrue(property.isHiddenFromKotlin)
        assertEquals(BuildConfig.KOTLIN_DEFAULT_REASON, property.hideFromKotlinMessage)
    }

    @Test
    fun `Annotated field is hidden and default message is correctly applied`() {
        val property = ::invisibleFieldDefaultMessage
        property.javaField.assertNotNullAnd {
            assertTrue(isHiddenFromKotlin)
            assertEquals(BuildConfig.KOTLIN_DEFAULT_REASON, hideFromKotlinMessage)
        }
    }

    @Test
    fun `Annotated property is hidden and custom message is correctly applied`() {
        val property = invisiblePropertyCustomMessageAccessor
        assertTrue(property.isHiddenFromKotlin)
        assertEquals(CUSTOM_MESSAGE, property.hideFromKotlinMessage)
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
        val property = visibleFunctionAccessor
        assertFalse(property.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated method is hidden and default message is correctly applied`() {
        val property = invisibleFunctionDefaultMessageAccessor
        assertTrue(property.isHiddenFromKotlin)
        assertEquals(BuildConfig.KOTLIN_DEFAULT_REASON, property.hideFromKotlinMessage)
    }

    @Test
    fun `Annotated method is hidden and custom message is correctly applied`() {
        val property = invisibleFunctionCustomMessageAccessor
        assertTrue(property.isHiddenFromKotlin)
        assertEquals(CUSTOM_MESSAGE_2, property.hideFromKotlinMessage)
    }

    @Test
    fun `Not annotated constructor is not hidden`() {
        val constructor = VisibleClass::class.constructors.first {
            it.parameters.isNotEmpty() && it.parameters[0].name == "visible"
        }

        constructor.assertNotNullAnd {
            assertFalse(isHiddenFromKotlin)
        }
    }

    @Test
    fun `Annotated constructor is hidden and default message is correctly applied`() {
        val constructor = VisibleClass::class.constructors.first {
            it.parameters.isNotEmpty() && it.parameters[0].name == "invisible"
        }

        constructor.assertNotNullAnd {
            assertTrue(isHiddenFromKotlin)
            assertEquals(BuildConfig.KOTLIN_DEFAULT_REASON, hideFromKotlinMessage)
        }
    }

    @Test
    fun `Annotated constructor is hidden and custom message is correctly applied`() {
        val constructor = VisibleClass::class.constructors.first { it.parameters.isEmpty() }

        constructor.assertNotNullAnd {
            assertTrue(isHiddenFromKotlin)
            assertEquals(CUSTOM_MESSAGE_3, hideFromKotlinMessage)
        }
    }

    @Test
    fun `Not annotated class is not hidden`() {
        assertFalse(VisibleClass::class.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated class is hidden and default message is correctly applied`() {
        val clazz = invisibleClassDefaultMessageAccessor
        assertTrue(clazz.isHiddenFromKotlin)
        assertEquals(BuildConfig.KOTLIN_DEFAULT_REASON, clazz.hideFromKotlinMessage)
    }


    private val KAnnotatedElement.isHiddenFromKotlin: Boolean
        get() = annotations.any { it is Deprecated && it.level == DeprecationLevel.HIDDEN }

    private val AnnotatedElement.isHiddenFromKotlin: Boolean
        get() = annotations.any { it is Deprecated && it.level == DeprecationLevel.HIDDEN }

    private val KAnnotatedElement.hideFromKotlinMessage: String
        get() = annotations.find { it is Deprecated && it.level == DeprecationLevel.HIDDEN }
            ?.let { (it as Deprecated).message }
            ?: throw AssertionError("Element $this is not hidden from kotlin")

    private val AnnotatedElement.hideFromKotlinMessage: String
        get() = annotations.find { it is Deprecated && it.level == DeprecationLevel.HIDDEN }
            ?.let { (it as Deprecated).message }
            ?: throw AssertionError("Element $this is not hidden from kotlin")
}
