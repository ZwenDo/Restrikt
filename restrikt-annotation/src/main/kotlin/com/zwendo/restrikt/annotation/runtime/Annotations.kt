package com.zwendo.restrikt.annotation.runtime

/**
 * Runtime mirror of the [HideFromKotlin] annotation.
 *
 * **NOTE**: This annotation is available at runtime only if the annotation retention has been set to `RUNTIME` in the
 * plugin configuration.
 */
@Retention(AnnotationRetention.RUNTIME)
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
annotation class HideFromKotlin(val reason: String)

/**
 * Runtime mirror of the [HideFromJava] annotation.
 *
 * **NOTE**: This annotation is available at runtime only if the annotation retention has been set to `RUNTIME` in the
 * plugin configuration.
 */
@Retention(AnnotationRetention.RUNTIME)
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
annotation class HideFromJava(val reason: String)

/**
 * Runtime mirror of the [PackagePrivate] annotation.
 *
 * **NOTE**: This annotation is available at runtime only if the annotation retention has been set to `RUNTIME` in the
 * plugin configuration.
 */
@Retention(AnnotationRetention.RUNTIME)
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
annotation class PackagePrivate(val reason: String)
