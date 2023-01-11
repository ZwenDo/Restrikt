package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.backend.symbol.SymbolData
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.ModuleVisitor

/**
 * Restrikt visitor in charge of visiting modules.
 */
internal class RestriktModuleVisitor(
    private val context: SymbolData<ClassVisitor>,
    factory: () -> ModuleVisitor,
) : ModuleVisitor(ASM_VERSION) {

    private val original: ModuleVisitor by lazy { factory() }

    override fun visitMainClass(mainClass: String?) = queue { original.visitMainClass(mainClass) }

    override fun visitPackage(packaze: String?) = queue { original.visitPackage(packaze) }

    override fun visitRequire(module: String?, access: Int, version: String?) = queue {
        original.visitRequire(module, access, version)
    }

    override fun visitExport(packaze: String?, access: Int, vararg modules: String?) = queue {
        original.visitExport(packaze, access, *modules)
    }

    override fun visitOpen(packaze: String?, access: Int, vararg modules: String?) = queue {
        original.visitOpen(packaze, access, *modules)
    }

    override fun visitUse(service: String?) = queue { original.visitUse(service) }

    override fun visitProvide(service: String?, vararg providers: String?) = queue {
        original.visitProvide(service, *providers)
    }

    override fun visitEnd() = queue { original.visitEnd() }

    private inline fun queue(crossinline action: () -> Unit) = context.queueAction { action() }

}
