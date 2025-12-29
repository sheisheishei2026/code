package com.alicloud.databox.opensdk;

import android.content.Context;
import org.json.JSONObject;

public abstract class AliyunpanCredentials implements AliyunpanTokenServer {
    protected final Context context;
    protected final String appId;

    public AliyunpanCredentials(Context context, String appId) {
        this.context = context;
        this.appId = appId;
    }

    public abstract boolean preCheckTokenValid();

    public abstract void clearToken();

    public abstract java.util.Map<String, String> getOAuthRequest(String scope);

    public abstract String getAccessToken();

    public abstract void updateAccessToken(JSONObject jsonObject) throws Exception;
} 