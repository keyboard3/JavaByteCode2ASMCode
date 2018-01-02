package com.keyboard3.ASMTranslation.action;

import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.keyboard3.ASMTranslation.ui.Toast;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: dim
 * Date: 14-7-4
 * Time: 下午3:58
 */
public class DataWriter extends WriteCommandAction.Simple {
    private static String pattern0 = "\\W+([a-zA-Z_0-9]+)";
    private PsiClass cls;
    private PsiElementFactory factory;
    private Project project;
    private PsiFile file;
    private String content;

    public DataWriter(PsiFile file, Project project, PsiClass cls) {
        super(project, file);
        factory = JavaPsiFacade.getElementFactory(project);
        this.file = file;
        this.project = project;
        this.cls = cls;
    }

    public void execute(String content) {
        this.content = content;
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "GsonFormat") {

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);
                long currentTimeMillis = System.currentTimeMillis();
                execute();
                progressIndicator.setIndeterminate(false);
                progressIndicator.setFraction(1.0);
                StringBuffer sb = new StringBuffer();
                sb.append("GsonFormat [" + (System.currentTimeMillis() - currentTimeMillis) + " ms]\n");
                Toast.make(project, MessageType.INFO, sb.toString());
            }
        });
    }

    @NotNull
    @Override
    @Deprecated()
    public RunResult execute() {
        return super.execute();
    }

    @Override
    protected void run() {
        //执行方法插入
        // 将弹出dialog的方法写在StringBuilder里

        Pattern r = Pattern.compile(pattern0);

        String[] lines = content.split(":");
        StringBuilder sb = new StringBuilder();
        sb.append("void visitCode(){super.visitCode();");
        for (String item :
                lines) {
            Matcher m = r.matcher(item);
            if (m.find()) {
                sb.append(handleCode(m.group(1), item));
            } else {
                System.out.println("NO MATCH " + item);
            }
        }
        sb.append("}\n");
        // 将代码添加到当前类里
        cls.add(factory.createMethodFromText(sb.toString(), cls));
        // 导入需要的类
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
        styleManager.optimizeImports(file);
        styleManager.shortenClassReferences(cls);
    }

    private String handleCode(String instruct, String source) {
        StringBuilder sb = new StringBuilder();
        if (instruct.contains("invoke")) {
            String pattern = "//\\W+\\w+\\W+(.+)\\.(.+):(.+)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(source);
            if (m.find()) {
                String packName = m.group(1);
                String methodName = m.group(2);
                String sign = m.group(3);
                sb.append(String.format("methodVisitor.visitMethodInsn(Opcodes.%s,\"%s\",\"%s\",\"%s\");\n", instruct.toUpperCase(), packName, methodName, sign));
            } else {
                System.out.println("NO MATCH ldc");
            }
        } else if (instruct.contains("aload")) {
            String num = instruct.substring(instruct.lastIndexOf("_") + 1);
            sb.append("methodVisitor.visitVarInsn(Opcodes.ALOAD, " + num + ");\n");
        } else if (instruct.equals("ldc")) {
            String pattern = "//\\W+\\w+\\W+([a-zA-Z_0-9]+)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(source);
            if (m.find()) {
                String arg = m.group(1);
                sb.append("methodVisitor.visitLdcInsn(\"" + arg + "\");\n");
            } else {
                System.out.println("NO MATCH ldc");
            }
        }
        return sb.toString();
    }

}
