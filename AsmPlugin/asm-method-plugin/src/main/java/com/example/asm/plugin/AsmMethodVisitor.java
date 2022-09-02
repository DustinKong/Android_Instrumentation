package com.example.asm.plugin;

import org.objectweb.asm.*;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class AsmMethodVisitor extends MethodVisitor {
    private String className;
    private String methodName;
    Label newStart = new Label();
    private int lineNumber = -1;
    // Indicates whether the function needs to be instrumented
    private boolean flag = false;

    public AsmMethodVisitor(MethodVisitor mv, String className, String methodName) {
        super(Opcodes.ASM6, mv);
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        visitLabel(newStart);
        String pathName = "D:/Android/Project/EventHandlerFinder/MethodList.txt";
        File destFile = new File(pathName);
        // Instrument all functions if the target file doesn't exist
        if (!destFile.exists() || destFile.length() == 0) {
            flag = true;
            mv.visitLdcInsn(className);
            mv.visitLdcInsn(methodName);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "realtimecoverage/MethodVisitor", "visit", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        } else {
            try {
                InputStreamReader Reader = new InputStreamReader(new FileInputStream(destFile), "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(Reader);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if (lineTxt.equals(className + "/" + methodName)) {
                        System.out.println("Now instrumenting：" + className + "/" + methodName);
                        flag = true;
                        mv.visitLdcInsn(className);
                        mv.visitLdcInsn(methodName);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "realtimecoverage/MethodVisitor", "visit", "(Ljava/lang/String;Ljava/lang/String;)V", false);
                        break;
                    }
                }
                Reader.close();
            } catch (Exception e) {
                System.out.println("Reading File Error!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        if (newStart != null) {
            start = newStart;
            newStart = null;
            super.visitLineNumber(line, start);
            this.lineNumber = line;
            return;
        }
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitInsn(int opcode) {
        if (flag && ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW)) {
            mv.visitLdcInsn(className);
//            // If you need the method's line number, you can use this code
//            mv.visitLdcInsn(methodName + "_" + lineNumber);
            mv.visitLdcInsn(methodName);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "realtimecoverage/MethodVisitor", "visitFinish", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }


}
