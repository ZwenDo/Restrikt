package com.zwendo.restrikt.test.hidefromkotlin

import com.zwendo.restrikt.annotation.HideFromKotlin
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
    fun `Annotated property is hidden and message is correctly applied`() {
        val property = invisiblePropertyAccessor
        assertTrue(property.isHiddenFromKotlin)
        assertEquals(BuildConfig.DEPRECATED_REASON, property.deprecatedMessage)
    }

    @Test
    fun `Annotated field is hidden and message is correctly applied`() {
        val property = ::invisibleField
        property.javaField.assertNotNullAnd {
            assertTrue(isHiddenFromKotlin)
            assertEquals(BuildConfig.DEPRECATED_REASON, deprecatedMessage)
        }
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
    fun `Annotated method is hidden and message is correctly applied`() {
        val property = invisibleFunctionAccessor
        assertTrue(property.isHiddenFromKotlin)
        assertEquals(BuildConfig.DEPRECATED_REASON, property.deprecatedMessage)
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
            assertEquals(BuildConfig.DEPRECATED_REASON, deprecatedMessage)
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
        assertEquals(BuildConfig.DEPRECATED_REASON, clazz.deprecatedMessage)
    }

    @Test
    fun `Not annotated annotation is not hidden`() {
        val annotation = visibleAnnotationAccessor
        assertFalse(annotation.isHiddenFromKotlin)
    }

    @Test
    fun `Annotated annotation is hidden and message is correctly applied`() {
        val annotation = invisibleAnnotationAccessor
        assertTrue(annotation.isHiddenFromKotlin)
        assertEquals(BuildConfig.DEPRECATED_REASON, annotation.deprecatedMessage)
    }

    @Test
    fun `Custom runtime retention is correctly applied`() {
        val function = invisibleFunctionWithRuntimeRetentionAccessor
        assertTrue(function.isHiddenFromKotlin)
        assertTrue(function.hasHideFromKotlin)
    }

    @Test
    fun `Custom binary retention is correctly applied`() {
        val function = invisibleFunctionWithBinaryRetentionAccessor
        assertTrue(function.isHiddenFromKotlin)
        assertFalse(function.hasHideFromKotlin)
    }

    @Test
    fun `Custom source retention is correctly applied`() {
        val function = invisibleFunctionWithSourceRetentionAccessor
        assertTrue(function.isHiddenFromKotlin)
        assertFalse(function.hasHideFromKotlin)
    }


    private val KAnnotatedElement.isHiddenFromKotlin: Boolean
        get() = annotations.any { it is Deprecated && it.level == DeprecationLevel.HIDDEN }

    private val AnnotatedElement.isHiddenFromKotlin: Boolean
        get() = annotations.any { it is Deprecated && it.level == DeprecationLevel.HIDDEN }

    private val KAnnotatedElement.deprecatedMessage: String
        get() = annotations.find { it is Deprecated && it.level == DeprecationLevel.HIDDEN }
            ?.let { (it as Deprecated).message }
            ?: throw AssertionError("Element $this is not hidden from kotlin")

    private val AnnotatedElement.deprecatedMessage: String
        get() = annotations.find { it is Deprecated && it.level == DeprecationLevel.HIDDEN }
            ?.let { (it as Deprecated).message }
            ?: throw AssertionError("Element $this is not hidden from kotlin")

    private val KAnnotatedElement.hasHideFromKotlin: Boolean
        get() = annotations.any { it is HideFromKotlin }
}
