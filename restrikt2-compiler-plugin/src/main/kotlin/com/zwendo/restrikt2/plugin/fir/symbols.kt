@file:OptIn(ScopeFunctionRequiresPrewarm::class)

package com.zwendo.restrikt2.plugin.fir

import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.unsubstitutedScope
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
import org.jetbrains.kotlin.fir.resolve.toClassSymbol
import org.jetbrains.kotlin.fir.scopes.ScopeFunctionRequiresPrewarm
import org.jetbrains.kotlin.fir.scopes.getDirectOverriddenMembers
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol


fun FirCallableDeclaration.getDirectOverriddenSymbols(context: CheckerContext): List<FirCallableSymbol<FirCallableDeclaration>> {
    if (!this.isOverride) return emptyList()
    val classSymbol = this.containingClassLookupTag()?.toClassSymbol(context.session) ?: return emptyList()
    val scope = classSymbol.unsubstitutedScope(context)
    when (this.symbol) {
        is FirNamedFunctionSymbol -> scope.processFunctionsByName(this.symbol.name) { }
        is FirPropertySymbol -> scope.processPropertiesByName(this.symbol.name) { }
    }

    return scope.getDirectOverriddenMembers(this.symbol)
}