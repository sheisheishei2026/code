package com.alicloud.databox.opensdk.auth;

import android.content.Context;
import android.util.Base64;
import com.alicloud.databox.opensdk.AliyunpanCredentials;
import com.alicloud.databox.opensdk.io.MessageDigestHelper;
import com.alicloud.databox.opensdk.utils.DataStoreControl;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliyunpanPKCECredentials extends AliyunpanCredentials {
    private static final String TAG = "PKCECredentials";
    private static final String METHOD_SHA_256 = "S256";
    private static final String METHOD_SHA_PLAIN = "plain";

    private final DataStoreControl dataStoreControl;
    private AuthModel authModel;
    private final String codeVerifier;

    public AliyunpanPKCECredentials(Context context, String appId, String identifier) {
        super(context, appId);
        this.dataStoreControl = new DataStoreControl(context, "PKCE", identifier);
        this.codeVerifier = getRandomString();
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
        map.put("code_challenge", getCodeChallenge());
        map.put("code_challenge_method", getCodeChallengeMethod());
        return map;
    }

    @Override
    public JSONObject getOAuthQRCodeRequest(List<String> scopes) {
        Map<String, Object> map = new HashMap<>();
        map.put("client_id", appId);
        map.put("bundle_id", context.getPackageName());
        map.put("scopes", scopes);
        map.put("source", "app");
        map.put("code_challenge", getCodeChallenge());
        map.put("code_challenge_method", getCodeChallengeMethod());
        return new JSONObject(map);
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
        Map<String, Object> map = new HashMap<>();
        map.put("client_id", appId);
        map.put("grant_type", "authorization_code");
        map.put("code", authCode);
        map.put("code_verifier", getCodeVerifier());
        return new JSONObject(map);
    }

    private String getCodeVerifier() {
        return codeVerifier;
    }

    private String getCodeChallenge() {
        String challengeMethod = getCodeChallengeMethod();
        if (METHOD_SHA_PLAIN.equals(challengeMethod)) {
            return getCodeVerifier();
        } else if (METHOD_SHA_256.equals(challengeMethod)) {
            byte[] bytes = getCodeVerifier().getBytes();
            byte[] sha256Bytes = new byte[0];
            try {
                sha256Bytes = MessageDigestHelper.getSHA256(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getBase64UrlSafe(sha256Bytes);
        }
        return "";
    }

    private String getCodeChallengeMethod() {
        return METHOD_SHA_256;
    }

    private static String getRandomString() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeByteArray = new byte[30];
        secureRandom.nextBytes(codeByteArray);
        return bytesToHex(codeByteArray);
    }

    private static String getBase64UrlSafe(byte[] input) {
        return Base64.encodeToString(input, Base64.URL_SAFE | Base64.NO_WRAP);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
