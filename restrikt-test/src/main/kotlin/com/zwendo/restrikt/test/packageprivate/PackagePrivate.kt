package com.zwendo.restrikt.test.packageprivate

import com.zwendo.restrikt.annotation.PackagePrivate
import com.zwendo.restrikt.annotation.RestriktRetention

class VisibleClass

@PackagePrivate
class PackagePrivateClass


fun visibleFunction() {

}

@PackagePrivate
fun packagePrivateFunction() {

}

var visibleProperty = 1

@PackagePrivate
var packagePrivateProperty = 2

@get:PackagePrivate
var propertyWithHiddenGetter = Any()

@set:PackagePrivate
var propertyWithHiddenSetter = Any()

@field:PackagePrivate
var propertyWithHiddenField = Any()


class DummyClass @PackagePrivate constructor() {

    constructor(i: Int) : this()

    @PackagePrivate
    fun foo() {

    }

}

@PackagePrivate(retention = RestriktRetention.RUNTIME)
fun functionWithCustomRuntimeRetention() {

}

@PackagePrivate(retention = RestriktRetention.BINARY)
fun functionWithCustomBinaryRetention() {

}

@PackagePrivate(retention = RestriktRetention.SOURCE)
fun functionWithCustomSourceRetention() {

}
