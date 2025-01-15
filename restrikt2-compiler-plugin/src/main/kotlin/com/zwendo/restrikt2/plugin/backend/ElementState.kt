package com.zwendo.restrikt2.plugin.backend

/**
 * Using a value class here is useless because instances will be passed through visitor methods, requiring a lot of
 * boxing and unboxing.
 */
internal class ElementState(val value: Int, val kind: ParentKind) {

    val isInternal: Boolean
        get() = !hasPackagePrivate && (value and IS_INTERNAL != 0)

    val hasPackagePrivate: Boolean
        get() = value and HAS_PACKAGE_PRIVATE != 0

    val hasHideFromJava: Boolean
        get() = value and HAS_HIDE_FROM_JAVA != 0

    val hasHideFromKotlin: Boolean
        get() = value and HAS_HIDE_FROM_KOTLIN != 0

    fun defaultIfFileState(): ElementState =
        if (ParentKind.FILE == kind) {
            ElementState(DEFAULT, ParentKind.OTHER)
        } else {
            this
        }

    enum class ParentKind {
        FILE,
        PROPERTY,
        FIELD,
        OTHER,
        ;

        val isFileOrProperty: Boolean
            get() = this == FILE || this == PROPERTY
    }

    override fun equals(other: Any?): Boolean =
        other is ElementState && value == other.value && kind == other.kind

    override fun hashCode(): Int = value * 31 + kind.hashCode()


    companion object {

        const val DEFAULT = 0

        const val IS_INTERNAL = 1

        const val HAS_PACKAGE_PRIVATE = 2

        const val HAS_HIDE_FROM_JAVA = 4

        const val HAS_HIDE_FROM_KOTLIN = 8

        val IGNORE_STATE = ElementState(DEFAULT, ParentKind.OTHER)

    }

}
