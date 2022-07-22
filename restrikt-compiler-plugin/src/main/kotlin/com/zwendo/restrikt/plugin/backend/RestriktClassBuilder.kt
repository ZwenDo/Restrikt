package com.zwendo.restrikt.plugin.backend

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder

internal class RestriktClassBuilder(private val original: ClassBuilder) : DelegatingClassBuilder() {

    override fun getDelegate(): ClassBuilder = original
}
