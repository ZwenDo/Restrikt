package com.zwendo.restrikt.test.hidefromkotlin

import com.zwendo.restrikt.annotation.HideFromKotlin
import com.zwendo.restrikt.annotation.RestriktRetention


val visibleProperty = 5

val visiblePropertyAccessor = ::visibleProperty

@HideFromKotlin
val invisibleProperty = "a"

@JvmField
@field:HideFromKotlin
val invisibleField = "b"

val invisiblePropertyAccessor = ::invisibleProperty

@get:HideFromKotlin
var invisiblePropertyGetter = 3

val invisiblePropertyGetterAccessor = ::invisiblePropertyGetter

@set:HideFromKotlin
var invisiblePropertySetter = 18

val invisiblePropertySetterAccessor = ::invisiblePropertySetter

fun visibleFunction() {

}

val visibleFunctionAccessor = ::visibleFunction

@HideFromKotlin
fun invisibleFunction() {

}

val invisibleFunctionAccessor = ::invisibleFunction


class VisibleClass {

    @HideFromKotlin
    constructor(invisible: Int)

    constructor()

    @HideFromKotlin
    companion object

}

val visibleClassAccessor = VisibleClass::class

@HideFromKotlin
class InvisibleClass

val invisibleClassAccessor = InvisibleClass::class


annotation class VisibleAnnotation

val visibleAnnotationAccessor = VisibleAnnotation::class

@HideFromKotlin
annotation class InvisibleAnnotation

val invisibleAnnotationAccessor = InvisibleAnnotation::class

@HideFromKotlin(retention = RestriktRetention.RUNTIME)
fun invisibleFunctionWithRuntimeRetention() {

}

val invisibleFunctionWithRuntimeRetentionAccessor = ::invisibleFunctionWithRuntimeRetention

@HideFromKotlin(retention = RestriktRetention.BINARY)
fun invisibleFunctionWithBinaryRetention() {

}

val invisibleFunctionWithBinaryRetentionAccessor = ::invisibleFunctionWithBinaryRetention

@HideFromKotlin(retention = RestriktRetention.SOURCE)
fun invisibleFunctionWithSourceRetention() {

}

val invisibleFunctionWithSourceRetentionAccessor = ::invisibleFunctionWithSourceRetention

