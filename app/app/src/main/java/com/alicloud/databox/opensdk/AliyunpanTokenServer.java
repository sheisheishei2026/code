package com.alicloud.databox.opensdk;

import org.json.JSONObject;

public interface AliyunpanTokenServer {
    /**
     * Get token request
     *
     * @param authCode 客户端授权回调的code
     * @return 获取token的请求JsonObject 同步方式
     */
    default JSONObject getTokenRequest(String authCode) {
        return null;
    }

    /**
     * Get token
     *
     * @param authCode authCode 客户端授权回调的code
     * @param onResult 异步回调的token结果 JsonObject结果
     */
    default void getToken(String authCode, Consumer<JSONObject> onResult) {
        onResult.accept(null);
    }

    /**
     * Get oauth qrcode request
     *
     * @param scopes 申请的授权范围
     * @return 获取授权二维码的请求JsonObject 同步方式
     */
    default JSONObject getOAuthQRCodeRequest(java.util.List<String> scopes) {
        return null;
    }

    /**
     * Get oauth qrcode request
     *
     * @param scopes 申请的授权范围
     * @param onResult 异步回调的获取授权二维码的请求JsonObject
     */
    default void getOAuthQRCodeRequest(java.util.List<String> scopes, Consumer<JSONObject> onResult) {
        onResult.accept(null);
    }
} 