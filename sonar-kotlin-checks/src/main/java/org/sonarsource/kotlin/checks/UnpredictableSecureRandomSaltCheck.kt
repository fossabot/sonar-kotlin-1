/*
 * SonarSource Kotlin
 * Copyright (C) 2018-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonarsource.kotlin.checks

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.sonar.check.Rule
import org.sonarsource.kotlin.api.checks.ArgumentMatcher
import org.sonarsource.kotlin.api.checks.BYTE_ARRAY_CONSTRUCTOR_SIZE_ARG_ONLY
import org.sonarsource.kotlin.api.checks.CallAbstractCheck
import org.sonarsource.kotlin.api.checks.ConstructorMatcher
import org.sonarsource.kotlin.api.checks.FunMatcher
import org.sonarsource.kotlin.api.checks.isBytesInitializedFromString
import org.sonarsource.kotlin.api.checks.isInitializedPredictably
import org.sonarsource.kotlin.api.checks.matches
import org.sonarsource.kotlin.api.checks.predictRuntimeValueExpression
import org.sonarsource.kotlin.api.frontend.secondaryOf
import org.sonarsource.kotlin.api.checks.simpleArgExpressionOrNull
import org.sonarsource.kotlin.api.frontend.KotlinFileContext

private const val MESSAGE = "Change this seed value to something unpredictable, or remove the seed."
private const val SECURE_RANDOM = "java.security.SecureRandom"

@org.sonarsource.kotlin.api.frontend.K1only
@Rule(key = "S4347")
class UnpredictableSecureRandomSaltCheck : CallAbstractCheck() {
    override val functionsToVisit = listOf(
        FunMatcher(qualifier = SECURE_RANDOM, name = "setSeed"),
        ConstructorMatcher(SECURE_RANDOM) {
            withArguments(ArgumentMatcher.ANY)
        }
    )

    override fun visitFunctionCall(callExpression: KtCallExpression, resolvedCall: ResolvedCall<*>, kotlinFileContext: KotlinFileContext) {
        val bindingContext = kotlinFileContext.bindingContext

        val saltArg = resolvedCall.simpleArgExpressionOrNull(0) ?: return
        val predictedSaltValue = saltArg.predictRuntimeValueExpression(bindingContext)

        if (predictedSaltValue is KtConstantExpression || predictedSaltValue.isBytesInitializedFromString(bindingContext)) {
            kotlinFileContext.reportIssue(saltArg, MESSAGE, listOf(kotlinFileContext.secondaryOf(predictedSaltValue)))
        } else if (
            predictedSaltValue.getResolvedCall(bindingContext) matches BYTE_ARRAY_CONSTRUCTOR_SIZE_ARG_ONLY &&
            saltArg.isInitializedPredictably(predictedSaltValue, bindingContext)
        ) {
            kotlinFileContext.reportIssue(saltArg, MESSAGE, listOf(kotlinFileContext.secondaryOf(predictedSaltValue)))
        }
    }
}
