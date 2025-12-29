package com.alicloud.databox.opensdk.http;

import com.alicloud.databox.opensdk.AliyunpanException;

public class AliyunpanExceedMaxConcurrencyException extends AliyunpanException {
    
    public AliyunpanExceedMaxConcurrencyException(String code, String message) {
        super(code, message);
    }

    @Override
    public String toString() {
        return "AliyunpanExceedMaxConcurrencyException(code='" + getCode() + "', message='" + getMessage() + "')";
    }
} 