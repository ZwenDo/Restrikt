package com.zwendo.restrikt2.plugin.backend

import com.zwendo.restrikt2.plugin.backend.RestriktPackagePrivateChecker.Lazy.LOWERING_OVERRIDDEN_SYMBOL_VISIBILITY
import com.zwendo.restrikt2.plugin.backend.RestriktPackagePrivateChecker.Lazy.PP_INTERFACE_MEMBER_DECLARATION
import com.zwendo.restrikt2.plugin.backend.RestriktPackagePrivateChecker.Lazy.VISIBILITY_ERROR
import com.zwendo.restrikt2.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.error3
import org.jetbrains.kotlin.diagnostics.error4
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.classKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyAccessorChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirSimpleFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirBasicExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirResolvedQualifierChecker
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.checkers.getDirectOverriddenSymbols
import org.jetbrains.kotlin.fir.analysis.checkers.hasAnnotationOrInsideAnnotatedClass
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrorsDefaultMessages
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertySetter
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirResolvable
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.references.symbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

/**
 * Class that checks and catch misuse of package-private symbols.
 */
internal class RestriktPackagePrivateChecker(session: FirSession) : FirAdditionalCheckersExtension(session) {

    //region usage
    private val resolvedQualifierChecker = object : FirResolvedQualifierChecker(MppCheckerKind.Common) {
        override fun check(expression: FirResolvedQualifier, context: CheckerContext, reporter: DiagnosticReporter) {
            val currentFile = context.containingFile ?: return
            val callee = expression.symbol ?: return
            tryReportUsage(expression, callee, currentFile, context, reporter)
        }

    }

