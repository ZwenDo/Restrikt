package com.zwendo.restrikt2.test.misc

import com.zwendo.restrikt2.test.HFK
import com.zwendo.restrikt2.test.PP
import java.util.function.Consumer


private fun foo(block: String.() -> Unit) {}

@HFK
fun hideFromKotlinWithAnonymousObject(block: Consumer<String>): Unit = foo { block.accept(this) }


// Should not compile if there is a bug with the PP enums
@PP
internal object Bar {
    enum class Toto {
        TOTO
    }

    fun sout() {
        println(Toto.TOTO)
    }
}
