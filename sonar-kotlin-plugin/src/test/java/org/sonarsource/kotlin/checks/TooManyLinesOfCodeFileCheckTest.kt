/*
 * SonarSource SLang
 * Copyright (C) 2018-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.kotlin.checks

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.sonarsource.kotlin.verifier.KotlinVerifier

class TooManyLinesOfCodeFileCheckTest {
    @Disabled("missing filename in the message")
    @Test
    fun slang() {
        val check = org.sonarsource.slang.checks.TooManyLinesOfCodeFileCheck()
        check.max = 1
        KotlinVerifier.verify(
            "../../../../../kotlin-checks-test-sources/src/main/kotlin/checks/TooManyLinesOfCodeFileCheckSample.kt",
            check)
    }

    @Test
    fun test() {
        val check = TooManyLinesOfCodeFileCheck()
        check.max = 1
        KotlinVerifier(check) {
            fileName = "TooManyLinesOfCodeFileCheckSample.kt"
        }.verify()
        KotlinVerifier(check) {
            fileName = "TooManyLinesOfCodeFileCheckSampleCompliant.kt"
        }.verifyNoIssue()
    }
}