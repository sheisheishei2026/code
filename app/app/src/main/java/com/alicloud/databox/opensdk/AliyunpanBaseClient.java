package com.alicloud.databox.opensdk;

import android.app.Activity;
import okhttp3.OkHttpClient;

public interface AliyunpanBaseClient {
    void clearOauth();

    void fetchToken(Activity activity);

    boolean isInstanceYunpanApp();

    OkHttpClient getOkHttpInstance();
} 