package com.alicloud.databox.opensdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AliyunpanBroadcastHelper {
    private static final String TAG = "AliyunpanBroadcastHelper";
    private static final IntentFilter filter;

    static {
        filter = new IntentFilter();
        for (AliyunpanAction action : AliyunpanAction.values()) {
            filter.addAction(action.name());
        }
        filter.setPriority(1000);
    }

    private AliyunpanBroadcastHelper() {
        // Private constructor to prevent instantiation
    }

    public static void registerReceiver(Context context, BroadcastReceiver receiver) {
        try {
            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
        } catch (Exception e) {
            LLogger.log(TAG, "registerReceiver", e);
        }
    }

    public static void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        try {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        } catch (Exception e) {
            LLogger.log(TAG, "unregisterReceiver", e);
        }
    }

    static void sentBroadcast(Context context, AliyunpanAction action) {
        sentBroadcast(context, action, null);
    }

    static void sentBroadcast(Context context, AliyunpanAction action, String message) {
        Intent intent = new Intent(action.name());
        if (message != null && !message.isEmpty()) {
            intent.putExtra("message", message);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
} 