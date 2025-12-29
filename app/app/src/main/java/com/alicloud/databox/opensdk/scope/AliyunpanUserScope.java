package com.alicloud.databox.opensdk.scope;

import com.alicloud.databox.opensdk.AliyunpanScope;
import java.util.Collections;
import java.util.Map;

public interface AliyunpanUserScope {

    /**
     * 通过 access_token 获取用户信息
     */
    abstract class GetUsersInfo implements AliyunpanScope {
        @Override
        public String getHttpMethod() {
            return "GET";
        }

        @Override
        public String getApi() {
            return "oauth/users/info";
        }

        @Override
        public Map<String, Object> getRequest() {
            return Collections.emptyMap();
        }
    }

    /**
     * 获取用户信息和drive信息
     */
    abstract class GetDriveInfo implements AliyunpanScope {
        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/user/getDriveInfo";
        }

        @Override
        public Map<String, Object> getRequest() {
            return Collections.emptyMap();
        }
    }

    /**
     * 获取用户空间信息
     */
    abstract class GetSpaceInfo implements AliyunpanScope {
        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/user/getSpaceInfo";
        }

        @Override
        public Map<String, Object> getRequest() {
            return Collections.emptyMap();
        }
    }

    /**
     * 获取用户vip信息
     */
    abstract class GetVipInfo implements AliyunpanScope {
        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "v1.0/user/getVipInfo";
        }

        @Override
        public Map<String, Object> getRequest() {
            return Collections.emptyMap();
        }
    }

    /**
     * 通过 access_token 获取用户权限信息
     */
    abstract class GetUsersScopes implements AliyunpanScope {
        @Override
        public String getHttpMethod() {
            return "GET";
        }

        @Override
        public String getApi() {
            return "oauth/users/scopes";
        }

        @Override
        public Map<String, Object> getRequest() {
            return Collections.emptyMap();
        }
    }
}
