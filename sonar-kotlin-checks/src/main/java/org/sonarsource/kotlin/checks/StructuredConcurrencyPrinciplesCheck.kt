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

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContext.TYPE
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.resolve.calls.model.ExpressionValueArgument
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.sonar.check.Rule
import org.sonarsource.kotlin.api.checks.CallAbstractCheck
import org.sonarsource.kotlin.api.checks.FUNS_ACCEPTING_DISPATCHERS
import org.sonarsource.kotlin.api.checks.FunMatcher
import org.sonarsource.kotlin.api.checks.KOTLINX_COROUTINES_PACKAGE
import org.sonarsource.kotlin.api.checks.determineTypeAsString
import org.sonarsource.kotlin.api.checks.predictReceiverExpression
import org.sonarsource.kotlin.api.frontend.KotlinFileContext

private val JOB_CONSTRUCTOR = FunMatcher(qualifier = KOTLINX_COROUTINES_PACKAGE, name = "Job")
private val SUPERVISOR_JOB_CONSTRUCTOR = FunMatcher(qualifier = KOTLINX_COROUTINES_PACKAGE, name = "SupervisorJob")
private const val MESSAGE_ENDING = " here leads to the breaking of structured concurrency principles."
private const val DELICATE_API_CLASS_TYPE = "kotlin.reflect.KClass<kotlinx.coroutines.DelicateCoroutinesApi>"

@org.sonarsource.kotlin.api.frontend.K1only
@Rule(key = "S6306")
class StructuredConcurrencyPrinciplesCheck : CallAbstractCheck() {

    override val functionsToVisit = FUNS_ACCEPTING_DISPATCHERS

    override fun visitFunctionCall(callExpression: KtCallExpression, resolvedCall: ResolvedCall<*>, kotlinFileContext: KotlinFileContext) {
        val bindingContext = kotlinFileContext.bindingContext
        val receiver = callExpression.predictReceiverExpression(bindingContext) as? KtNameReferenceExpression
        if (receiver?.getReferencedName() == "GlobalScope" && !callExpression.checkOptInDelicateApi(bindingContext)) {
            kotlinFileContext.reportIssue(receiver, """Using "GlobalScope"$MESSAGE_ENDING""")
        } else {
            resolvedCall.valueArgumentsByIndex?.let { args ->
                if (args.isEmpty()) return
                val argExprCall = (args[0] as? ExpressionValueArgument)?.valueArgument?.getArgumentExpression() as? KtCallExpression ?: return
                if (JOB_CONSTRUCTOR.matches(argExprCall, bindingContext) || SUPERVISOR_JOB_CONSTRUCTOR.matches(argExprCall, bindingContext)
                ) {
                    kotlinFileContext.reportIssue(argExprCall, """Using "${argExprCall.text}"$MESSAGE_ENDING""")
                }
            }
        }
    }
}

private fun KtExpression.checkOptInDelicateApi(bindingContext: BindingContext): Boolean {
    var parent: PsiElement? = this
    while (parent != null) {
        val annotations = (parent as? KtAnnotated)?.annotationEntries
        if (annotations.isAnnotatedWithOptInDelicateApi(bindingContext)) return true
        parent = parent.parent
    }
    return false
}

private fun MutableList<KtAnnotationEntry>?.isAnnotatedWithOptInDelicateApi(bindingContext: BindingContext) =
    this?.let {
        it.any { annotation ->
            bindingContext[TYPE, annotation.typeReference]
            val typeFqn = annotation.typeReference?.determineTypeAsString(bindingContext)
            typeFqn == "kotlinx.coroutines.DelicateCoroutinesApi" ||
                (typeFqn == "kotlin.OptIn"
                    && annotation.valueArguments.any { valueArgument ->
                    valueArgument.getArgumentExpression()?.getType(bindingContext)
                        ?.getKotlinTypeFqName(true) == DELICATE_API_CLASS_TYPE
                })
        }
    } ?: false
