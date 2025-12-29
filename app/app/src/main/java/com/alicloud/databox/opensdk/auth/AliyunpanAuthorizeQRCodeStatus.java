package com.alicloud.databox.opensdk.auth;

public enum AliyunpanAuthorizeQRCodeStatus {
    /**
     * 等待扫码
     */
    WAIT_LOGIN("WaitLogin"),

    /**
     * 扫码成功，待确认
     */
    SCAN_SUCCESS("ScanSuccess"),

    /**
     * 授权成功
     */
    LOGIN_SUCCESS("LoginSuccess"),

    /**
     * 二维码失效
     */
    QRCODE_EXPIRED("QRCodeExpired");

    private final String stateName;

    AliyunpanAuthorizeQRCodeStatus(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }
} 