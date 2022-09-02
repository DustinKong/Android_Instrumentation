package realtimecoverage;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static List<String> collectedMethods = new ArrayList<String>();

    private Context mContext;
    private volatile static CrashHandler mCrashHandler;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (mCrashHandler == null) {
            synchronized (CrashHandler.class) {
                if (mCrashHandler == null) {
                    mCrashHandler = new CrashHandler();
                }
            }
        }
        return mCrashHandler;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if(!MethodVisitor.visitedMethods.isEmpty()){
            Log.i("Themis", "The blocking queue is not empty!");
            collectedMethods.clear();
            MethodVisitor.visitedMethods.drainTo(collectedMethods);
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
            } catch (Exception ex) {
                Log.e("Error: ", ex.toString());
            }
        }
        if (mDefaultHandler != null) {
            Log.i("uncaughtException", "System Exception Handling...");
            mDefaultHandler.uncaughtException(t, e);
        } else {
            Log.i("uncaughtException", "Custom Exception Handling...");
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

}