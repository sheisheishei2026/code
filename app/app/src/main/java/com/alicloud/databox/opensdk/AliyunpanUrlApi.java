package com.alicloud.databox.opensdk;


import okhttp3.HttpUrl;

import java.util.Map;

public interface AliyunpanUrlApi {
    HttpUrl.Builder builder();

    HttpUrl buildUrl(String segments);

    HttpUrl buildUrl(String segments, Map<String, String> queryMap);

    class Factory {
        private static final String DEFAULT_API = "openapi.alipan.com";
        private static final String DEFAULT_SCHEME = "https";

        public static AliyunpanUrlApi getUriApi() {
            return new AliyunpanUrlApi() {
                @Override
                public HttpUrl.Builder builder() {
                    return new HttpUrl.Builder()
                            .scheme(DEFAULT_SCHEME)
                            .host(DEFAULT_API);
                }

                @Override
                public HttpUrl buildUrl(String segments) {
                    return new HttpUrl.Builder()
                            .scheme(DEFAULT_SCHEME)
                            .host(DEFAULT_API)
                            .addPathSegments(segments)
                            .build();
                }

                @Override
                public HttpUrl buildUrl(String segments, Map<String, String> queryMap) {
                    HttpUrl.Builder builder = new HttpUrl.Builder()
                            .scheme(DEFAULT_SCHEME)
                            .host(DEFAULT_API)
                            .addPathSegments(segments);

                    for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                        builder.addQueryParameter(entry.getKey(), entry.getValue());
                    }

                    return builder.build();
                }
            };
        }
    }
}
