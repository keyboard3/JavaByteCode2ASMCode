package com.keyboard3.ASMTranslation;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import com.keyboard3.ASMTranslation.ui.JsonDialog;

/**
 * @author keyboard3
 * @date 2018/1/2
 */
public class ASMTranslateAction extends BaseGenerateAction {

    public ASMTranslateAction() {
        super(null);
    }

    public ASMTranslateAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        PsiFile mFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiClass psiClass = getTargetClass(editor, mFile);
        JsonDialog jsonD = new JsonDialog(psiClass, mFile, project);
        jsonD.setClass(psiClass);
        jsonD.setFile(mFile);
        jsonD.setProject(project);
        jsonD.setSize(600, 400);
        jsonD.setLocationRelativeTo(null);
        jsonD.setVisible(true);
    }
}
