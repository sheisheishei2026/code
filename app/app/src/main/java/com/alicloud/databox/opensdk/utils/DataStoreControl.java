package com.alicloud.databox.opensdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;

public class DataStoreControl {
    private final Context context;
    private final String name;
    private final String identifier;

    public DataStoreControl(Context context, String name, String identifier) {
        this.context = context;
        this.name = name;
        this.identifier = identifier;
    }

    public void saveDataStore(String key, String value) {
        getSharedPreferences()
            .edit()
            .putString(getIdKey(key), value)
            .apply();
    }

    public void saveDataStore(Map<String, Object> map) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            editor.putString(getIdKey(entry.getKey()), entry.getValue().toString());
        }
        editor.apply();
    }

    public void clearAll() {
        getSharedPreferences()
            .edit()
            .clear()
            .apply();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences("YunpanSdk-" + name + ".sp", Context.MODE_PRIVATE);
    }

    public String getDataStore(String key) {
        return getDataStore(key, null);
    }

    public String getDataStore(String key, String defValue) {
        return getSharedPreferences().getString(getIdKey(key), defValue);
    }

    private String getIdKey(String key) {
        return identifier + "_" + key;
    }
} 