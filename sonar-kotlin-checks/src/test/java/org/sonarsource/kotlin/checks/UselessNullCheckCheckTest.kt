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

import io.mockk.every
import io.mockk.mockk
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.junit.jupiter.api.Test
import org.sonarsource.kotlin.testapi.KotlinVerifier

internal class UselessNullCheckCheckTest : CheckTestWithNoSemantics(UselessNullCheckCheck()) {
    @Test
    fun `ensure issues are not raised when MISSING_BUILT_IN_DECLARATION diagnostics is found on node`() {
        val diagnostic = mockk<Diagnostic> {
            every { factory } returns Errors.MISSING_BUILT_IN_DECLARATION
            every { psiElement } returns mockk<PsiElement> {
                every { startOffset } returns 0
            }
        }

        KotlinVerifier(check) {
            this.fileName = sampleFileNoSemantics ?: "UselessNullCheckCheckSampleWithErrorDiagnostics.kt"
            this.customDiagnostics = listOf(diagnostic)
        }.verifyNoIssue()
    }
}
