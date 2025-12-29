package com.alicloud.databox.opensdk.http;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {
    private final TokenAuthenticatorConfig config;

    public TokenAuthenticator(TokenAuthenticatorConfig config) {
        this.config = config;
    }

    @Override
    public Request authenticate(Route route, Response response) {
        config.authInvalid();
        return null;
    }

    public interface TokenAuthenticatorConfig {
        void authInvalid();
    }
} 