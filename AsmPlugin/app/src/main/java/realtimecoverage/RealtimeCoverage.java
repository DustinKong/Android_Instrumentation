package realtimecoverage;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RealtimeCoverage {
    private static Timer timer = new Timer();
    private static List<String> collectedMethods = new ArrayList<String>();

    private static final int INTERVAL = 1000;

    public static void init() {
        // Write the method call trace to a file
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveFile();
            }
        }, 0, INTERVAL);

//        // Or print the method call trace to the Logcat
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                printMethodInfo();
//            }
//        }, 0, INTERVAL);
    }

    private static void saveFile() {
        collectedMethods.clear();
        MethodVisitor.visitedMethods.drainTo(collectedMethods);
        if (collectedMethods.isEmpty()) return;
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/realtimecoverage");
        dir.mkdirs();
        File destFile = new File(dir, "coverage.txt");
        BufferedWriter fout;
        try {
            fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile, true)));
            for (String s: collectedMethods) {
                fout.write(s + "\n");
            }
            fout.close();
        } catch (Exception e) {
//            Log.e(Utils.LOG_TAG, e.toString());
            Log.e("Error: ", e.toString());
        }
    }

    private static void printMethodInfo() {
        collectedMethods.clear();
        MethodVisitor.visitedMethods.drainTo(collectedMethods);
        if (collectedMethods.isEmpty()) return;
        for (String s: collectedMethods) {
            Log.i("RealtimeMethodCoverage", s);
        }
    }
}
