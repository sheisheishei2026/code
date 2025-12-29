package com.alicloud.databox.opensdk.auth;

import android.content.Context;
import com.alicloud.databox.opensdk.AliyunpanCredentials;
import com.alicloud.databox.opensdk.AliyunpanTokenServer;
import com.alicloud.databox.opensdk.utils.DataStoreControl;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AliyunpanServerCredentials extends AliyunpanCredentials implements AliyunpanTokenServer {
    private final DataStoreControl dataStoreControl;
    private final AliyunpanTokenServer tokenServer;
    private AuthModel authModel;

    public AliyunpanServerCredentials(Context context, String appId, String identifier, 
                                    AliyunpanTokenServer tokenServer) {
        super(context, appId);
        this.dataStoreControl = new DataStoreControl(context, "Server", identifier);
        this.tokenServer = tokenServer;
        this.authModel = AuthModel.parse(dataStoreControl);
    }

    @Override
    public boolean preCheckTokenValid() {
        return authModel != null && authModel.isValid();
    }

    @Override
    public void clearToken() {
        if (authModel != null) {
            authModel.clearStore(dataStoreControl);
            authModel = null;
        }
    }

    @Override
    public Map<String, String> getOAuthRequest(String scope) {
        Map<String, String> map = new HashMap<>();
        map.put("client_id", appId);
        map.put("bundle_id", context.getPackageName());
        map.put("scope", scope);
        map.put("redirect_uri", "oob");
        map.put("response_type", "code");
        return map;
    }

    @Override
    public String getAccessToken() {
        return authModel != null ? authModel.getAccessToken() : null;
    }

    @Override
    public void updateAccessToken(JSONObject jsonObject) {
        this.authModel = AuthModel.parse(jsonObject);
        if (this.authModel != null) {
            this.authModel.saveStore(dataStoreControl);
        }
    }

    @Override
    public JSONObject getTokenRequest(String authCode) {
        return tokenServer.getTokenRequest(authCode);
    }
} 