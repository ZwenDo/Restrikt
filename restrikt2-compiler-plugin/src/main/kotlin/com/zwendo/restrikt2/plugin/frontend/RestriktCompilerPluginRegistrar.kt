package com.zwendo.restrikt2.plugin.frontend

import com.zwendo.restrikt2.plugin.backend.RestriktPackagePrivateChecker
import com.zwendo.restrikt2.plugin.backend.RestriktAnnotationProcessor
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.platform.jvm.isJvm

/**
 * Class that registers plugin custom class generation interceptor
 */
@OptIn(ExperimentalCompilerApi::class)
internal class RestriktCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (!PluginConfiguration.enabled) return

        FirExtensionRegistrarAdapter.registerExtension(RestriktFirExtensionRegistrar)
        IrGenerationExtension.registerExtension(RestriktIrGenerationExtension)
    }

}

private object RestriktIrGenerationExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext): Unit =
        RestriktAnnotationProcessor(pluginContext).processModule(moduleFragment)

}

private object RestriktFirExtensionRegistrar : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +::RestriktPackagePrivateChecker
    }

}
