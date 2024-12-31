package com.zwendo.restrikt2.plugin.backend

import com.zwendo.restrikt2.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.hasChild
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.backend.FirMetadataSource
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.extensions.transform
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.SimpleTypeNullability
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.load.java.JavaDescriptorVisibilities
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

internal class RestriktIrElementTransformer(
    private val pluginContext: IrPluginContext,
) {

    private val defaultState = ParentState(ParentState.DEFAULT, ParentState.ParentKind.OTHER)

    private val anyConstructor: IrConstructorSymbol
    private val assertionErrorType: IrSimpleType
    private val assertionErrorConstructor: IrConstructorSymbol
    private var isInValueClass = false

    private val jvmNameId: ClassId
    private val jvmNameConstructor: IrConstructorSymbol
    private val jvmNameType: IrSimpleType

    private val transformer = object : IrElementTransformer<ParentState> {
        override fun visitFile(declaration: IrFile, data: ParentState): IrFile {
            var state = ParentState.DEFAULT
            if (declaration.hasAnyAnnotation(PluginConfiguration.hideFromJavaAnnotations)) {
                state = state or ParentState.HAS_HIDE_FROM_JAVA
            }
            if (declaration.hasAnyAnnotation(PluginConfiguration.packagePrivateAnnotations)) {
                state = state or ParentState.HAS_PACKAGE_PRIVATE
            }
            if (declaration.hasAnyAnnotation(PluginConfiguration.hideFromKotlinAnnotations)) {
                state = state or ParentState.HAS_HIDE_FROM_KOTLIN
            }
            return super.visitFile(declaration, ParentState(state, ParentState.ParentKind.FILE)).also {
                // we add the constructor in the 'also' to avoid visiting it
                // generate private constructor for file if it has at least one top-level decl
                if (declaration.hasChild { it.accept(TopLevelFinder, null) }) {
                    generatePrivateConstructor(declaration)
                }
            }
        }

        override fun visitClass(declaration: IrClass, data: ParentState): IrStatement {
            val oldIsInValueClass = isInValueClass
            return try {
                isInValueClass = declaration.isValue
                super.visitClass(
                    declaration,
                    processDeclaration(
                        declaration,
                        data.defaultIfFileState(),
                        (declaration.metadata as? FirMetadataSource.Class)?.fir,
                        ParentState.ParentKind.OTHER
                    )
                )
            } finally {
                isInValueClass = oldIsInValueClass
            }
        }

        override fun visitFunction(declaration: IrFunction, data: ParentState): IrStatement =
            super.visitFunction(
                declaration,
                processDeclaration(
                    declaration,
                    data,
                    (declaration.metadata as? FirMetadataSource.Function)?.fir,
                    ParentState.ParentKind.OTHER
                )
            )

        override fun visitField(declaration: IrField, data: ParentState): IrStatement =
            super.visitField(
                declaration,
                processDeclaration(
                    declaration,
                    data,
                    (declaration.metadata as? FirMetadataSource.Field)?.fir,
                    ParentState.ParentKind.FIELD
                )
            )

        override fun visitProperty(declaration: IrProperty, data: ParentState): IrStatement =
            super.visitProperty(
                declaration,
                processDeclaration(
                    declaration,
                    data,
                    (declaration.metadata as? FirMetadataSource.Property)?.fir,
                    ParentState.ParentKind.PROPERTY
                )
            )
    }

    fun processModule(module: IrModuleFragment) {
        module.transform(transformer, defaultState)
    }

    private fun <T> processDeclaration(
        declaration: T,
        parentState: ParentState,
        fir: FirMemberDeclaration?,
        currentKind: ParentState.ParentKind,
    ): ParentState where T : IrDeclarationWithVisibility, T : IrDeclarationWithName {
        var isSynthetic = declaration.origin.isSynthetic
        val pState = parentState.value
        var newState = pState

        if (PluginConfiguration.automaticInternalHiding && (parentState.isInternal || DescriptorVisibilities.INTERNAL == declaration.visibility)) {
            if (!isSynthetic) {
                declaration.origin = makeSynthetic(declaration.origin)
                isSynthetic = true
            }
            newState = newState or ParentState.IS_INTERNAL
        }

        if (PluginConfiguration.annotationProcessing) {
            val parentIsPackagePrivate = (parentState.kind.isFileOrProperty && parentState.hasPackagePrivate)
            val isField = currentKind == ParentState.ParentKind.FIELD
            if ((parentIsPackagePrivate && !isField) || declaration.hasAnyAnnotation(PluginConfiguration.packagePrivateAnnotations)) {
                declaration.visibility = JavaDescriptorVisibilities.PACKAGE_VISIBILITY
                if (currentKind == ParentState.ParentKind.PROPERTY) { // we need to propagate the package-private state to the getter and setter
                    newState = newState or ParentState.HAS_PACKAGE_PRIVATE
                }
            }

            if (parentState.hasHideFromJava || declaration.hasAnyAnnotation(PluginConfiguration.hideFromJavaAnnotations)) {
                if (!isSynthetic) {
                    declaration.origin = makeSynthetic(declaration.origin)
                }
                newState = newState or ParentState.HAS_HIDE_FROM_JAVA
            }

            if (
                PluginConfiguration.annotationProcessing &&
                (
                    (parentState.kind.isFileOrProperty && parentState.hasHideFromKotlin) ||
                        declaration.hasAnyAnnotation(PluginConfiguration.hideFromKotlinAnnotations)
                    )
            ) {
                newState = newState or ParentState.HAS_HIDE_FROM_KOTLIN
            }
        }

        return ParentState(newState, currentKind).also { it ->
            if (!it.hasHideFromKotlin || fir == null) return@also

            val visibility = fir.status.visibility

            // we apply the hide from kotlin internal visibility change only if it does not increase the visibility
            if (visibility == Visibilities.Private || visibility == Visibilities.PrivateToThis || visibility == Visibilities.Local) return@also

            fir.replaceStatus(fir.status.transform(visibility = Visibilities.Internal))

            // if the declaration already has a JvmName annotation, we do not add another one
            if (declaration.hasAnnotation(jvmNameId)) return@also

            // We also must apply a JvmName annotation, because the way we change the visibility to internal is not
            // handled correctly by the compiler. Normally the name of members should change when they are internal
            // (i.e. foo$thePackage()), but the way we do it does not trigger this behavior.
            // It is a problem when members from the same kotlin project (meaning that they can access the internal
            // members) but compiled at a different time (typically test modules are compiled after the main module)
            // try to access the internal members. The compiler will detect that the members are internal and will
            // mangle the name following its rules, but the name of the members are not mangled as stated above.
            // This will lead to a NoSuchMethodError at runtime.
            val name = if (declaration.isPropertyAccessor) {
                @OptIn(UnsafeDuringIrConstructionAPI::class)
                (declaration as IrSimpleFunction).correspondingPropertySymbol?.owner?.let {
                    val propName = it.name.asString()
                    if (declaration.isGetter) JvmAbi.getterName(propName) else JvmAbi.setterName(propName)
                } ?: declaration.name.asString()
            } else {
                declaration.name.asString()
            }
            declaration.annotations += jvmNameAnnotation(name)
        }
    }

    private fun IrAnnotationContainer.hasAnyAnnotation(annotations: Set<ClassId>): Boolean =
        annotations.any(this::hasAnnotation)

    private fun makeSynthetic(origin: IrDeclarationOrigin): IrDeclarationOrigin =
        if (isInValueClass) {
            origin
        } else {
            IrDeclarationOriginImpl("com.zwendo.restrikt2:syntheticMember", true)
        }

    private fun jvmNameAnnotation(name: String): IrConstructorCall {
        val call = IrConstructorCallImpl.fromSymbolOwner(jvmNameType, jvmNameConstructor)
        call.putValueArgument(
            0,
            IrConstImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                pluginContext.irBuiltIns.stringType,
                IrConstKind.String,
                name,
            )
        )
        return call
    }

    private fun generatePrivateConstructor(declaration: IrFile) {
        val constructor = pluginContext.irFactory.createConstructor(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            IrDeclarationOrigin.DEFINED,
            Name.special("<init>"),
            DescriptorVisibilities.PRIVATE,
            isInline = false,
            isExpect = false,
            returnType = pluginContext.irBuiltIns.unitType,
            symbol = IrConstructorSymbolImpl(),
            isPrimary = true,
        )
        constructor.body = pluginContext.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
            // super call
            statements += IrDelegatingConstructorCallImpl(
                startOffset,
                endOffset,
                pluginContext.irBuiltIns.unitType,
                anyConstructor,
                0,
            )
            // add throws AssertionError()
            val aeCtorCall = IrConstructorCallImpl.fromSymbolOwner(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                assertionErrorType,
                assertionErrorConstructor
            )
            statements += IrThrowImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                assertionErrorType,
                aeCtorCall
            )
        }
        declaration.addChild(constructor)
    }

    init {
        jvmNameId = ClassId.fromString("kotlin/jvm/JvmName")
        jvmNameConstructor = pluginContext.referenceConstructors(jvmNameId).first()
        jvmNameType = IrSimpleTypeImpl(
            pluginContext.referenceClass(jvmNameId)!!,
            SimpleTypeNullability.DEFINITELY_NOT_NULL,
            emptyList(),
            emptyList()
        )

        @OptIn(UnsafeDuringIrConstructionAPI::class)
        anyConstructor = pluginContext.irBuiltIns.anyClass.constructors.first()

        val assertionError = ClassId.fromString("java/lang/AssertionError")
        assertionErrorType = IrSimpleTypeImpl(
            pluginContext.referenceClass(assertionError)!!,
            SimpleTypeNullability.DEFINITELY_NOT_NULL,
            emptyList(),
            emptyList()
        )
        assertionErrorConstructor = pluginContext.referenceConstructors(assertionError).find {
            @OptIn(UnsafeDuringIrConstructionAPI::class)
            it.owner.allParametersCount == 1
        }!!
    }

    private object TopLevelFinder : IrElementVisitor<Boolean, Nothing?> {

        override fun visitElement(element: IrElement, data: Nothing?): Boolean = false

        override fun visitFunction(declaration: IrFunction, data: Nothing?): Boolean = true

        override fun visitProperty(declaration: IrProperty, data: Nothing?): Boolean = true

    }

}
