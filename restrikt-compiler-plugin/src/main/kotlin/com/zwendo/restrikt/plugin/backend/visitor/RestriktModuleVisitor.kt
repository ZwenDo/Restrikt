package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.RestriktContext as context
import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import org.jetbrains.org.objectweb.asm.ModuleVisitor

/**
 * Restrikt visitor in charge of visiting modules.
 */
internal class RestriktModuleVisitor(factory: () -> ModuleVisitor) : ModuleVisitor(ASM_VERSION) {

    private val original: ModuleVisitor by lazy { factory() }

    override fun visitMainClass(mainClass: String?) = context.addAction { original.visitMainClass(mainClass) }

    override fun visitPackage(packaze: String?) = context.addAction { original.visitPackage(packaze) }

    override fun visitRequire(module: String?, access: Int, version: String?) = context.addAction {
        original.visitRequire(module, access, version)
    }

    override fun visitExport(packaze: String?, access: Int, vararg modules: String?) = context.addAction {
        original.visitExport(packaze, access, *modules)
    }

    override fun visitOpen(packaze: String?, access: Int, vararg modules: String?) = context.addAction {
        original.visitOpen(packaze, access, *modules)
    }

    override fun visitUse(service: String?) = original.visitUse(service)

    override fun visitProvide(service: String?, vararg providers: String?) = original.visitProvide(service, *providers)

    override fun visitEnd() = context.addAction { original.visitEnd() }

}
