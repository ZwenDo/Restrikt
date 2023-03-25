package com.zwendo.restrikt.annotation

/**
 * This annotation is used to hide elements from kotlin sources.
 *
 * Used with the [Restrikt compiler plugin](https://github.com/ZwenDo/Restrikt), this annotation causes the generation
 * of the [Deprecated] annotation with the level [DeprecationLevel.HIDDEN], which hides the element for Kotlin sources.
 * However, elements will still be accessible at runtime from compiled Kotlin files.
 *
 * Like [JvmSynthetic] acts to hide elements to Java, this annotation is intended for API designers who want to hide a
 * Java-specific target from Kotlin language, in order to keep an idiomatic API for both languages.
 *
 * **NOTE**: This annotation retention is set to `SOURCE` by default, so it will not be available at runtime. As an
 * alternative, you can use the [com.zwendo.restrikt.annotation.runtime.HideFromKotlin] annotation, which is available
 * at runtime.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class HideFromKotlin(val reason: String = "")

/**
 * Used with the [Restrikt compiler plugin](https://github.com/ZwenDo/Restrikt), this annotation acts like
 * [JvmSynthetic] annotation, but can be used on all symbol declarations.
 *
 * **NOTE**: This annotation retention is set to `SOURCE` by default, so it will not be available at runtime. As an
 * alternative, you can use the [com.zwendo.restrikt.annotation.runtime.HideFromJava] annotation, which is available
 * at runtime.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FILE,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class HideFromJava(val reason: String = "")

/**
 * Used with the [Restrikt compiler plugin](https://github.com/ZwenDo/Restrikt), this annotation is used to force
 * `package-private` visibility on Kotlin symbols.
 *
 * **NOTE**: the original visibility will be overridden by the compiler plugin.
 *
 * **NOTE**: This annotation retention is set to `SOURCE` by default, so it will not be available at runtime. As an
 * alternative, you can use the [com.zwendo.restrikt.annotation.runtime.PackagePrivate] annotation, which is available
 * at runtime.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FILE,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class PackagePrivate(val reason: String = "")
