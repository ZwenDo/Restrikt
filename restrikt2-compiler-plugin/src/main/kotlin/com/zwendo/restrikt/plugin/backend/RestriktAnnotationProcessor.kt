package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.hasChild
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithVisibility
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.createBlockBody
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
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

    private val defaultState = ParentState(ParentState.DEFAULT)

    private val deprecatedConstructor: IrConstructorSymbol
    private val deprecatedType: IrSimpleType
    private val deprecationLevelType: IrSimpleTypeImpl
    private val hidden: IrEnumEntry
    private val anyConstructor: IrConstructorSymbol
    private val assertionErrorType: IrSimpleType
    private val assertionErrorConstructor: IrConstructorSymbol

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
        return super.visitFile(declaration, ParentState(state)).also {
            // we add the constructor in the 'also' to avoid visiting it
            // generate private constructor for file if it has at least one top-level decl
            if (declaration.hasChild { it.accept(TopLevelFinder, null) }) {
                generatePrivateConstructor(declaration)
            }
        }
    }

    override fun visitClass(declaration: IrClass, data: ParentState): IrStatement =
        super.visitClass(declaration, processDeclaration(declaration, data))

    override fun visitFunction(declaration: IrFunction, data: ParentState): IrStatement =
        super.visitFunction(declaration, processDeclaration(declaration, data))

    override fun visitField(declaration: IrField, data: ParentState): IrStatement =
        super.visitField(declaration, processDeclaration(declaration, data))

    override fun visitProperty(declaration: IrProperty, data: ParentState): IrStatement =
        super.visitProperty(declaration, processDeclaration(declaration, data))

    private fun processDeclaration(declaration: IrDeclarationWithVisibility, parentState: ParentState): ParentState {
        var isSynthetic = declaration.origin.isSynthetic
        val pState = parentState.value
        var newState = pState

        if (parentState.isInternal || PluginConfiguration.automaticInternalHiding && DescriptorVisibilities.INTERNAL == declaration.visibility) {
            if (!isSynthetic) {
                declaration.origin = makeSynthetic(declaration.origin)
                isSynthetic = true
            }
            // we can use the parent state to determine whether we entered this 'if' due to parent
            newState = newState or ParentState.IS_INTERNAL
        }

        if (PluginConfiguration.annotationProcessing) {
            if (parentState.hasPackagePrivate || declaration.hasAnyAnnotation(PluginConfiguration.packagePrivateAnnotations)) {
                declaration.visibility = JavaDescriptorVisibilities.PACKAGE_VISIBILITY
                newState = newState or ParentState.HAS_PACKAGE_PRIVATE
            }

            if (parentState.hasHideFromJava || declaration.hasAnyAnnotation(PluginConfiguration.hideFromJavaAnnotations)) {
                if (!isSynthetic) {
                    declaration.origin = makeSynthetic(declaration.origin)
                }
                newState = newState or ParentState.HAS_HIDE_FROM_JAVA
            }

            if (parentState.hasHideFromKotlin || declaration.hasAnyAnnotation(PluginConfiguration.hideFromKotlinAnnotations)) {
                declaration.annotations += deprecatedHiddenAnnotation()
                newState = newState or ParentState.HAS_HIDE_FROM_KOTLIN
            }
        }

        return if (newState == pState) parentState else ParentState(newState)
    }

    private fun IrAnnotationContainer.hasAnyAnnotation(annotations: Set<ClassId>): Boolean =
        annotations.any(this::hasAnnotation)

    private fun makeSynthetic(origin: IrDeclarationOrigin): IrDeclarationOrigin = object : IrDeclarationOrigin {
        override val name: String
            get() = origin.name
        override val isSynthetic: Boolean
            get() = true
    }

    private fun deprecatedHiddenAnnotation(): IrConstructorCall {
        val call = IrConstructorCallImpl.fromSymbolOwner(deprecatedType, deprecatedConstructor)
        call.putValueArgument(
            0,
            IrConstImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                pluginContext.irBuiltIns.stringType,
                IrConstKind.String,
                "",
            )
        )
        call.putValueArgument(
            2,
            IrGetEnumValueImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                deprecationLevelType,
                hidden.symbol,
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
        val deprecatedId = ClassId.fromString("kotlin/Deprecated")

        deprecatedConstructor = pluginContext.referenceConstructors(deprecatedId).first()
        deprecatedType = IrSimpleTypeImpl(
            pluginContext.referenceClass(deprecatedId)!!,
            SimpleTypeNullability.DEFINITELY_NOT_NULL,
            listOf(),
            emptyList()
        )

        val deprecationLevel = pluginContext.referenceClass(ClassId.fromString("kotlin/DeprecationLevel"))!!

        deprecationLevelType = IrSimpleTypeImpl(
            deprecationLevel,
            SimpleTypeNullability.DEFINITELY_NOT_NULL,
            emptyList(),
            emptyList()
        )
        hidden = @OptIn(UnsafeDuringIrConstructionAPI::class) deprecationLevel.owner
            .declarations
            .first { it is IrEnumEntry && it.name == Name.identifier("HIDDEN") } as IrEnumEntry

        anyConstructor =
            @OptIn(UnsafeDuringIrConstructionAPI::class) pluginContext.irBuiltIns.anyClass.constructors.first()

        val assertionError = ClassId.fromString("java/lang/AssertionError")
        assertionErrorType = IrSimpleTypeImpl(
            pluginContext.referenceClass(assertionError)!!,
            SimpleTypeNullability.DEFINITELY_NOT_NULL,
            listOf(),
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

