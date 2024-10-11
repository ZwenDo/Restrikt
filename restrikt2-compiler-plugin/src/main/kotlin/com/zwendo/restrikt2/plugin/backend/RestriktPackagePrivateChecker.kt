package com.zwendo.restrikt2.plugin.backend

import com.zwendo.restrikt2.plugin.backend.RestriktPackagePrivateChecker.Lazy.VISIBILITY_ERROR
import com.zwendo.restrikt2.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirBasicExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.hasAnnotationOrInsideAnnotatedClass
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrorsDefaultMessages
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.expressions.FirResolvable
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.references.symbol
import org.jetbrains.kotlin.fir.references.toResolvedNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

/**
 * Class that checks and catch misuse of package-private symbols.
 */
internal class RestriktPackagePrivateChecker(session: FirSession) : FirAdditionalCheckersExtension(session) {

    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override val basicExpressionCheckers: Set<FirBasicExpressionChecker> = setOf(Checker(session))
    }

    class Checker(private val session: FirSession) : FirBasicExpressionChecker(MppCheckerKind.Common) {

        override fun check(expression: FirStatement, context: CheckerContext, reporter: DiagnosticReporter) {
            val currentFile = context.containingFile
            // If the symbol is not resolvable or if we don't know in which file we currently are, we cannot perform the
            // check.
            if (expression !is FirResolvable || currentFile == null) return

            val callee = expression.calleeReference
            val calleeSymbol = callee.symbol ?: return

            // We check if the callee is in the same package as where we currently are. If they are the same, we can
            // skip the check as we should be able to access the symbol anyway.
            val calleePackage = calleeSymbol.packageFqName()
            if (calleePackage == currentFile.packageFqName) return

            // We check if the callee has a package-private annotation or if it is in a class that has it.
            val isAnnotated = PluginConfiguration.packagePrivateAnnotations.any {
                calleeSymbol.hasAnnotationOrInsideAnnotatedClass(it, session)
            }
            // If the symbol is not annotated, there is no error to report.
            if (!isAnnotated) return

            // If we reached this point, we detected an error and we report it.
            reporter.reportOn(expression.source, VISIBILITY_ERROR, calleeSymbol, calleeSymbol.toString(), context)
        }

    }

    private object Lazy {

        val VISIBILITY_ERROR by error2<PsiElement, FirBasedSymbol<*>, String>()

        init {
            FirErrorsDefaultMessages.MAP.put(
                VISIBILITY_ERROR,
                "Cannot access ''{0}'': it is package-private in package ''{1}''",
                FirDiagnosticRenderers.DECLARATION_NAME,
                CommonRenderers.STRING
            )
        }

    }

}
