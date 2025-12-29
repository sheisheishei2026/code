package com.alicloud.databox.opensdk.http;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HttpHeaderInterceptor implements Interceptor {
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    private final HttpHeaderConfig config;

    public HttpHeaderInterceptor(HttpHeaderConfig config) {
        this.config = config;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        
        builder.addHeader(HEADER_USER_AGENT, config.getConfigUserAgent());
        String accessToken = config.getConfigAuthorization();
        if (accessToken != null && !accessToken.isEmpty()) {
            builder.addHeader(HEADER_AUTHORIZATION, accessToken);
        }

        return chain.proceed(builder.build());
    }

    public interface HttpHeaderConfig {
        String getConfigUserAgent();
        String getConfigAuthorization();
    }
} 