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
import org.jetbrains.kotlin.resolve.bindingContextUtil.isUsedAsStatement
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.sonar.check.Rule
import org.sonarsource.kotlin.api.checks.CallAbstractCheck
import org.sonarsource.kotlin.api.checks.FunMatcher
import org.sonarsource.kotlin.api.checks.simpleName
import org.sonarsource.kotlin.api.frontend.KotlinFileContext

@org.sonarsource.kotlin.api.frontend.K1only
@Rule(key = "S899")
class IgnoredOperationStatusCheck : CallAbstractCheck() {

    override val functionsToVisit = listOf(
        FunMatcher(qualifier = "java.io.File") {
            withNames("delete", "mkdir", "renameTo", "setReadOnly", "setLastModified", "setWritable", "setReadable", "setExecutable")
        },
        FunMatcher(qualifier = "java.util.Iterator", name = "hasNext") { withNoArguments() },
        FunMatcher(qualifier = "kotlin.collections.Iterator", name = "hasNext") { withNoArguments() },
        FunMatcher(qualifier = "kotlin.collections.MutableIterator", name = "hasNext") { withNoArguments() },
        FunMatcher(qualifier = "java.util.Enumeration", name = "hasMoreElements") { withNoArguments() },
        FunMatcher(qualifier = "java.util.concurrent.locks.Lock", name = "tryLock"),
        FunMatcher(qualifier = "java.util.concurrent.locks.Condition", name = "await") {
            withArguments("kotlin.Long", "java.util.concurrent.TimeUnit")
        },
        FunMatcher(qualifier = "java.util.concurrent.locks.Condition") {
            withNames("awaitNanos", "awaitUntil")
            /* different argument types */
        },
        FunMatcher(qualifier = "java.util.concurrent.CountDownLatch", name = "await") {
            withArguments("kotlin.Long", "java.util.concurrent.TimeUnit")
        },
        FunMatcher(qualifier = "java.util.concurrent.Semaphore", name = "tryAcquire"),
        FunMatcher(qualifier = "java.util.concurrent.BlockingQueue") {
            withNames("offer", "remove")
        },
    )

    override fun visitFunctionCall(callExpression: KtCallExpression, resolvedCall: ResolvedCall<*>, kotlinFileContext: KotlinFileContext) {
        if (callExpression.isUsedAsStatement(kotlinFileContext.bindingContext)) {
            resolvedCall.resultingDescriptor?.let { resultingDescriptor ->
                val returnType = resultingDescriptor.returnType?.simpleName() ?: "this method";
                val message = """Do something with the "$returnType" value returned by "${resultingDescriptor.name}"."""
                kotlinFileContext.reportIssue(callExpression.calleeExpression!!, message)
            }
        }
    }

}
