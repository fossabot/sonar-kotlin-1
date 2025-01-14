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

import com.intellij.openapi.util.Disposer
import org.assertj.core.api.Assertions
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.psi.KtFile
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.sonarsource.kotlin.tools.AstPrinter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.readText

fun main() {
    val disposable = Disposer.newDisposable()
    try {
        fix_all_cls_files_test_automatically(Environment(disposable, emptyList(), LanguageVersion.LATEST_STABLE))
    } finally {
        Disposer.dispose(disposable)
    }
}

internal class KotlinASTTest {
    private val disposable = Disposer.newDisposable()

    @AfterEach
    fun dispose() {
        Disposer.dispose(disposable)
    }

    private val environment = Environment(disposable, emptyList(), LanguageVersion.LATEST_STABLE)

    @Test
    fun all_kotlin_files() {
        for (kotlinPath in kotlinSources()) {
            val astPath = Path.of(kotlinPath.toString().replaceFirst("\\.kts?$".toRegex(), ".txt"))
            val ktFile = parse(environment, kotlinPath)
            val actualAst = AstPrinter.txtPrint(ktFile, ktFile.viewProvider.document)
            val expectingAst = if (astPath.toFile().exists()) astPath.readText() else ""
            Assertions.assertThat(actualAst.trim { it <= ' ' })
                .describedAs("In the file: $astPath (run KotlinASTTest.main manually)")
                .isEqualToIgnoringWhitespace(expectingAst.trim { it <= ' ' })
        }
    }
}

private fun fix_all_cls_files_test_automatically(environment: Environment) {
    for (kotlinPath in kotlinSources()) {
        val astPath = Path.of(kotlinPath.toString().replaceFirst("\\.kts?$".toRegex(), ".txt"))
        val actualAst = AstPrinter.txtPrint(parse(environment, kotlinPath))
        Files.write(astPath, actualAst.toByteArray(StandardCharsets.UTF_8))
    }
}

private fun kotlinSources(): List<Path> {
    Files.walk(Path.of("src", "test", "resources", "ast")).use { pathStream ->
        return pathStream
            .filter { path: Path ->
                !path.toFile().isDirectory && path.fileName.toString()
                    .endsWith(".kt") || path.fileName.toString().endsWith(".kts")
            }
            .sorted()
            .collect(Collectors.toList())
    }
}

private fun parse(environment: Environment, path: Path): KtFile {
    val code = String(Files.readAllBytes(path), StandardCharsets.UTF_8)
    return try {
        environment.ktPsiFactory.createFile(code.replace("""\r\n?""".toRegex(), "\n"))
    } catch (e: ParseException) {
        throw ParseException(e.message + " in file " + path, e.position, e)
    } catch (e: RuntimeException) {
        throw RuntimeException(e.javaClass.simpleName + ": " + e.message + " in file " + path, e)
    }
}
