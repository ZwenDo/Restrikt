package com.zwendo.restrikt.plugin.backend

/**
 * Using a value class here is bad because instances will be passed through visitor methods, requiring a lot of boxing
 * and unboxing.
 */
internal class ParentState(val value: Int) {

    val isInternal: Boolean
        get() = value and IS_INTERNAL != 0

    val hasPackagePrivate: Boolean
        get() = value and HAS_PACKAGE_PRIVATE != 0

    val hasHideFromJava: Boolean
        get() = value and HAS_HIDE_FROM_JAVA != 0

    val hasHideFromKotlin: Boolean
        get() = value and HAS_HIDE_FROM_KOTLIN != 0

    companion object {

        const val DEFAULT = 0

        const val IS_INTERNAL = 1

        const val HAS_PACKAGE_PRIVATE = 2

        const val HAS_HIDE_FROM_JAVA = 4

        const val HAS_HIDE_FROM_KOTLIN = 8

    }

}
