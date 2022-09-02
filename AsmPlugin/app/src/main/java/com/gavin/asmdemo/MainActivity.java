package com.gavin.asmdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the blocking queue of Method-Call-Listener
        realtimecoverage.RealtimeCoverage.init();
        super.onCreate(savedInstanceState);
        // Set an interface for handlers invoked when a Thread abruptly terminates due to an uncaught exception
        realtimecoverage.CrashHandler crashHandler = realtimecoverage.CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        setContentView(R.layout.activity_main);
    }

    public void toSecond(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }
}
