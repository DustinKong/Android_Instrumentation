package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

public class GetMethodList {
    public static Set<String> s = new HashSet<>();
    public static final String APP_PACKAGE_NAME = "com/ichi2/anki";

    public static void readTxt(String filePath) {
        // Create sqlite-jdbc connection
        try (var c = DriverManager.getConnection("jdbc:sqlite::memory:")){
            var stmt = c.createStatement();
            stmt.executeUpdate("restore from listener.db");
            System.out.println("Opened database successfully！");
            File file = new File(filePath);
            if(file.isFile() && file.exists() && file.length() != 0) {
                try(InputStreamReader Reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(Reader)){
                    String line;
                    // tag: Whether the current method is the one we are interested in
                    boolean tag = false;
                    String head = "";
                    long startTime = System.currentTimeMillis();
                    while ((line = bufferedReader.readLine()) != null) {
                        String methodName = line.substring(line.lastIndexOf("/") + 1);
                        if (!tag && methodName.startsWith("on")){
                            String sql = "select * from event_handler_method where name='" + methodName + "' and name not like '%onCreate%'";
                            ResultSet rs = stmt.executeQuery(sql);
                            if (line.startsWith(APP_PACKAGE_NAME) && rs.next()) {
                                tag = true;
                                head = line;
                                System.out.println("Now instrumenting：" + line);
                            }
                        }
                        if (tag && !line.startsWith("[")) {
                            s.add(line);
                        }
                        if (line.equals("[" + head + "]")) {
                            tag = false;
                            head = "";
                        }
                    }
                    File destFile = new File("./MethodList.txt");
                    try (var of = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile, true)))){
                        for (String str: s) {
                            of.write(str + "\n");
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    long endTime = System.currentTimeMillis();
                    long time = endTime - startTime;
                    System.out.println("Instumentation costs: " + time + "ms");
                }
            } else {
                System.out.println("Can't find the target file!");
            }
        } catch (Exception e) {
            System.err.println("Reading File Error!");
            System.err.println(e);
        }
    }

    public static void main(String[] args) {
        String filePath = "coverage.txt";
        readTxt(filePath);
    }
}
