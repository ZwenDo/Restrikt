package com.zwendo.restrikt2.plugin.backend

import com.zwendo.restrikt2.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.hasChild
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.backend.FirMetadataSource
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.extensions.transform
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrThrowImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.SimpleTypeNullability
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.allParametersCount
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.load.java.JavaDescriptorVisibilities
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

internal class RestriktAnnotationProcessor(
    private val pluginContext: IrPluginContext,
) : IrElementTransformer<ParentState> {

    private val defaultState = ParentState(ParentState.DEFAULT, ParentState.ParentKind.OTHER)

    private val anyConstructor: IrConstructorSymbol
    private val assertionErrorType: IrSimpleType
    private val assertionErrorConstructor: IrConstructorSymbol
    private var isInValueClass = false

    fun processModule(module: IrModuleFragment) {
        module.transform(this, defaultState)
    }

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
            val state = processDeclaration(declaration, data.defaultIfFileState())
            super.visitClass(declaration, state).also {
                if (state.hasHideFromKotlin) {
                    val metadata = declaration.metadata as? FirMetadataSource.Class ?: return@also
                    metadata.fir.setInternalIfNeeded()
                }
            }
        } finally {
            isInValueClass = oldIsInValueClass
        }
    }

    override fun visitFunction(declaration: IrFunction, data: ParentState): IrStatement {
        val state = processDeclaration(declaration, data)
        return super.visitFunction(declaration, state).also {
            if (state.hasHideFromKotlin) {
                val metadata = declaration.metadata as? FirMetadataSource.Function ?: return@also
                metadata.fir.setInternalIfNeeded()
            }
        }
    }

    override fun visitField(declaration: IrField, data: ParentState): IrStatement {
        val state = processDeclaration(declaration, data, ParentState.ParentKind.FIELD)
        return super.visitField(declaration, state).also {
            if (state.hasHideFromKotlin) {
                val metadata = declaration.metadata as? FirMetadataSource.Field ?: return@also
                metadata.fir.setInternalIfNeeded()
            }
        }
    }

    override fun visitProperty(declaration: IrProperty, data: ParentState): IrStatement {
        val state = processDeclaration(declaration, data, ParentState.ParentKind.PROPERTY)
        return super.visitProperty(declaration, state).also {
            if (state.hasHideFromKotlin) {
                val metadata = declaration.metadata as? FirMetadataSource.Property ?: return@also
                metadata.fir.setInternalIfNeeded()
            }
        }
    }

    private fun processDeclaration(
        declaration: IrDeclarationWithVisibility,
        parentState: ParentState,
        currentKind: ParentState.ParentKind = ParentState.ParentKind.OTHER,
    ): ParentState {
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

        return ParentState(newState, currentKind)
    }

    private fun FirMemberDeclaration.setInternalIfNeeded() {
        val visibility = status.visibility
        if (visibility == Visibilities.Private || visibility == Visibilities.PrivateToThis || visibility == Visibilities.Local) return
        replaceStatus(status.transform(visibility = Visibilities.Internal))
    }

    private fun IrAnnotationContainer.hasAnyAnnotation(annotations: Set<ClassId>): Boolean =
        annotations.any(this::hasAnnotation)

    private fun makeSynthetic(origin: IrDeclarationOrigin): IrDeclarationOrigin =
        if (isInValueClass) {
            origin
        } else {
            IrDeclarationOriginImpl("com.zwendo.restrikt2:syntheticMember", true)
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
                startOffset, endOffset, pluginContext.irBuiltIns.unitType, anyConstructor,
                0, 0
            )
            // add throws AssertionError()
            val aeCtorCall = IrConstructorCallImpl.fromSymbolOwner(assertionErrorType, assertionErrorConstructor)
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

