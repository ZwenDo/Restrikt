package com.zwendo.restrikt2.gradle

import com.zwendo.restrikt2_gradle_plugin.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

internal class RestriktGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create( // add configuration object to the gradle file
            BuildConfig.EXTENSION_NAME,
            RestriktConfiguration::class.java
        )
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        val extension = project.extensions.findByType(RestriktConfiguration::class.java)
            ?: RestriktConfiguration()

        val parameters = mutableListOf<SubpluginOption>()

        extension.enabled?.let {
            parameters.add(SubpluginOption(BuildConfig.ENABLED, it.toString()))
        }

        extension.toplevelPrivateConstructor?.let {
            parameters += SubpluginOption(BuildConfig.TOPLEVEL_PRIVATE_CONSTRUCTOR, it.toString())
        }

        extension.automaticInternalHiding?.let {
            parameters += SubpluginOption(BuildConfig.AUTOMATIC_INTERNAL_HIDING, it.toString())
        }

        extension.annotationProcessing?.let {
            parameters += SubpluginOption(BuildConfig.ANNOTATION_PROCESSING, it.toString())
        }

        extension.ignoreDefaultAnnotations?.let {
            parameters += SubpluginOption(BuildConfig.IGNORE_DEFAULT_ANNOTATIONS, it.toString())
        }

        extension.hideFromJavaAnnotations.forEach {
            parameters += SubpluginOption(BuildConfig.HIDE_FROM_JAVA_ANNOTATION, it)
        }

        extension.hideFromKotlinAnnotations.forEach {
            parameters += SubpluginOption(BuildConfig.HIDE_FROM_KOTLIN_ANNOTATION, it)
        }

        extension.packagePrivateAnnotations.forEach {
            parameters += SubpluginOption(BuildConfig.PACKAGE_PRIVATE_ANNOTATION, it)
        }

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

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        kotlinCompilation.compilationName != KotlinCompilation.TEST_COMPILATION_NAME

}
