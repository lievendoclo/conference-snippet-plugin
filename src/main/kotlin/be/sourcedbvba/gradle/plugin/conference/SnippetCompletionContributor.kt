package be.sourcedbvba.gradle.plugin.conference

import com.intellij.codeInsight.actions.FileInEditorProcessor
import com.intellij.codeInsight.actions.LastRunReformatCodeOptionsProvider
import com.intellij.codeInsight.actions.TextRangeType
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile

class SnippetCompletionContributor : CompletionContributor() {

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val snippets = SnippetLookup().getSnippets(parameters.editor.project!!)
        snippets.forEach { s -> addCompletionForSnippet(s, result, parameters) }
    }

    private fun addCompletionForSnippet(snippet: Snippet, result: CompletionResultSet, parameters: CompletionParameters) {
        result.addElement(createLookupElementFromSnippet(snippet, parameters))
    }

    private fun createLookupElementFromSnippet(snippet: Snippet, parameters: CompletionParameters): LookupElement {
        return LookupElementBuilder
                .create(snippet.code)
                .withPresentableText(snippet.key)
                .withTypeText("Conference snippet")
                .withLookupString(snippet.key)
                .withInsertHandler({ insertionContext, lookupElement ->
                    removeLastInsertedCharacter(insertionContext)
                    doReformat(parameters)
                })
    }

    private fun removeLastInsertedCharacter(insertionContext: InsertionContext) {
        val currentCaretPosition = getCurrentCaretPosition(insertionContext)
        removeCharacterBeforeOffset(insertionContext, currentCaretPosition)
    }

    private fun removeCharacterBeforeOffset(insertionContext: InsertionContext, offset: Int) {
        insertionContext.document.deleteString(offset - 1, offset)
    }

    private fun getCurrentCaretPosition(insertionContext: InsertionContext) =
            insertionContext.editor.caretModel.offset

    private fun doReformat(completionParameters: CompletionParameters) {
        val editor = completionParameters.editor
        val file = getCurrentEditorFile(editor)
        val fileInEditorProcessor = createFileInEditorProcessor(file, editor)
        fileInEditorProcessor.processCode()
    }

    private fun createFileInEditorProcessor(file: PsiFile, editor: Editor): FileInEditorProcessor {
        val provider = LastRunReformatCodeOptionsProvider(PropertiesComponent.getInstance())
        val currentRunOptions = provider.getLastRunOptions(file)
        currentRunOptions.setProcessingScope(TextRangeType.WHOLE_FILE)
        return FileInEditorProcessor(file, editor, currentRunOptions)
    }

    private fun getCurrentEditorFile(editor: Editor): PsiFile {
        val project = editor.project
        val file = PsiDocumentManager.getInstance(project!!).getPsiFile(editor.document)
        return file!!
    }
}