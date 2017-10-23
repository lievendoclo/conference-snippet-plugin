package be.sourcedbvba.gradle.plugin.conference

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.moandjiezana.toml.Toml

class SnippetLookup {
    companion object {
        private val toml = Toml()
        private var currentSnippets: Map<String, Any> = mapOf()
        private var modificationStamp: Long = -1
    }

    fun getSnippets(project: Project): List<Snippet> {
        val snippetFile = getSnippetFile(project)
        if(snippetFile != null) {
            if (fileHasChanged(snippetFile)) {
                updateLastModified(snippetFile)
                loadSnippets(snippetFile)
            }
        }
        return toSnippetList(currentSnippets);
    }

    private fun updateLastModified(snippetFile: VirtualFile) {
        modificationStamp = snippetFile.modificationStamp
    }

    private fun getSnippetFile(project: Project) = project.baseDir.findChild(".snippets.toml")

    private fun fileHasChanged(snippetFile: VirtualFile) = modificationStamp < snippetFile.modificationStamp

    private fun toSnippetList(snippets: Map<String, Any>) = snippets.map { Snippet(it.key, it.value.toString()) }.toList()

    private fun loadSnippets(snippetFile: VirtualFile) {
        try {
            currentSnippets = toml.read(snippetFile.inputStream).toMap()
        } catch (e: Exception) {
            currentSnippets =  mapOf()
        }
    }
}