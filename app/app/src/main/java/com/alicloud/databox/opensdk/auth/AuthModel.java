package com.alicloud.databox.opensdk.auth;

import com.alicloud.databox.opensdk.utils.DataStoreControl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthModel {
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String EXPIRES_IN = "expires_in";
    private static final String EXPIRED = "expired";

    private final String accessToken;
    private final String refreshToken;
    private final long expired;

    private AuthModel(String accessToken, String refreshToken, long expired) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expired = expired;
    }

    public boolean isValid() {
        return !accessToken.isEmpty() && System.currentTimeMillis() < expired;
    }

    public boolean supportRefresh() {
        return refreshToken != null && !refreshToken.isEmpty();
    }

    public void saveStore(DataStoreControl dataStoreControl) {
        Map<String, Object> data = new HashMap<>();
        data.put(ACCESS_TOKEN, accessToken);
        data.put(REFRESH_TOKEN, refreshToken);
        data.put(EXPIRED, String.valueOf(expired));
        dataStoreControl.saveDataStore(data);
    }

    public void clearStore(DataStoreControl dataStoreControl) {
        dataStoreControl.clearAll();
    }

    public static AuthModel parse(JSONObject jsonObject) {
        String accessToken = null;
        try {
            accessToken = jsonObject.getString(ACCESS_TOKEN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String refreshToken = null;
        try {
            refreshToken = jsonObject.getString(REFRESH_TOKEN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        long expiresIn = 0;
        try {
            expiresIn = jsonObject.getLong(EXPIRES_IN);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        long expired = System.currentTimeMillis() + (expiresIn * 1000);
        return new AuthModel(accessToken, refreshToken, expired);
    }

    public static AuthModel parse(DataStoreControl dataStoreControl) {
        String accessToken = dataStoreControl.getDataStore(ACCESS_TOKEN);
        if (accessToken == null) return null;

        String refreshToken = dataStoreControl.getDataStore(REFRESH_TOKEN);
        if (refreshToken == null) return null;

        String expiredStr = dataStoreControl.getDataStore(EXPIRED);
        if (expiredStr == null) return null;

        long expired;
        try {
            expired = Long.parseLong(expiredStr);
        } catch (Exception e) {
            return null;
        }

        return new AuthModel(accessToken, refreshToken, expired);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpired() {
        return expired;
    }
}
