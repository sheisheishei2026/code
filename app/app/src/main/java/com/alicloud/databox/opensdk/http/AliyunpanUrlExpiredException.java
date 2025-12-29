package com.alicloud.databox.opensdk.http;

import com.alicloud.databox.opensdk.AliyunpanException;

public class AliyunpanUrlExpiredException extends AliyunpanException {
    
    public AliyunpanUrlExpiredException(String code, String message) {
        super(code, message);
    }

    @Override
    public String toString() {
        return "AliyunpanUrlExpiredException(code='" + getCode() + "', message='" + getMessage() + "')";
    }
} 