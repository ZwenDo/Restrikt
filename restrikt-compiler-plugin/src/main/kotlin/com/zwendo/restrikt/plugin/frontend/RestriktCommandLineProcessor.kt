package com.zwendo.restrikt.plugin.frontend

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * Class that process command line to retrieve the configuration of the plugin.
 */
internal class RestriktCommandLineProcessor : CommandLineProcessor {

    override val pluginId = "restrikt" // never change this

    /**
     * List of options that can be used by the plugin.
     */
    override val pluginOptions = listOf(EnabledOption.cliOption)

    /**
     * Function called for each option encountered
     */
    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) = when (option.optionName) {
        EnabledOption.name -> EnabledOption.action(value, configuration)
        else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
    }

}

