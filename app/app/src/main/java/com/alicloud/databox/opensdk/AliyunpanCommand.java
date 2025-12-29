package com.alicloud.databox.opensdk;

import org.json.JSONObject;

public interface AliyunpanCommand {
    String getUrl();

    JSONObject getBody();
    // 标记接口，无需实现任何方法
}
