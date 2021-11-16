/*
 * SonarSource Kotlin
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
package org.sonarsource.kotlin

import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.TextPointer
import org.sonar.api.batch.fs.TextRange
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.rule.RuleKey
import org.sonarsource.kotlin.api.InputFileContext
import org.sonarsource.kotlin.api.SecondaryLocation

class DummyInputFileContext : InputFileContext {
    override var filteredRules: Map<String, Set<TextRange>> = emptyMap()
    override val inputFile: InputFile = DummyInputFile()
    override val sensorContext: SensorContext by lazy { throw NotImplementedError() }
    override val isAndroid: Boolean = false

    val issuesReported = mutableListOf<ReportedIssue>()

    override fun reportIssue(
        ruleKey: RuleKey,
        textRange: TextRange?,
        message: String,
        secondaryLocations: List<SecondaryLocation>,
        gap: Double?
    ) {
        issuesReported.add(ReportedIssue(ruleKey, textRange, message, secondaryLocations, gap))
    }

    override fun reportAnalysisParseError(repositoryKey: String, inputFile: InputFile, location: TextPointer?) {
        throw NotImplementedError()
    }

    override fun reportAnalysisError(message: String?, location: TextPointer?) {
        throw NotImplementedError()
    }

}

data class ReportedIssue(
    val ruleKey: RuleKey,
    val textRange: TextRange?,
    val message: String,
    val secondaryLocations: List<SecondaryLocation>,
    val gap: Double?
)