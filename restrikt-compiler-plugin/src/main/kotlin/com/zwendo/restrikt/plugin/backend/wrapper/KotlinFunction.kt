package com.zwendo.restrikt.plugin.backend.wrapper

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import kotlinx.metadata.Flag
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction
import kotlinx.metadata.jvm.signature

internal interface KotlinFunction {

    fun isSynthetic(originClass: KotlinClass): Boolean

    companion object {

        fun new(inner: KmFunction): KotlinFunction = Impl(
            "${inner.signature?.asString()}",
            Flag.IS_INTERNAL(inner.flags)
        )

        fun new(inner: KmConstructor): KotlinFunction = Impl(
            "<init>${inner.signature?.asString()}",
            Flag.IS_INTERNAL(inner.flags)
        )

        fun new(name: String): KotlinFunction = Impl(name, false)

    }

    private class Impl(
        private val innerName: String,
        private val isInternal: Boolean,
    ) : KotlinFunction {

        override fun isSynthetic(originClass: KotlinClass): Boolean =
            (PluginConfiguration.automaticInternalHiding && isInternal) || originClass.isForceSynthetic(innerName)

    }

}

fun printIntBytes(i: Int) {
    repeat(32) {
        val bit = i and (1 shl it) != 0
        if (it % 8 == 0 && it != 0) {
            print(" ")
        }
        print(if (bit) "1" else "0")
    }
}

fun main() {
    val v = 0.inv() ushr 3
    printIntBytes(v)
}
