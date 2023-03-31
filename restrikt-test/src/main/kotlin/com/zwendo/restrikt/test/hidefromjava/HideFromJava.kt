package com.zwendo.restrikt.test.hidefromjava

import com.zwendo.restrikt.annotation.HideFromJava
import com.zwendo.restrikt.annotation.RestriktRetention

class VisibleClass

@HideFromJava
class InvisibleClass {

    fun invisibleFunctionDueToClassAnnotation() {
    }

    class NestedClass {
        fun nestedClassFunction() {
        }
    }

}

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

@HideFromJava(retention = RestriktRetention.RUNTIME)
fun functionWithCustomRuntimeRetention() {

}

@HideFromJava(retention = RestriktRetention.BINARY)
fun functionWithCustomBinaryRetention() {

}

@HideFromJava(retention = RestriktRetention.SOURCE)
fun functionWithCustomSourceRetention() {

}
