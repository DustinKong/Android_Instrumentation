package realtimecoverage;

import android.util.Log;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MethodVisitor {
    public static BlockingQueue<String> visitedMethods;

    static {
        visitedMethods = new LinkedBlockingQueue<String>();
    }

    public static void visit(String className, String methodName) {
        try {
            visitedMethods.put(className + "/" + methodName);
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
    }

    public static void visitFinish(String className, String methodName) {
        try {
            visitedMethods.put("[" + className + "/" + methodName + "]");
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
    }
}
