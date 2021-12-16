package com.example.forest;

import android.os.SystemClock;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by naresh on 17-Jul-2019.
 */
public class BaseActivity extends AppCompatActivity {
    private static final long MIN_CLICK_INTERVAL = 1000;
    private long lastClickTime = 0;

    protected void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    //method to avoid fast double click
    protected boolean isProcessClick() {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            return true;
        }
        return false;
    }
}
