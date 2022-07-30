package com.zwendo.restrikt.plugin.frontend

import org.jetbrains.kotlin.compiler.plugin.CliOption

internal enum class Option {

    Enabled {

        override val optionName = "enabled"

        override val cliOption = CliOption(
            optionName,
            "<true|false>",
            "whether plugin is enabled",
            required = false,
            allowMultipleOccurrences = false
        )

        override val action: (String) -> Unit = { PluginConfiguration.enabled = it.toBooleanStrict() }

    },

    KeepAnnotations {

        override val optionName = "keepAnnotations"

        override val cliOption = CliOption(
            optionName,
            "<true|false>",
            "whether to keep plugin annotations",
            required = false,
            allowMultipleOccurrences = false
        )

        override val action: (String) -> Unit = { PluginConfiguration.keepAnnotations = it.toBooleanStrict() }

    },

    DefaultReason {

        override val optionName = "defaultReason"

        override val cliOption = CliOption(
            optionName,
            "[reason]",
            "default reason on generated annotations",
            required = false,
            allowMultipleOccurrences = false
        )

        override val action: (String) -> Unit = {
            PluginConfiguration.defaultReason = it
        }

    },

    ;

    abstract val optionName: String

    abstract val cliOption: CliOption

    abstract val action: (String) -> Unit

    companion object {

        val CLI_OPTIONS = values().map { it.cliOption }

        val NAME_TO_OPTION = values().associateBy { it.optionName }

    }

}
