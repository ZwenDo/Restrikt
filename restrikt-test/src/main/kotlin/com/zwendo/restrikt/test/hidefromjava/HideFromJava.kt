package com.zwendo.restrikt.test.hidefromjava

import com.zwendo.restrikt.annotation.HideFromJava

class VisibleClass

@HideFromJava
class InvisibleClass

var visibleProperty = 5

@HideFromJava
var invisibleProperty = 5

@get:HideFromJava
var invisiblePropertyGetter = "foo"

@set:HideFromJava
var invisiblePropertySetter = 4894

@JvmField
@HideFromJava
var invisibleField = Any()


fun visibleFunction() {

}

@HideFromJava
fun invisibleFunction() {

}

class VisibleConstructorClass

class InvisibleConstructorClass @HideFromJava constructor()


annotation class VisibleAnnotation

@HideFromJava
annotation class InvisibleAnnotation
