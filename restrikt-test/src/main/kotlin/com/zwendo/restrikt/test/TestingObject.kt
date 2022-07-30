package com.zwendo.restrikt.test

import com.zwendo.restrikt.annotation.HideFromKotlin

internal object TestingObject {

    object VisibleClass

    fun visibleFunction() {}

    val visibleProperty = Any()

    @HideFromKotlin
    object HiddenClass

    @HideFromKotlin("this function is hidden")
    fun hiddenFunction() {
    }

    @HideFromKotlin("hidden property")
    val hiddenProperty = Any()

}
