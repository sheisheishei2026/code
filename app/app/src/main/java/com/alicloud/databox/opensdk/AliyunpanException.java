package com.alicloud.databox.opensdk;

public class AliyunpanException extends Exception {
    private final String code;

    public AliyunpanException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "AliyunpanException(code='" + code + "', message='" + getMessage() + "')";
    }

//    public static class ErrorCodes {
        public static final String CODE_AUTH_REDIRECT_INVALID = "AuthRedirectInvalid";
        public static final String CODE_AUTH_REDIRECT_ERROR = "AuthRedirectError";
        public static final String CODE_AUTH_QRCODE_ERROR = "AuthQRCodeError";
        public static final String CODE_REQUEST_INVALID = "RequestInvalid";
        public static final String CODE_DOWNLOAD_ERROR = "DownloadError";
        public static final String CODE_UPLOAD_ERROR = "UploadError";

//        private ErrorCodes() {
//            // Private constructor to prevent instantiation
//        }

        public static AliyunpanException buildError(String code, String message) {
            return new AliyunpanException(code, message);
        }
//    }
}
