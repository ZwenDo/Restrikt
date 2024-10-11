package com.zwendo.restrikt2.test.hidefromjava

import com.zwendo.restrikt2.test.HFJ
import com.zwendo.restrikt2.test.PP

class VisibleClass

@HFJ
class InvisibleClass {

    fun invisibleFunctionDueToClassAnnotation() {
    }

    class NestedClass {
        fun nestedClassFunction() {
        }
    }

}

var visiblePropertyHFJ = 5

@HFJ
var invisibleProperty = 5

@get:HFJ
var invisiblePropertyGetter = "foo"

@set:HFJ
var invisiblePropertySetter = 4894

@JvmField
@HFJ
var invisibleField = Any()


fun visibleFunction() {

}

@HFJ
fun invisibleFunction() {

}

class VisibleConstructorClass

class InvisibleConstructorClass @HFJ constructor()

@HFJ
fun functionWithCustomRuntimeRetention() {

}

@HFJ
fun functionWithCustomBinaryRetention() {

}

@HFJ
fun functionWithCustomSourceRetention() {

}
