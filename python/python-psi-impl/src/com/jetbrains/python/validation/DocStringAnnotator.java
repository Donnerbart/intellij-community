/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.validation;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.documentation.docstrings.*;
import com.jetbrains.python.highlighting.PyHighlighter;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * Highlights doc strings in classes, functions, and files.
 */
public final class DocStringAnnotator extends PyAnnotator {

  @Override
  public void visitPyFile(final @NotNull PyFile node) {
    annotateDocStringStmt(DocStringUtil.findDocStringExpression(node));
  }

  @Override
  public void visitPyFunction(final @NotNull PyFunction node) {
    annotateDocStringStmt(DocStringUtil.findDocStringExpression(node.getStatementList()));
  }

  @Override
  public void visitPyClass(final @NotNull PyClass node) {
    annotateDocStringStmt(DocStringUtil.findDocStringExpression(node.getStatementList()));
  }

  @Override
  public void visitPyAssignmentStatement(@NotNull PyAssignmentStatement node) {
    if (node.isAssignmentTo(PyNames.DOC)) {
      PyExpression right = node.getAssignedValue();
      if (right instanceof PyStringLiteralExpression) {
        getHolder().newSilentAnnotation(HighlightSeverity.INFORMATION).range(right).textAttributes(PyHighlighter.PY_DOC_COMMENT).create();
        annotateDocStringStmt((PyStringLiteralExpression)right);
      }
    }
  }

  @Override
  public void visitPyExpressionStatement(@NotNull PyExpressionStatement node) {
    if (node.getExpression() instanceof PyStringLiteralExpression &&
        DocStringUtil.isVariableDocString((PyStringLiteralExpression)node.getExpression())) {
      annotateDocStringStmt((PyStringLiteralExpression)node.getExpression());
    }
  }

  private void annotateDocStringStmt(final PyStringLiteralExpression stmt) {
    if (stmt != null) {
      final DocStringFormat format = DocStringUtil.getConfiguredDocStringFormat(stmt);
      final String[] tags;
      if (format == DocStringFormat.EPYTEXT) {
        tags = EpydocString.ALL_TAGS;
      }
      else if (format == DocStringFormat.REST) {
        tags = SphinxDocString.ALL_TAGS;
      }
      else {
        return;
      }
      int pos = 0;
      while (true) {
        TextRange textRange = DocStringReferenceProvider.findNextTag(stmt.getText(), pos, tags);
        if (textRange == null) break;
        getHolder().newSilentAnnotation(
          HighlightSeverity.INFORMATION).range(textRange.shiftRight(stmt.getTextRange().getStartOffset())).textAttributes(PyHighlighter.PY_DOC_COMMENT_TAG).create();
        pos = textRange.getEndOffset();
      }
    }
  }
}
