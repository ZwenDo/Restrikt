package com.zwendo.restrikt.plugin.backend.visitor

import org.jetbrains.org.objectweb.asm.ModuleVisitor

internal class RestriktModuleVisitor(
    private val actionAccumulator: (() -> Unit) -> Unit,
    factory: () -> ModuleVisitor,
) : ModuleVisitor(ASM_VERSION) {

    private val original: ModuleVisitor by lazy { factory() }

    override fun visitMainClass(mainClass: String?): Unit = actionAccumulator { original.visitMainClass(mainClass) }

    override fun visitPackage(packaze: String?): Unit = actionAccumulator { original.visitPackage(packaze) }

    override fun visitRequire(module: String?, access: Int, version: String?): Unit = actionAccumulator {
        original.visitRequire(module, access, version)
    }

    override fun visitExport(packaze: String?, access: Int, vararg modules: String?): Unit = actionAccumulator {
        original.visitExport(packaze, access, *modules)
    }

    override fun visitOpen(packaze: String?, access: Int, vararg modules: String?): Unit = actionAccumulator {
        original.visitOpen(packaze, access, *modules)
    }

    override fun visitUse(service: String?): Unit = actionAccumulator { original.visitUse(service) }

    override fun visitProvide(service: String?, vararg providers: String?): Unit = actionAccumulator {
        original.visitProvide(service, *providers)
    }

    override fun visitEnd(): Unit = actionAccumulator { original.visitEnd() }

}
