package com.zwendo.restrikt2.test.packageprivate

import com.zwendo.restrikt2.test.PP

class VisibleClass

@PP
class PackagePrivateClass


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

@field:PP
var propertyWithHiddenField = Any()


class DummyClass @PP constructor() {

    constructor(i: Int) : this()

}
