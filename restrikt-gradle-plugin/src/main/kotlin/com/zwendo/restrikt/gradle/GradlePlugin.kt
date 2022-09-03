package com.zwendo.restrikt.gradle

import com.zwendo.restrikt_gradle_plugin.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

internal class GradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create( // add configuration object to the gradle file
            BuildConfig.EXTENSION_NAME,
            RestriktConfiguration::class.java
        )

        target.dependencies.apply { // add the annotations to the project
            add("implementation", ANNOTATION_DEPENDENCY)
        }
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        val extension = project.extensions.findByType(RestriktConfiguration::class.java)
            ?: RestriktConfiguration()

        val parameters = mutableListOf<SubpluginOption>()

        extension.automaticInternalHiding?.let {
            parameters += SubpluginOption("automatic-internal-hiding", it.toString())
        }

        extension.annotationProcessing?.let {
            parameters += SubpluginOption("annotation-processing", it.toString())
        }

        annotationConfiguration(parameters, "hide-from-java", extension.hideFromJava)
        annotationConfiguration(parameters, "hide-from-kotlin", extension.hideFromKotlin)
        annotationConfiguration(parameters, "package-private", extension.packagePrivate)

        return project.provider { parameters }
    }

    override fun getCompilerPluginId(): String = BuildConfig.PLUGIN_ID

    /**
     * Gets the kotlin compiler plugin associated with this gradle plugin.
     */
    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        BuildConfig.GROUP_ID,
        BuildConfig.COMPILER_PLUGIN_ID,
        BuildConfig.VERSION,
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    private fun annotationConfiguration(
        list: MutableList<SubpluginOption>,
        annotationName: String,
        config: AnnotationConfiguration,
    ) {
        config.enabled?.let {
            list += SubpluginOption("$annotationName-enabled", it.toString())
        }

        config.keepAnnotation?.let {
            list += SubpluginOption("$annotationName-keep-annotation", it.toString())
        }

        config.defaultReason?.let {
            list += SubpluginOption("$annotationName-default-reason", it)
        }
    }

}

private const val ANNOTATION_DEPENDENCY = "${BuildConfig.GROUP_ID}:${BuildConfig.ANNOTATION_ID}:${BuildConfig.VERSION}"
