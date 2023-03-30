package com.zwendo.restrikt.annotation

/**
 * This annotation is used to hide elements from kotlin sources.
 *
 * Used with the [Restrikt compiler plugin](https://github.com/ZwenDo/Restrikt), this annotation causes the retention
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
 * @param reason The reason why the element is hidden. This will be used as the message of the [Deprecated] annotation.
 * @param retention The retention policy of the annotation.
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
@Retention(AnnotationRetention.RUNTIME)
annotation class HideFromKotlin(
    val reason: String = "",
    val retention: RestriktRetention = RestriktRetention.DEFAULT,
)

/**
 * Used with the [Restrikt compiler plugin](https://github.com/ZwenDo/Restrikt), this annotation acts like
 * [JvmSynthetic] annotation, but can be used on all symbol declarations.
 *
 * &nbsp;
 *
 * @param reason The reason why the element is hidden.
 * annotation.
 * @param retention The retention policy of the annotation.
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
@Retention(AnnotationRetention.RUNTIME)
annotation class HideFromJava(
    val reason: String = "",
    val retention: RestriktRetention = RestriktRetention.DEFAULT,
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
 * @param reason The reason why the element is hidden.
 * @param retention The retention policy of the annotation.
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
@Retention(AnnotationRetention.RUNTIME)
annotation class PackagePrivate(
    val reason: String = "",
    val retention: RestriktRetention = RestriktRetention.DEFAULT,
)

/**
 * Runtime annotation retention policy.
 */
enum class RestriktRetention {

    /**
     * Use the default policy defined in the plugin configuration.
     */
    DEFAULT,

    /**
     * The annotation will have the retention policy [AnnotationRetention.SOURCE].
     */
    SOURCE,

    /**
     * The annotation will have the retention policy [AnnotationRetention.BINARY].
     */
    BINARY,

    /**
     * The annotation will have the retention policy [AnnotationRetention.RUNTIME].
     */
    RUNTIME,
    ;

}
