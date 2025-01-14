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
package org.sonarsource.kotlin.api.frontend

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.sonarsource.kotlin.api.checks.InputFileContext
import org.sonarsource.kotlin.api.reporting.KotlinTextRanges.textRange
import org.sonarsource.kotlin.api.reporting.SecondaryLocation

data class KotlinFileContext(
    val inputFileContext: InputFileContext,
    val ktFile: KtFile,
    /**
     * @see [org.sonarsource.kotlin.api.visiting.withKaSession]
     */
    @Deprecated("use kotlin-analysis-api instead")
    val bindingContext: BindingContext,
    val diagnostics: List<Diagnostic>,
    val regexCache: RegexCache,
)

fun KotlinFileContext.secondaryOf(psiElement: PsiElement, msg: String? = null) = SecondaryLocation(textRange(psiElement), msg)
