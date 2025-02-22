/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.intellij.psi.impl.source.tree.java;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteral;
import com.intellij.psi.PsiAnnotationSupport;
import org.jetbrains.annotations.NotNull;

public final class JavaAnnotationSupport implements PsiAnnotationSupport {
  @Override
  @NotNull
  public PsiLiteral createLiteralValue(@NotNull String value, @NotNull PsiElement context) {
    return (PsiLiteral)JavaPsiFacade.getElementFactory(context.getProject()).createExpressionFromText("\"" + StringUtil.escapeStringCharacters(value) + "\"", null);
  }
}
