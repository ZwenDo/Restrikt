package com.zwendo.restrikt.annotation

/**
 * Runtime mirror of the [HideFromKotlin] annotation.
 *
 * &nbsp;
 *
 * **NOTE**: This annotation is available at runtime, **only if** the annotation retention has been set to `RUNTIME` in
 * the plugin configuration.
 *
 * &nbsp;
 *
 * @param reason The reason why the element is hidden.
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
@RestriktGeneratedAnnotation
@Retention(AnnotationRetention.RUNTIME)
annotation class HideFromKotlinMarker(val reason: String)

/**
 * Runtime mirror of the [HideFromJava] annotation.
 *
 * &nbsp;
 *
 * **NOTE**: This annotation is available at runtime, **only if** the annotation retention has been set to `RUNTIME` in
 * the plugin configuration.
 *
 * &nbsp;
 *
 * @param reason The reason why the element is hidden.
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
@RestriktGeneratedAnnotation
@Retention(AnnotationRetention.RUNTIME)
annotation class HideFromJavaMarker(val reason: String)

/**
 * Runtime mirror of the [PackagePrivate] annotation.
 *
 * &nbsp;
 *
 * **NOTE**: This annotation is available at runtime, **only if** the annotation retention has been set to `RUNTIME` in
 * the plugin configuration.
 *
 * &nbsp;
 *
 * @param reason The reason why the element is hidden.
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
@RestriktGeneratedAnnotation
@Retention(AnnotationRetention.RUNTIME)
annotation class PackagePrivateMarker(val reason: String)
