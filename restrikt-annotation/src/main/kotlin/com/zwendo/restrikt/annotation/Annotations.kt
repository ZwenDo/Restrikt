package com.zwendo.restrikt.annotation

/**
 * This annotation is used to hide elements from kotlin sources.
 *
 * Used with the [Restrikt compiler plugin](https://github.com/ZwenDo/Restrikt), this annotation causes the generation
 * of the [Deprecated] annotation with the level [DeprecationLevel.HIDDEN], which hides the element for Kotlin sources.
 * However, elements will still be accessible at runtime from compiled Kotlin files.
 *
 * &nbsp;
 *
 * Like [JvmSynthetic] acts to hide elements to Java, this annotation is intended for API designers who want to hide a
 * Java-specific target from Kotlin language, in order to keep an idiomatic API for both languages.
 *
 * &nbsp;
 *
 * **NOTE**: This annotation retention is set to `SOURCE`, so it will not be available at runtime. As an alternative,
 * you can use the [HideFromKotlinMarker] annotation, which is available at runtime.
 *
 * &nbsp;
 *
 * @param reason The reason why the element is hidden. This will be used as the message of the [Deprecated] annotation,
 * and also as the message of the [HideFromKotlinMarker] annotation.
 * @param generation The generation policy for the [HideFromKotlinMarker] annotation.
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
annotation class HideFromKotlin(
    val reason: String = "",
    val generation: AnnotationGeneration = AnnotationGeneration.DEFAULT,
)

/**
 * Used with the [Restrikt compiler plugin](https://github.com/ZwenDo/Restrikt), this annotation acts like
 * [JvmSynthetic] annotation, but can be used on all symbol declarations.
 *
 * &nbsp;
 *
 * **NOTE**: This annotation retention is set to `SOURCE`, so it will not be available at runtime. As an alternative,
 * you can use the [HideFromJavaMarker] annotation, which is available at runtime.
 *
 * &nbsp;
 *
 * @param reason The reason why the element is hidden. This will be used as the message of the [HideFromJavaMarker]
 * annotation.
 * @param generation The generation policy for the [HideFromJavaMarker] annotation.
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
annotation class HideFromJava(
    val reason: String = "",
    val generation: AnnotationGeneration = AnnotationGeneration.DEFAULT,
)

/**
 * Used with the [Restrikt compiler plugin](https://github.com/ZwenDo/Restrikt), this annotation is used to force
 * `package-private` visibility on Kotlin symbols.
 *
 * &nbsp;
 *
 * **NOTE**: the original visibility will be overridden by the compiler plugin.
 *
 * &nbsp;
 *
 * **NOTE**: This annotation retention is set to `SOURCE`, so it will not be available at runtime. As an alternative,
 * you can use the [PackagePrivateMarker] annotation, which is available at runtime.
 *
 * &nbsp;
 *
 * @param reason The reason why the element is hidden. This will be used as the message of the [PackagePrivateMarker]
 * annotation.
 * @param generation The generation policy for the [PackagePrivateMarker] annotation.
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
annotation class PackagePrivate(
    val reason: String = "",
    val generation: AnnotationGeneration = AnnotationGeneration.DEFAULT,
)

/**
 * Runtime annotation generation policy.
 */
enum class AnnotationGeneration {

    /**
     * Use the default policy defined in the plugin configuration.
     */
    DEFAULT,

    /**
     * Do not generate the marker annotation.
     */
    NONE,

    /**
     * Generate the annotation with the retention policy [AnnotationRetention.BINARY].
     */
    BINARY,

    /**
     * Generate the annotation with the retention policy [AnnotationRetention.RUNTIME].
     */
    RUNTIME,
    ;

}
