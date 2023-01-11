package com.zwendo.restrikt.plugin.frontend

import com.zwendo.restrikt_compiler_plugin.BuildConfig
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * Class that process command line to retrieve the configuration of the plugin.
 */
internal class RestriktCommandLineProcessor : CommandLineProcessor {

    override val pluginId = BuildConfig.PLUGIN_ID

    /**
     * List of options that can be used by the plugin.
     */
    override val pluginOptions
        get() = Option.OPTIONS

    /**
     * Function called for each option encountered
     */
    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        val opt = Option.NAME_TO_OPTION[option.optionName]
            ?: throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        try {
            opt.applyToCompilation(value)
        } catch (e: Exception) {
            throw CliOptionProcessingException(
                "Invalid value for option ${option.optionName} expected ${option.valueDescription} but was $value",
            )
        }
    }

}

