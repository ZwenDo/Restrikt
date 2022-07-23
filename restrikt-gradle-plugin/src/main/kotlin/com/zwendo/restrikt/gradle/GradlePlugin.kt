package com.zwendo.restrikt.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

internal class GradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create( // add configuration object to the gradle file
            EXTENSION_NAME,
            RestriktConfiguration::class.java
        )

        target.dependencies.apply { // add the annotations to the project
            add("implementation", "$GROUP_ID:$ANNOTATION_ID:$VERSION")
        }
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        val extension = project.extensions.findByType(RestriktConfiguration::class.java) ?: RestriktConfiguration()

        val isEnabled = SubpluginOption("enabled", extension.enabled.toString())

        return project.provider {
            listOf(isEnabled)
        }
    }

    override fun getCompilerPluginId(): String = PLUGIN_ID

    /**
     * Gets the kotlin compiler plugin associated with this gradle plugin.
     */
    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        GROUP_ID,
        COMPILER_PLUGIN_ID,
        VERSION,
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = kotlinCompilation.target
        .project
        .plugins
        .hasPlugin("$GROUP_ID.$PLUGIN_ID")

}

private const val EXTENSION_NAME = "restrikt"
private const val GROUP_ID = "com.zwendo"
private const val COMPILER_PLUGIN_ID = "restrikt-compiler-plugin"
private const val PLUGIN_ID = "restrikt"
private const val ANNOTATION_ID = "restrikt-annotations"
private const val VERSION = "0.1.0"
