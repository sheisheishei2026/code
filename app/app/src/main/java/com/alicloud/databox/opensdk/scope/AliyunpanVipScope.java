package com.alicloud.databox.opensdk.scope;

import com.alicloud.databox.opensdk.AliyunpanScope;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface AliyunpanVipScope {

    /**
     * 获取付费功能列表
     */
    abstract class GetVipFeatureList implements AliyunpanScope {
        @Override
        public String getHttpMethod() {
            return "GET";
        }

        @Override
        public String getApi() {
            return "business/v1.0/vip/feature/list";
        }

        @Override
        public Map<String, Object> getRequest() {
            return Collections.emptyMap();
        }
    }

    /**
     * 开始试用付费功能
     * @param featureCode 付费功能。枚举列表
     */
    abstract class GetVipFeatureTrial implements AliyunpanScope {
        private final String featureCode;

        public GetVipFeatureTrial(String featureCode) {
            this.featureCode = featureCode;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "business/v1.0/vip/feature/trial";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("featureCode", featureCode);
            return map;
        }
    }
}
