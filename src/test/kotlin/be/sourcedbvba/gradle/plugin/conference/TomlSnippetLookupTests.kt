package be.sourcedbvba.gradle.plugin.conference

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.nhaarman.mockito_kotlin.*
import io.damo.aspen.Test
import org.assertj.core.api.Assertions.*
import java.io.ByteArrayInputStream
import java.io.IOException

class TomlSnippetLookupTests : Test({
    val mockProject = mock<Project>()

    before {
        reset(mockProject)
    }

    describe("load snippets") {
        val snippets = """
                test = '''
                this is a test
                '''
            """

        test("snippets are present") {
            val snippetFile = mock<VirtualFile>()
            val baseDir = mock<VirtualFile>()
            whenever(mockProject.baseDir).thenReturn(baseDir)
            whenever(baseDir.findChild(".snippets.toml")).thenReturn(snippetFile);
            whenever(snippetFile.inputStream).thenReturn(ByteArrayInputStream(snippets.toByteArray()))
            val snippetLookup = TomlSnippetLookup()

            val loadedSnippets = snippetLookup.getSnippets(mockProject)

            assertThat(loadedSnippets).isNotEmpty
            assertThat(loadedSnippets).hasOnlyOneElementSatisfying {
                it.key == "test" && it.code == "this is a test"
            }
        }

        test("snippets are not present") {
            val baseDir = mock<VirtualFile>()
            whenever(mockProject.baseDir).thenReturn(baseDir)
            whenever(baseDir.findChild(".snippets.toml")).thenReturn(null);
            val snippetLookup = TomlSnippetLookup()

            val loadedSnippets = snippetLookup.getSnippets(mockProject)

            assertThat(loadedSnippets).isEmpty()
        }

        test("snippets read throws an exception") {
            val snippetFile = mock<VirtualFile>()
            val baseDir = mock<VirtualFile>()
            whenever(mockProject.baseDir).thenReturn(baseDir)
            whenever(baseDir.findChild(".snippets.toml")).thenReturn(snippetFile);
            whenever(snippetFile.inputStream).thenThrow(IOException())
            val snippetLookup = TomlSnippetLookup()

            val loadedSnippets = snippetLookup.getSnippets(mockProject)

            assertThat(loadedSnippets).isEmpty()
        }
    }
})