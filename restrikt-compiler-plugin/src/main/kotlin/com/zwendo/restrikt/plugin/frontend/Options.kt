package com.zwendo.restrikt.plugin.frontend

import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object EnabledOption {

    const val name = "enabled"

    val key = CompilerConfigurationKey<Boolean>(name)

    val cliOption = CliOption(
        name,
        "<true|false>",
        "whether plugin is enabled",
        required = false,
        allowMultipleOccurrences = false
    )

}

internal object KeepAnnotationsOption {

    const val name = "keepAnnotations"

    val key = CompilerConfigurationKey<Boolean>(name)

    val cliOption = CliOption(
        name,
        "<true|false>",
        "whether to keep plugin annotations",
        required = false,
        allowMultipleOccurrences = false
    )

    const val default = true

}
