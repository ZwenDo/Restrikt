package com.zwendo.restrikt.plugin.frontend

import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object EnabledOption {

    const val name = "enabled"

    val key = CompilerConfigurationKey<Boolean>(name)

    val action: (String, CompilerConfiguration) -> Unit = { value, config ->
        config.put(key, value.toBooleanStrict())
    }

    val cliOption = CliOption(
        name,
        "<true|false>",
        "whether plugin is enabled",
        required = false,
        allowMultipleOccurrences = false
    )

}
