package com.zwendo.restrikt2.plugin.backend

import com.zwendo.restrikt2.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.backend.FirMetadataSource
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.extensions.transform
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithVisibility
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrFunctionWithLateBinding
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createBlockBody
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrThrowImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
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
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isPropertyAccessor
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.load.java.JavaDescriptorVisibilities
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

internal class RestriktIrElementTransformer(
    private val pluginContext: IrPluginContext,
) {

    private val defaultState = ElementState(ElementState.DEFAULT, ElementState.ParentKind.OTHER)

    private val anyConstructor: IrConstructorSymbol
    private val assertionErrorType: IrSimpleType
    private val assertionErrorConstructor: IrConstructorSymbol
    private var isInValueClass = false

    private val jvmNameId: ClassId
    private val jvmNameConstructor: IrConstructorSymbol
    private val jvmNameType: IrSimpleType

    private val transformer = object : IrElementTransformer<ElementState> {
        override fun visitFile(declaration: IrFile, data: ElementState): IrFile {
            var state = ElementState.DEFAULT
            if (declaration.hasAnyAnnotation(PluginConfiguration.hideFromJavaAnnotations)) {
                state = state or ElementState.HAS_HIDE_FROM_JAVA
            }
            if (declaration.hasAnyAnnotation(PluginConfiguration.packagePrivateAnnotations)) {
                state = state or ElementState.HAS_PACKAGE_PRIVATE
            }
            if (declaration.hasAnyAnnotation(PluginConfiguration.hideFromKotlinAnnotations)) {
                state = state or ElementState.HAS_HIDE_FROM_KOTLIN
            }
            return super.visitFile(declaration, ElementState(state, ElementState.ParentKind.FILE)).also {
                // we add the constructor in the 'also' to avoid visiting it
                // generate private constructor for file if it has at least one top-level decl
                @OptIn(UnsafeDuringIrConstructionAPI::class)
                if (declaration.declarations.any { it.accept(TopLevelFinder, null) }) {
                    generatePrivateConstructor(declaration)
                }
            }
        }

        override fun visitClass(declaration: IrClass, data: ElementState): IrStatement {
            val oldIsInValueClass = isInValueClass
            val r = processDeclaration(
                declaration,
                data.defaultIfFileState(),
                (declaration.metadata as? FirMetadataSource.Class)?.fir,
                ElementState.ParentKind.OTHER
            )
            val parent = if (declaration.isEnumClass) {
                ElementState.IGNORE_STATE
            } else {
                r
            }
            return try {
                isInValueClass = declaration.isValue
                super.visitClass(
                    declaration,
                    parent
                )
            } finally {
                isInValueClass = oldIsInValueClass
            }
        }

        override fun visitFunction(declaration: IrFunction, data: ElementState): IrStatement {
            processDeclaration(
                declaration,
                data,
                (declaration.metadata as? FirMetadataSource.Function)?.fir,
                ElementState.ParentKind.OTHER
            )
            return super.visitFunction(declaration, ElementState.IGNORE_STATE)
        }

        override fun visitField(declaration: IrField, data: ElementState): IrStatement =
            super.visitField(
                declaration,
                processDeclaration(
                    declaration,
                    data,
                    (declaration.metadata as? FirMetadataSource.Field)?.fir,
                    ElementState.ParentKind.FIELD
                )
            )

        override fun visitProperty(declaration: IrProperty, data: ElementState): IrStatement =
            super.visitProperty(
                declaration,
                processDeclaration(
                    declaration,
                    data,
                    (declaration.metadata as? FirMetadataSource.Property)?.fir,
                    ElementState.ParentKind.PROPERTY
                )
            )
    }

    fun processModule(module: IrModuleFragment) {
        module.transform(transformer, defaultState)
    }

    private fun <T> processDeclaration(
        declaration: T,
        parentState: ElementState,
        fir: FirMemberDeclaration?,
        currentKind: ElementState.ParentKind,
    ): ElementState where T : IrDeclarationWithVisibility, T : IrDeclarationWithName {
        if (parentState === ElementState.IGNORE_STATE) return parentState
        var newState = parentState.value

        val isInternal =
            PluginConfiguration.automaticInternalHiding && (parentState.isInternal || declaration.isInternal)

        val hasPackagePrivate = run {
            if (!PluginConfiguration.annotationProcessing) return@run false
            val parentIsPackagePrivate = (parentState.kind.isFileOrProperty && parentState.hasPackagePrivate)
            val isField = currentKind == ElementState.ParentKind.FIELD
            // we propagate package private from parent only if current is not a field
            (parentIsPackagePrivate && !isField) || declaration.hasAnyAnnotation(PluginConfiguration.packagePrivateAnnotations)
        }

        val hasHideFromJava =
            PluginConfiguration.annotationProcessing && (parentState.hasHideFromJava || declaration.hasAnyAnnotation(
                PluginConfiguration.hideFromJavaAnnotations
            ))

        val hasHideFromKotlin =
            PluginConfiguration.annotationProcessing && ((parentState.kind.isFileOrProperty && parentState.hasHideFromKotlin) ||
                        declaration.hasAnyAnnotation(PluginConfiguration.hideFromKotlinAnnotations))

        // NOTE: here the order is important, package-private processing should be done before hfj, hfk and internal
        // because it takes precedence over them (e.g. if a symbol is package private, we do not apply synthetic to
        // them). Currently, hfk and pp both set the kotlin visibility to internal, but if the way hfk is handled
        // changes, we should make sure that the package-private processing is done before hfk because package-private
        // should prevent hfk from being applied.
        if (hasPackagePrivate && !declaration.isHidden) {
            declaration.visibility = JavaDescriptorVisibilities.PACKAGE_VISIBILITY
            // we also set the internal visibility to the declaration to avoid any unexpected behavior with the compiler
            // or IDE plugin where the usage of the package-private symbol would be allowed because only the metadata
            // would be checked.
            declaration.setInternal(fir)

            // we need to propagate the package-private state to the getter and setter
            if (currentKind == ElementState.ParentKind.PROPERTY) {
                newState = newState or ElementState.HAS_PACKAGE_PRIVATE
            }
        }

        if (isInternal || hasHideFromJava) {
            if (!declaration.origin.isSynthetic && !declaration.isHidden && declaration !is IrClass) {
                declaration.origin = makeSynthetic(declaration.origin)
            }
            if (isInternal) newState = newState or ElementState.IS_INTERNAL
            if (hasHideFromJava) newState = newState or ElementState.HAS_HIDE_FROM_JAVA
        }

        if (hasHideFromKotlin) {
            if (!declaration.isHidden) {
                declaration.setInternal(fir)
            }

            if (currentKind == ElementState.ParentKind.PROPERTY) {
                newState = newState or ElementState.HAS_HIDE_FROM_KOTLIN
            }
        }

        return ElementState(newState, currentKind)
    }

    private fun <T> T.setInternal(fir: FirMemberDeclaration?) where T : IrDeclarationWithVisibility, T : IrDeclarationWithName {
        if (fir == null) return

        fir.replaceStatus(fir.status.transform(visibility = Visibilities.Internal))

        // if the declaration already has a JvmName annotation, we do not add another one
        if (hasAnnotation(jvmNameId)) return

        // We also must apply a JvmName annotation, because the way we change the visibility to internal is not
        // handled correctly by the compiler. Normally the name of members should change when they are internal
        // (i.e. foo$thePackage()), but the way we do it does not trigger this behavior.
        // It is a problem when members from the same kotlin project (meaning that they can access the internal
        // members) but compiled at a different time (typically test modules are compiled after the main module)
        // try to access the internal members. The compiler will detect that the members are internal and will
        // mangle the name following its rules, but the name of the members are not mangled as stated above.
        // This will lead to a NoSuchMethodError at runtime.
        val name = if (isPropertyAccessor) {
            @OptIn(UnsafeDuringIrConstructionAPI::class)
            (this as IrSimpleFunction).correspondingPropertySymbol?.owner?.let {
                val propName = it.name.asString()
                if (isGetter) JvmAbi.getterName(propName) else JvmAbi.setterName(propName)
            } ?: name.asString()
        } else {
            name.asString()
        }
        annotations += jvmNameAnnotation(name)
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

    private val IrDeclarationWithVisibility.isHidden: Boolean
        get() = visibility == DescriptorVisibilities.PRIVATE
                    || visibility == DescriptorVisibilities.PRIVATE_TO_THIS
                    || visibility == JavaDescriptorVisibilities.PACKAGE_VISIBILITY

    private val IrDeclarationWithVisibility.isInternal: Boolean
        get() = visibility == DescriptorVisibilities.INTERNAL

    private object TopLevelFinder : IrElementVisitor<Boolean, Nothing?> {

        override fun visitElement(element: IrElement, data: Nothing?): Boolean = false

        override fun visitFunction(declaration: IrFunction, data: Nothing?): Boolean =
            !(declaration is IrConstructor || declaration is IrFunctionWithLateBinding)

        override fun visitProperty(declaration: IrProperty, data: Nothing?): Boolean = true

    }

}
