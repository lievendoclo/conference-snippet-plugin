package be.sourcedbvba.gradle.plugin.conference

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.nhaarman.mockito_kotlin.*
import com.sun.jna.platform.win32.WinNT
import io.damo.aspen.Test
import org.assertj.core.api.Assertions.*
import org.mockito.ArgumentCaptor

val project = mock<Project>()
val editor = mock<Editor>()
val caretModel = mock<CaretModel>()
val psiFileLookup = mock<PsiFileLookup>()
val editorFileFormatter = mock<EditorFileFormatter>()

class SnippetCompletionContributorTests : Test({

    before {
        reset(project, editor, caretModel, psiFileLookup)
        whenever(editor.project).thenReturn(project)
        whenever(editor.caretModel).thenReturn(caretModel)
    }

    describe("load snippets into resultset") {

        test("snippets are loaded") {
            val snippetLookup = mock<SnippetLookup>()
            val snippets = listOf(Snippet("test", "this is a test"))
            whenever(snippetLookup.getSnippets(project)).thenReturn(snippets)
            val snippetCompletionContributor = SnippetCompletionContributor(snippetLookup, psiFileLookup, editorFileFormatter)
            val completionParameters = mock<CompletionParameters>()
            whenever(completionParameters.editor).thenReturn(editor)
            val completionResultSet = mock<CompletionResultSet>()

            snippetCompletionContributor.fillCompletionVariants(completionParameters,  completionResultSet)

            val lookupElement = verifyElementWasAdded(completionResultSet)
            assertLookupElementHasCorrectText(lookupElement)
            assertLookupElementHasCorrectBehavior(lookupElement)
        }
    }
})

private fun assertLookupElementHasCorrectBehavior(lookupElement: LookupElement) {
    val insertionContext = mock<InsertionContext>()
    val document = mock<Document>()
    whenever(insertionContext.editor).thenReturn(editor)
    whenever(editor.document).thenReturn(document)
    whenever(insertionContext.document).thenReturn(document)
    whenever(caretModel.offset).thenReturn(10)
    val psiDocumentManager = mock<PsiDocumentManager>()
    whenever(PsiDocumentManager.getInstance(project)).thenReturn(psiDocumentManager)
    val psiFile = mock<PsiFile>()
    whenever(psiFileLookup.lookup(project, document)).thenReturn(psiFile)
    lookupElement.handleInsert(insertionContext)

    verify(document).deleteString(9, 10)
    verify(editorFileFormatter).format(psiFile, editor)
}

private fun assertLookupElementHasCorrectText(lookupElement: LookupElement) {
    assertThat(lookupElement.lookupString).isEqualTo("this is a test")
    val lookupPresentation = LookupElementPresentation()
    lookupElement.renderElement(lookupPresentation)
    assertThat(lookupPresentation.itemText).isEqualTo("test")
    assertThat(lookupPresentation.typeText).isEqualTo("Conference snippet")
}

private fun verifyElementWasAdded(completionResultSet: CompletionResultSet): LookupElement {
    val lookupElementCaptor = ArgumentCaptor.forClass(LookupElement::class.java)
    verify(completionResultSet).addElement(lookupElementCaptor.capture())
    return lookupElementCaptor.value
}