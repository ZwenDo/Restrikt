package com.zwendo.restrikt.plugin.frontend

import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal interface Option<T> {

    val name: String

    val callback: OptionCallback

    val key: CompilerConfigurationKey<T>

    val cliOption: CliOption
}

internal object Enabled : Option<Boolean> {

    override val name = "enabled"

    override val callback: OptionCallback = { value, configuration ->
        configuration.put(key, value.toBooleanStrict())
    }

    override val key = CompilerConfigurationKey<Boolean>(name)

    override val cliOption = CliOption(
        name,
        "<true|false>",
        "whether plugin is enabled",
        required = false,
        allowMultipleOccurrences = false
    )
}


internal val OPTIONS = listOf<Option<*>>(
    Enabled
)

internal val NAME_TO_OPTION = OPTIONS.associateBy { it.name }

private typealias OptionCallback = (String, CompilerConfiguration) -> Unit
