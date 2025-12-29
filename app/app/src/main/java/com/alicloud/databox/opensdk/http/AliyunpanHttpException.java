package com.alicloud.databox.opensdk.http;

import com.alicloud.databox.opensdk.AliyunpanException;

public class AliyunpanHttpException extends AliyunpanException {
    
    public AliyunpanHttpException(String code, String message) {
        super(code, message);
    }

    @Override
    public String toString() {
        return "AliyunpanHttpException(code='" + getCode() + "', message='" + getMessage() + "')";
    }
} 