    private val basicExpressionChecker = object : FirBasicExpressionChecker(MppCheckerKind.Common) {
        override fun check(expression: FirStatement, context: CheckerContext, reporter: DiagnosticReporter) {
            val currentFile = context.containingFile
            // If the symbol is not resolvable or if we don't know in which file we currently are, we cannot perform the
            // check.
            if (expression !is FirResolvable || currentFile == null) return

            val callee = expression.calleeReference
            val calleeSymbol = callee.symbol ?: return
            tryReportUsage(expression, calleeSymbol, currentFile, context, reporter)
        }
    }

    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override val basicExpressionCheckers: Set<FirBasicExpressionChecker> = setOf(basicExpressionChecker)
        override val resolvedQualifierCheckers: Set<FirResolvedQualifierChecker> = setOf(resolvedQualifierChecker)
    }

    private fun tryReportUsage(
        element: FirElement,
        calleeSymbol: FirBasedSymbol<*>,
        currentFile: FirFile,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        // We check if the callee is in the same package as where we currently are. If they are the same, we can
        // skip the check as we should be able to access the symbol anyway.
        val calleePackage = calleeSymbol.packageFqName()
        if (calleePackage == currentFile.packageFqName) return

        // We check if the callee has a package-private annotation or if it is in a class that has it.
        val isAnnotated = PluginConfiguration.packagePrivateAnnotations.any {
            calleeSymbol.hasAnnotationOrInsideAnnotatedClass(it, context.session)
        }
        // If the symbol is not annotated, there is no error to report.
        if (!isAnnotated) return

        // If we reached this point, we detected an error and we report it.
        reporter.reportOn(
            element.source,
            VISIBILITY_ERROR,
            calleeSymbol,
            calleePackage.toString(),
            context
        )
    }

    //endregion

    //region declaration

    private val simpleFunctionChecker = object : FirSimpleFunctionChecker(MppCheckerKind.Common) {
        override fun check(declaration: FirSimpleFunction, context: CheckerContext, reporter: DiagnosticReporter) =
            tryReportDeclaration(declaration, declaration.symbol, context, reporter)
    }

    private val propertyAccessorChecker = object : FirPropertyAccessorChecker(MppCheckerKind.Common) {
        override fun check(declaration: FirPropertyAccessor, context: CheckerContext, reporter: DiagnosticReporter) =
            tryReportDeclaration(declaration, declaration.symbol, context, reporter)
    }

    private val propertyChecker = object : FirPropertyChecker(MppCheckerKind.Common) {
        override fun check(declaration: FirProperty, context: CheckerContext, reporter: DiagnosticReporter) =
            tryReportDeclaration(declaration, declaration.symbol, context, reporter)
    }

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val simpleFunctionCheckers: Set<FirSimpleFunctionChecker> = setOf(simpleFunctionChecker)
        override val propertyAccessorCheckers: Set<FirPropertyAccessorChecker> = setOf(propertyAccessorChecker)
        override val propertyCheckers: Set<FirPropertyChecker> = setOf(propertyChecker)
    }

    private fun tryReportDeclaration(
        element: FirCallableDeclaration,
        symbol: FirBasedSymbol<*>,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val propertyKind = when (element) {
            is FirPropertyAccessor -> {
                if (element is FirDefaultPropertySetter) PropertyKind.SETTER else PropertyKind.GETTER
            }
            else -> PropertyKind.NOT_A_PROPERTY
        }
        // only used for reporting
        val actualSymbol = if (symbol is FirPropertyAccessorSymbol) {
            symbol.propertySymbol
        } else {
            symbol
        }

        // We check if the callee has a package-private annotation.
        val isAnnotated = PluginConfiguration.packagePrivateAnnotations.any {
            symbol.hasAnnotation(it, context.session)
        }

        // If the symbol is not annotated, there is nothing to report.
        if (!isAnnotated) return

        val declarationOwner = symbol.getContainingClassSymbol()

        if (declarationOwner != null && declarationOwner.classKind == ClassKind.INTERFACE) {
            // user is trying to set the method of an interface to package-private

            reporter.reportOn(
                element.source,
                PP_INTERFACE_MEMBER_DECLARATION,
                actualSymbol,
                propertyKind,
                declarationOwner,
                context
            )
            return
        }

        // If the declaration is not an override, we can skip the check.
        if (!element.isOverride) return

        @OptIn(SymbolInternals::class)
        val overrideCheckSource = if (element is FirPropertyAccessor) {
            element.propertySymbol.fir
        } else {
            element.symbol.fir
        }
        val overriddenSymbols = overrideCheckSource.getDirectOverriddenSymbols(context)

        val firstNonPP = overriddenSymbols.firstOrNull { overriddenSymbol ->
            PluginConfiguration.packagePrivateAnnotations.all {
                !overriddenSymbol.hasAnnotation(it, context.session)
            }
        }

        if (firstNonPP == null) return

        val actualVisibility = when (propertyKind) {
            PropertyKind.GETTER -> (firstNonPP as FirPropertySymbol).getterSymbol!!.visibility
            PropertyKind.SETTER -> (firstNonPP as FirPropertySymbol).setterSymbol!!.visibility
            PropertyKind.NOT_A_PROPERTY -> firstNonPP.visibility
        }

        reporter.reportOn(
            element.source,
            LOWERING_OVERRIDDEN_SYMBOL_VISIBILITY,
            actualSymbol,
            propertyKind,
            actualVisibility.toString(),
            firstNonPP.getContainingClassSymbol()!!,
            context
        )
    }

    //endregion

    private object Lazy {

        val VISIBILITY_ERROR by error2<PsiElement, FirBasedSymbol<*>, String>()

        val PP_INTERFACE_MEMBER_DECLARATION by error3<PsiElement, FirBasedSymbol<*>, PropertyKind, FirClassLikeSymbol<*>>()

        val LOWERING_OVERRIDDEN_SYMBOL_VISIBILITY by error4<PsiElement, FirBasedSymbol<*>, PropertyKind, String, FirClassLikeSymbol<*>>()

        init {
            FirErrorsDefaultMessages.MAP.put(
                VISIBILITY_ERROR,
                "Cannot access ''{0}'': it is package-private in package ''{1}''.",
                FirDiagnosticRenderers.SYMBOL,
                CommonRenderers.STRING
            )
            val propertyKindRenderer = Renderer<PropertyKind> {
                when (it) {
                    PropertyKind.GETTER -> "(getter) "
                    PropertyKind.SETTER -> "(setter) "
                    PropertyKind.NOT_A_PROPERTY -> ""
                }
            }
            FirErrorsDefaultMessages.MAP.put(
                PP_INTERFACE_MEMBER_DECLARATION,
                "Cannot set the visibility of ''{0}'' {1}to package-private: it is a member of the interface ''{2}'' and therefore must be public.",
                FirDiagnosticRenderers.SYMBOL,
                propertyKindRenderer,
                FirDiagnosticRenderers.DECLARATION_FQ_NAME,
            )

            FirErrorsDefaultMessages.MAP.put(
                LOWERING_OVERRIDDEN_SYMBOL_VISIBILITY,
                "Cannot set the visibility of ''{0}'' {1}to package-private: it would lower its original visibility ({2}) overridden from ''{3}''.",
                FirDiagnosticRenderers.SYMBOL,
                propertyKindRenderer,
                CommonRenderers.STRING,
                FirDiagnosticRenderers.DECLARATION_FQ_NAME
            )
        }

    }

    private enum class PropertyKind {
        GETTER,
        SETTER,
        NOT_A_PROPERTY
    }

}
