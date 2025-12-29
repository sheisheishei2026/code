package com.alicloud.databox.opensdk;

import android.util.Log;

public class LLogger {
    private static final String TAG = "AliyunpanSdk";

    private LLogger() {
        // Private constructor to prevent instantiation
    }

    static void log(String tag, String msg) {
        Log.d(TAG, tag + " " + msg);
    }

    static void log(String tag, String msg, Exception exception) {
        Log.e(TAG, tag + " " + msg, exception);
    }
}
