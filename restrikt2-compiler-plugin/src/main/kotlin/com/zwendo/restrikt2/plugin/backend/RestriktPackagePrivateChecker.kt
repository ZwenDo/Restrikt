package com.zwendo.restrikt2.plugin.backend

import com.zwendo.restrikt2.plugin.backend.RestriktPackagePrivateChecker.Lazy.LOWERING_OVERRIDDEN_SYMBOL_VISIBILITY
import com.zwendo.restrikt2.plugin.backend.RestriktPackagePrivateChecker.Lazy.PP_INTERFACE_MEMBER_DECLARATION
import com.zwendo.restrikt2.plugin.backend.RestriktPackagePrivateChecker.Lazy.VISIBILITY_ERROR
import com.zwendo.restrikt2.plugin.fir.getDirectOverriddenSymbols
import com.zwendo.restrikt2.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.classKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyAccessorChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirBasicExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirResolvedQualifierChecker
import org.jetbrains.kotlin.fir.analysis.checkers.hasAnnotationOrInsideAnnotatedClass
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertySetter
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirResolvable
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFileSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

/**
 * Class that checks and catch misuse of package-private symbols.
 */
internal class RestriktPackagePrivateChecker(session: FirSession) : FirAdditionalCheckersExtension(session) {

    //region usage
    private val resolvedQualifierChecker = object : FirResolvedQualifierChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(expression: FirResolvedQualifier) {
            val currentPackageName = context.containingFileSymbol ?: return
            val callee = expression.toResolvedCallableSymbol(context.session) ?: return
            tryReportUsage(expression, callee, currentPackageName, context, reporter)
        }
    }

    private val basicExpressionChecker = object : FirBasicExpressionChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(expression: FirStatement) {
            // If the symbol is not resolvable or if we don't know in which file we currently are, we cannot perform the
            // check.
            val currentPackageName = context.containingFileSymbol ?: return
            if (expression !is FirResolvable) return
            val callee = expression.calleeReference
            val calleeSymbol = callee.toResolvedCallableSymbol() ?: return
            tryReportUsage(expression, calleeSymbol, currentPackageName, context, reporter)
        }
    }

    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override val basicExpressionCheckers: Set<FirBasicExpressionChecker> = setOf(basicExpressionChecker)
        override val resolvedQualifierCheckers: Set<FirResolvedQualifierChecker> = setOf(resolvedQualifierChecker)
    }

    private fun tryReportUsage(
        element: FirElement,
        calleeSymbol: FirBasedSymbol<*>,
        currentFile: FirFileSymbol,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
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

    private val simpleFunctionChecker = object : FirFunctionChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirFunction) =
            tryReportDeclaration(declaration, declaration.symbol, context, reporter)
    }

    private val propertyAccessorChecker = object : FirPropertyAccessorChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirPropertyAccessor) =
            tryReportDeclaration(declaration, declaration.symbol, context, reporter)
    }

    private val propertyChecker = object : FirPropertyChecker(MppCheckerKind.Common) {
//        override fun check(declaration: FirProperty, context: CheckerContext, reporter: DiagnosticReporter) =

        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirProperty) =
            tryReportDeclaration(declaration, declaration.symbol, context, reporter)
    }

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val functionCheckers: Set<FirFunctionChecker> = setOf(simpleFunctionChecker)
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

    object Lazy : KtDiagnosticsContainer() {

        val VISIBILITY_ERROR by error2<PsiElement, FirBasedSymbol<*>, String>()
        val PP_INTERFACE_MEMBER_DECLARATION by error3<PsiElement, FirBasedSymbol<*>, PropertyKind, FirClassLikeSymbol<*>>()
        val LOWERING_OVERRIDDEN_SYMBOL_VISIBILITY by error4<PsiElement, FirBasedSymbol<*>, PropertyKind, String, FirClassLikeSymbol<*>>()

        private val rendererMap by KtDiagnosticFactoryToRendererMap("Restrikt2") { map ->
            map.put(
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

            map.put(
                PP_INTERFACE_MEMBER_DECLARATION,
                "Cannot set the visibility of ''{0}'' {1}to package-private: it is a member of the interface ''{2}'' and therefore must be public.",
                FirDiagnosticRenderers.SYMBOL,
                propertyKindRenderer,
                FirDiagnosticRenderers.DECLARATION_FQ_NAME,
            )

            map.put(
                LOWERING_OVERRIDDEN_SYMBOL_VISIBILITY,
                "Cannot set the visibility of ''{0}'' {1}to package-private: it would lower its original visibility ({2}) overridden from ''{3}''.",
                FirDiagnosticRenderers.SYMBOL,
                propertyKindRenderer,
                CommonRenderers.STRING,
                FirDiagnosticRenderers.DECLARATION_FQ_NAME
            )
        }

        override fun getRendererFactory(): BaseDiagnosticRendererFactory {
            return object : BaseDiagnosticRendererFactory() {
                override val MAP: KtDiagnosticFactoryToRendererMap
                    get() = rendererMap
            }
        }
    }

    enum class PropertyKind {
        GETTER,
        SETTER,
        NOT_A_PROPERTY
    }
}