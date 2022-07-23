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

    override val pluginOptions = OPTIONS.map(Option<*>::cliOption)

    /**
     * Function called for each option encountered
     */
    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) = NAME_TO_OPTION[option.optionName]?.callback?.invoke(value, configuration)
        ?: throw CliOptionProcessingException("Unexpected option: ${option.optionName}")

}
