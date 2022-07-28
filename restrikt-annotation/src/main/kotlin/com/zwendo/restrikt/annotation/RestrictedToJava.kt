package com.zwendo.restrikt.annotation

/**
 * This annotation is used to mark elements that should only be used from Java code.
 *
 * Used with the [Restrikt compiler plugin](https://github.com/ZwenDo/Restrikt), this annotation causes the generation
 * of the [Deprecated] annotation with the level [DeprecationLevel.HIDDEN], which hides the element for Kotlin sources.
 * However, elements will still be accessible at runtime from compiled Kotlin files.
 *
 * Like [JvmSynthetic] acts to hide elements to Java, this annotation is intended for API designers who want to hide a
 * Java-specific target from Kotlin language, in order to keep an idiomatic API for both languages.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
annotation class RestrictedToJava(val message: String = DEFAULT_MESSAGE) {

    companion object {

        const val DEFAULT_MESSAGE = "this element is hidden to kotlin"

    }

}

