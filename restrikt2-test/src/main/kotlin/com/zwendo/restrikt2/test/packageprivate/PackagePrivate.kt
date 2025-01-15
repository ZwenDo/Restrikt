package com.zwendo.restrikt2.test.packageprivate

import com.zwendo.restrikt2.test.HFJ
import com.zwendo.restrikt2.test.PP

class VisibleClass

@PP
class PackagePrivateClass {

    fun publicFunction() {

    }

    constructor()

    var publicProperty = 5

    companion object

    class InnerClass

}


fun visibleFunction() {

}

@PP
fun packagePrivateFunction() {

}


var visiblePropertyPP = 1

@PP
var packagePrivateProperty = 2

@get:PP
var propertyWithHiddenGetter = Any()

@set:PP
var propertyWithHiddenSetter = Any()

class DummyClass @PP constructor() {

    constructor(i: Int) : this()

}

@PP
@HFJ
fun ppFunctionWithHfj() {

}

@PP
internal fun packagePrivateFunctionWithInternal() {

}

class Foo {

    @PP
    private fun privateFunction() {

    }

}
