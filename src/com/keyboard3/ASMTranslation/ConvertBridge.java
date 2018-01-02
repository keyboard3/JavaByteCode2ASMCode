package com.keyboard3.ASMTranslation;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.keyboard3.ASMTranslation.action.DataWriter;
import com.keyboard3.ASMTranslation.common.PsiClassUtil;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by dim on 2015/8/21.
 * 把 json 转成 实体类
 */
public class ConvertBridge {

    private PsiClass targetClass;
    private PsiClass currentClass;
    private PsiElementFactory factory;
    private Project project;
    private PsiFile file;
    private String jsonStr;
    private String generateClassName;
    private StringBuilder fullFilterRegex = null;
    private StringBuilder briefFilterRegex = null;
    private String filterRegex = null;
    private Operator operator;
    private String packageName;

    public ConvertBridge(Operator operator,
                         String jsonStr, PsiFile file, Project project,
                         PsiClass targetClass,
                         PsiClass currentClass, String generateClassName) {

        factory = JavaPsiFacade.getElementFactory(project);
        this.file = file;
        this.generateClassName = generateClassName;
        this.operator = operator;
        this.jsonStr = jsonStr;
        this.project = project;
        this.targetClass = targetClass;
        this.currentClass = currentClass;
        fullFilterRegex = new StringBuilder();
        briefFilterRegex = new StringBuilder();

    }

    public void run() {
        operator.cleanErrorInfo();
        try {
            operator.setVisible(false);
            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                @Override
                public void run() {
                    if (targetClass == null) {
                        try {
                            targetClass = PsiClassUtil.getPsiClass(file, project, generateClassName);
                        } catch (Throwable throwable) {
                            handlePathError(throwable);
                        }
                    }
                    if (targetClass != null) {
                        try {
                            operator.setVisible(false);
                            DataWriter dataWriter = new DataWriter(file, project, targetClass);
                            dataWriter.execute(jsonStr);
                            operator.dispose();
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                }
            });
        } catch (Exception e2) {
            handleDataError(e2);
            operator.setVisible(true);
        }
    }



    private void handleDataError(Exception e2) {
        e2.printStackTrace();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e2.printStackTrace(printWriter);
        printWriter.close();
        operator.showError(Error.DATA_ERROR);
        operator.setErrorInfo(writer.toString());
    }

    public String getPackName(PsiClass psiClass) {
        String packName = null;
        if (psiClass.getQualifiedName() != null) {
            int i = psiClass.getQualifiedName().lastIndexOf(".");
            if (i >= 0) {
                packName = psiClass.getQualifiedName().substring(0, i);
            } else {
                packName = psiClass.getQualifiedName();
            }
        }
        return packName;

    }

    /**
     * 过滤掉// 和/** 注释
     *
     * @param str
     * @return
     */
    public String removeComment(String str) {
        String temp = str.replaceAll("/\\*" +
                "[\\S\\s]*?" +
                "\\*/", "");
        return temp.replaceAll("//[\\S\\s]*?\n", "");
    }



    private boolean isCollection(PsiClass element) {

        if ("java.util.Collection".equals(element.getQualifiedName())) {
            return true;
        }
        for (PsiClass psiClass : element.getInterfaces()) {
            if (isCollection(psiClass)) {
                return true;
            }
        }
        return false;
    }



    private void handlePathError(Throwable throwable) {
        throwable.printStackTrace();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        operator.setErrorInfo(writer.toString());
        operator.setVisible(true);
        operator.showError(Error.PATH_ERROR);
    }

    public interface Operator {

        void showError(Error err);

        void dispose();

        void setVisible(boolean visible);

        void setErrorInfo(String error);

        void cleanErrorInfo();
    }

    public enum Error {
        DATA_ERROR, PARSE_ERROR, PATH_ERROR;
    }
}

