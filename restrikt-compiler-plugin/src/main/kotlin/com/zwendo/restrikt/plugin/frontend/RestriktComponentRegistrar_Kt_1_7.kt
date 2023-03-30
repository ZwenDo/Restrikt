package com.zwendo.restrikt.plugin.frontend

import com.zwendo.restrikt.plugin.Logger
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings


@OptIn(ExperimentalCompilerApi::class)
internal class RestriktComponentRegistrar_Kt_1_7 : @Suppress("DEPRECATION") ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        if (configuration.languageVersionSettings.languageVersion.minor > 7) return

        if (!PluginConfiguration.enabled) return

        ClassBuilderInterceptorExtension.registerExtension(project, ClassGenerationInterceptor)
    }

}
