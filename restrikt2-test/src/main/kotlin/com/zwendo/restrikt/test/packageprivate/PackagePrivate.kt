package com.zwendo.restrikt.test.packageprivate

import com.zwendo.restrikt.test.PP

class VisibleClass

@PP
class PackagePrivateClass


fun visibleFunction() {

}

@PP
fun packagePrivateFunction() {

}

var visibleProperty = 1

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
