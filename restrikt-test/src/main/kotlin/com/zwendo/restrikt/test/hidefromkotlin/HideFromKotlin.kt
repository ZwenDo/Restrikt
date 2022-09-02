package com.zwendo.restrikt.test.hidefromkotlin

import com.zwendo.restrikt.annotation.HideFromKotlin


val visibleProperty = 5

val visiblePropertyAccessor = ::visibleProperty

@HideFromKotlin
val invisiblePropertyDefaultMessage = "a"

@JvmField
@field:HideFromKotlin
val invisibleFieldDefaultMessage = "b"

val invisiblePropertyDefaultMessageAccessor = ::invisiblePropertyDefaultMessage

const val CUSTOM_MESSAGE = "custom message"

@HideFromKotlin(CUSTOM_MESSAGE)
val invisiblePropertyCustomMessage = 2

val invisiblePropertyCustomMessageAccessor = ::invisiblePropertyCustomMessage

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
fun invisibleFunctionWithDefaultMessage() {

}

val invisibleFunctionDefaultMessageAccessor = ::invisibleFunctionWithDefaultMessage

const val CUSTOM_MESSAGE_2 = "custom message 2"

@HideFromKotlin(CUSTOM_MESSAGE_2)
fun invisibleFunctionCustomMessage() {

}

val invisibleFunctionCustomMessageAccessor = ::invisibleFunctionCustomMessage

const val CUSTOM_MESSAGE_3 = "custom message 3"

class VisibleClass @HideFromKotlin(CUSTOM_MESSAGE_3) constructor() {

    @HideFromKotlin
    constructor(invisible: Int) : this()

    constructor(visible: Any) : this()

    @HideFromKotlin
    companion object

}

@HideFromKotlin
class InvisibleClassDefaultMessage

val invisibleClassDefaultMessageAccessor = InvisibleClassDefaultMessage::class
