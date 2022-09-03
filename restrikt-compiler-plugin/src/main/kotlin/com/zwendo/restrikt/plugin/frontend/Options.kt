package com.zwendo.restrikt.plugin.frontend

import com.zwendo.restrikt_compiler_plugin.BuildConfig
import org.jetbrains.kotlin.compiler.plugin.CliOption

internal sealed interface Option {

    val optionName: String

    val cliOption: CliOption

    val action: (String) -> Unit

    companion object {

        private val OPTIONS = listOf(
            AutomaticInternalHiding,
            AnnotationProcessing,
            *annotationConfiguration(BuildConfig.HIDE_FROM_JAVA, PluginConfiguration.hideFromJava),
            *annotationConfiguration(BuildConfig.HIDE_FROM_KOTLIN, PluginConfiguration.hideFromKotlin),
            *annotationConfiguration(BuildConfig.PACKAGE_PRIVATE, PluginConfiguration.packagePrivate),
        )

        val CLI_OPTIONS = OPTIONS.map { it.cliOption }

        val NAME_TO_OPTION = OPTIONS.associateBy { it.optionName }

    }

}


private object AutomaticInternalHiding : Option {

    override val optionName = BuildConfig.AUTOMATIC_INTERNAL_HIDING

    override val cliOption = CliOption(
        optionName,
        "<true|false>",
        "whether internal symbols should be hidden automatically",
        required = false,
        allowMultipleOccurrences = false
    )

    override val action: (String) -> Unit = { PluginConfiguration.automaticInternalHiding = it.toBooleanStrict() }

}


private object AnnotationProcessing : Option {

    override val optionName = BuildConfig.ANNOTATION_PROCESSING

    override val cliOption = CliOption(
        optionName,
        "<true|false>",
        "whether the plugin should process annotations",
        required = false,
        allowMultipleOccurrences = false
    )

    override val action: (String) -> Unit = { PluginConfiguration.annotationProcessing = it.toBooleanStrict() }

}


private class AnnotationEnabled(
    annotationName: String,
    annotationConfiguration: PluginConfiguration.AnnotationConfiguration,
) : Option {

    override val optionName = "$annotationName-${BuildConfig.ANNOTATION_POSTFIX_ENABLED}"

    override val cliOption = CliOption(
        optionName,
        "<true|false>",
        "whether the $annotationName annotation should be enabled",
        required = false,
        allowMultipleOccurrences = false
    )

    override val action: (String) -> Unit = { annotationConfiguration.enabled = it.toBooleanStrict() }

}


private class AnnotationKeeping(
    annotationName: String,
    annotationConfiguration: PluginConfiguration.AnnotationConfiguration,
) : Option {

    override val optionName = "keep-$annotationName-${BuildConfig.ANNOTATION_POSTFIX_KEEP_ANNOTATION}"

    override val cliOption = CliOption(
        optionName,
        "<true|false>",
        "whether the plugin should keep the $annotationName annotation",
        required = false,
        allowMultipleOccurrences = false
    )

    override val action: (String) -> Unit = { annotationConfiguration.keepAnnotation = it.toBooleanStrict() }
}


private class AnnotationDefaultReason(
    annotationName: String,
    annotationConfiguration: PluginConfiguration.AnnotationConfiguration,
) : Option {

    override val optionName = "$annotationName-${BuildConfig.ANNOTATION_POSTFIX_DEFAULT_REASON}"

    override val cliOption = CliOption(
        optionName,
        "<reason>",
        "the default reason for the $annotationName annotation",
        required = false,
        allowMultipleOccurrences = false
    )

    override val action: (String) -> Unit = { annotationConfiguration.defaultReason = it }
}


private fun annotationConfiguration(
    annotationName: String,
    configuration: PluginConfiguration.AnnotationConfiguration,
) = arrayOf(
    AnnotationEnabled(annotationName, configuration),
    AnnotationKeeping(annotationName, configuration),
    AnnotationDefaultReason(annotationName, configuration),
)
