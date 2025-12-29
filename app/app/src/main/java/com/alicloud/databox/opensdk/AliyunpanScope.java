package com.alicloud.databox.opensdk;

import java.util.Map;

public interface AliyunpanScope extends AliyunpanCommand {
    String getHttpMethod();

    String getApi();

    Map<String, Object> getRequest();
} 