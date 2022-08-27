package com.zwendo.restrikt.test

import kotlin.reflect.KClass


open class InternalTestClass {

    //region internal
    internal companion object {

    }

    internal class NestedInternalClass

    internal var internalProperty = 1


    internal fun internalFunction() {

    }
    //endregion

    //region private
    private class NestedPrivateClass

    val nestedPrivateClassAccessor: KClass<*> = NestedPrivateClass::class

    private var privateProperty = 1

    val privatePropertyAccessor = ::privateProperty

    private fun privateFunction() {

    }

    val privateFunctionAccessor = ::privateFunction
    //endregion

    //region protected
    protected class NestedProtectedClass

    val nestedProtectedClassAccessor: KClass<*> = NestedProtectedClass::class

    protected var protectedProperty = 1

    val protectedPropertyAccessor = ::protectedProperty

    protected fun protectedFunction() {

    }

    val protectedFunctionAccessor = ::protectedFunction
    //endregion

    //region public
    class NestedPublicClass

    var publicProperty = 1

    fun publicFunction() {

    }
    //endregion

}


internal class InternalClass

private class PrivateClass {

    private companion object

}

val privateClassAccessor: KClass<*> = PrivateClass::class

class PublicClass {

    companion object

}

internal var internalProperty = 3

internal fun internalFunction() {

}

private var privateProperty = 3

val privatePropertyAccessor = ::privateProperty

private fun privateFunction() {

}

val privateFunctionAccessor = ::privateFunction

var publicProperty = 3

fun publicFunction() {

}
