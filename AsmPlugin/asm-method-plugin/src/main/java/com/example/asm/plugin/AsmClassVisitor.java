package com.example.asm.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.HashSet;

public class AsmClassVisitor extends ClassVisitor implements Opcodes {

    private String mClassName;
    private String mainActivity;

    public AsmClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM6, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        Set<String> s = getActivityList("D:\\Android\\Project\\Android_Instrumentation\\AsmPlugin\\app\\src\\main\\AndroidManifest.xml");
        /**
         * flag = 0 this class is not an Activity
         * flag = 1 this class is an Activity class but not Main Activity
         * flag = 2 this class is Main Activity
         **/
        int flag = 0;
        if(mClassName.endsWith(mainActivity)) {
            flag = 2;
        }
        else {
            for(String str : s){
                if(mClassName.endsWith(str)){
                    flag = 1;
                }
            }
        }
        return new AsmMethodVisitor(mv, mClassName, name, desc, flag, signature);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    private Set<String> getActivityList(String xmlFilePath){
        Set<String> activitySet = new HashSet<>();
        File logFile = new File(xmlFilePath);
        if(logFile.isFile() && logFile.exists() && logFile.length() != 0) {
            try {
                InputStreamReader Reader = new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(Reader);
                String line;
                boolean flag = false;
                int count = 0;
                String activityName = "";
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.indexOf("<activity") != -1) {
                        flag = true;
                        count++;
                    }
                    if(flag && line.indexOf("android:name") != -1){
                        int index1 = line.indexOf("\"");
                        int index2 = line.lastIndexOf("\"");
                        activityName = line.substring(index1+1, index2).replace('.', '/');
                        activitySet.add(activityName);
                        flag = false;
                    }
                    if(line.indexOf("<action android:name=\"android.intent.action.MAIN\" />") != -1) {
                        this.mainActivity = activityName;
                    }
                }
//                System.out.println("There are " + count + " Activities registered in this app's xml file.");
            } catch (Exception e) {
                System.err.println("Reading File Error!");
                System.err.println(e);
            }
        }
        return activitySet;
    }
}
