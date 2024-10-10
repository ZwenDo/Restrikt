package com.zwendo.restrikt.test.hidefromkotlin

import com.zwendo.restrikt.test.HFK


val visibleProperty = 5

val visiblePropertyAccessor = ::visibleProperty

@HFK
val invisibleProperty = "a"

@JvmField
@field:HFK
val invisibleField = "b"

val invisiblePropertyAccessor = ::invisibleProperty

@get:HFK
var invisiblePropertyGetter = 3

val invisiblePropertyGetterAccessor = ::invisiblePropertyGetter

@set:HFK
var invisiblePropertySetter = 18

val invisiblePropertySetterAccessor = ::invisiblePropertySetter

fun visibleFunction() {

}

val visibleFunctionAccessor = ::visibleFunction

@HFK
fun invisibleFunction() {

}

val invisibleFunctionAccessor = ::invisibleFunction


class VisibleClass {

    @HFK
    constructor(invisible: Int)

    constructor()

    @HFK
    companion object

}

val visibleClassAccessor = VisibleClass::class

@HFK
class InvisibleClass

val invisibleClassAccessor = InvisibleClass::class


annotation class VisibleAnnotation

val visibleAnnotationAccessor = VisibleAnnotation::class

@HFK
annotation class InvisibleAnnotation

val invisibleAnnotationAccessor = InvisibleAnnotation::class
