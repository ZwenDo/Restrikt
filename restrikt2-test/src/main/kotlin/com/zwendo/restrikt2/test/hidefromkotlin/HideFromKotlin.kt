package com.zwendo.restrikt2.test.hidefromkotlin

import com.zwendo.restrikt2.test.HFK


val visiblePropertyHFK = 5

val visiblePropertyAccessor = ::visiblePropertyHFK

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

    @HFK
    private fun privateFunction() {

    }

}

val visibleClassAccessor = VisibleClass::class

@HFK
class InvisibleClass {

    constructor()

    fun publicFunction() {

    }

    var publicProperty = 5

    companion object

    class InnerClass

}

val invisibleClassAccessor = InvisibleClass::class


annotation class VisibleAnnotation

val visibleAnnotationAccessor = VisibleAnnotation::class

@HFK
annotation class InvisibleAnnotation

val invisibleAnnotationAccessor = InvisibleAnnotation::class
