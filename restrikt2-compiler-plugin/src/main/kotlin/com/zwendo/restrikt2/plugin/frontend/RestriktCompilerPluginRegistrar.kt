@file:OptIn(ExperimentalCompilerApi::class)

package com.zwendo.restrikt2.plugin.frontend

import com.zwendo.restrikt2.plugin.backend.RestriktIrElementTransformer
import com.zwendo.restrikt2.plugin.backend.RestriktPackagePrivateChecker
import com.zwendo.restrikt2_compiler_plugin.BuildConfig
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

/**
 * Class that registers plugin custom class generation interceptor
 */
internal class RestriktCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = BuildConfig.PLUGIN_ID

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (!PluginConfiguration.enabled) return

        FirExtensionRegistrarAdapter.registerExtension(RestriktFirExtensionRegistrar)
        IrGenerationExtension.registerExtension(RestriktIrGenerationExtension)
    }

}

private object RestriktIrGenerationExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext): Unit =
        RestriktIrElementTransformer(pluginContext).processModule(moduleFragment)

}

private object RestriktFirExtensionRegistrar : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        registerDiagnosticContainers(RestriktPackagePrivateChecker.Lazy)
        +::RestriktPackagePrivateChecker
    }
}
