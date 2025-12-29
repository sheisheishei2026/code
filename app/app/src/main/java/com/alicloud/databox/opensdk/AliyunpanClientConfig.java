package com.alicloud.databox.opensdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import com.alicloud.databox.opensdk.auth.AliyunpanPKCECredentials;
import com.alicloud.databox.opensdk.auth.AliyunpanServerCredentials;
import com.alicloud.databox.opensdk.http.HttpHeaderInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AliyunpanClientConfig implements HttpHeaderInterceptor.HttpHeaderConfig {
    private static final String USER_AGENT_FORMAT = "%s/%s (%s; build:%s; Android %s) AliyunpanSDK/%s";
    public static final String SCOPE_USER_BASE = "user:base";
    public static final String SCOPE_FILE_READ = "file:all:read";
    public static final String SCOPE_FILE_WRITE = "file:all:write";
    public static final String SCOPE_ALBUM_SHARED_READ = "album:shared:read";
    private static final String SCOPE_SEPARATOR = ",";

      Context context;
      AppLevel appLevel;
      String scope;
      AliyunpanUrlApi urlApi;
      AliyunpanCredentials credentials;
      String downloadFolderPath;
      boolean autoLogin;
      String userAgent;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setAppLevel(AppLevel appLevel) {
        this.appLevel = appLevel;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setUrlApi(AliyunpanUrlApi urlApi) {
        this.urlApi = urlApi;
    }

    public void setCredentials(AliyunpanCredentials credentials) {
        this.credentials = credentials;
    }

    public void setDownloadFolderPath(String downloadFolderPath) {
        this.downloadFolderPath = downloadFolderPath;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public static String getUserAgentFormat() {
        return USER_AGENT_FORMAT;
    }

    public static String getScopeUserBase() {
        return SCOPE_USER_BASE;
    }

    public static String getScopeFileRead() {
        return SCOPE_FILE_READ;
    }

    public static String getScopeFileWrite() {
        return SCOPE_FILE_WRITE;
    }

    public static String getScopeAlbumSharedRead() {
        return SCOPE_ALBUM_SHARED_READ;
    }

    public static String getScopeSeparator() {
        return SCOPE_SEPARATOR;
    }

    public Context getContext() {
        return context;
    }

    public AppLevel getAppLevel() {
        return appLevel;
    }

    public String getScope() {
        return scope;
    }

    public AliyunpanUrlApi getUrlApi() {
        return urlApi;
    }

    public AliyunpanCredentials getCredentials() {
        return credentials;
    }

    public String getDownloadFolderPath() {
        return downloadFolderPath;
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public String getUserAgent() {
        return userAgent;
    }

    private AliyunpanClientConfig(Context context, AppLevel appLevel, String scope,
                                  AliyunpanUrlApi urlApi, AliyunpanCredentials credentials,
                                  String downloadFolderPath, boolean autoLogin) {
        this.context = context;
        this.appLevel = appLevel;
        this.scope = scope;
        this.urlApi = urlApi;
        this.credentials = credentials;
        this.downloadFolderPath = downloadFolderPath;
        this.autoLogin = autoLogin;
        this.userAgent = buildUserAgent();
    }

    private String buildUserAgent() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String appName = packageInfo.packageName.substring(packageInfo.packageName.lastIndexOf('.') + 1);
            return String.format(USER_AGENT_FORMAT,
                    appName,
                    packageInfo.versionName,
                    packageInfo.packageName,
                    getSdkBuild(),
                    Build.VERSION.RELEASE,
                    getSdkVersion());
        } catch (Exception e) {
            return "";
        }
    }

    private String getSdkVersion() {
        return "0.2.3";
    }

    public String getSdkBuild() {
        return "1";
    }

    @Override
    public String getConfigUserAgent() {
        return userAgent;
    }

    @Override
    public String getConfigAuthorization() {
        return credentials.getAccessToken();
    }

    public enum AppLevel {
        DEFAULT(3, 15 * 60),
        APPROVAL(6, 4 * 60 * 60),
        RISK(2, 15 * 60);

        private final int downloadTaskLimit;
        private final int downloadUrlExpireSec;

        AppLevel(int downloadTaskLimit, int downloadUrlExpireSec) {
            this.downloadTaskLimit = downloadTaskLimit;
            this.downloadUrlExpireSec = downloadUrlExpireSec;
        }

        public int getDownloadTaskLimit() {
            return downloadTaskLimit;
        }

        public int getDownloadUrlExpireSec() {
            return downloadUrlExpireSec;
        }
    }

    public static class Builder {
        private final Context context;
        private final String appId;
        private String identifier = "sdk_user";
        private List<String> scopes = Arrays.asList(SCOPE_USER_BASE, SCOPE_FILE_READ);
        private AppLevel appLevel = AppLevel.DEFAULT;
        private final AliyunpanUrlApi urlApi = AliyunpanUrlApi.Factory.getUriApi();
        private AliyunpanTokenServer tokenServer;
        private String downloadFolderPath = "";
        private boolean autoLogin = false;

        public Builder(Context context, String appId) {
            this.context = context.getApplicationContext();
            this.appId = appId;
        }

        public Builder scope(String scopes) {
            this.scopes = Arrays.asList(scopes.split(SCOPE_SEPARATOR));
            return this;
        }

        public Builder scope(List<String> scopes) {
            this.scopes = new ArrayList<>(scopes);
            return this;
        }

        public Builder appendScope(String scope) {
            this.scopes = new ArrayList<>(this.scopes);
            this.scopes.add(scope);
            return this;
        }

        public Builder appLevel(AppLevel appLevel) {
            this.appLevel = appLevel;
            return this;
        }

        public Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder tokenServer(AliyunpanTokenServer tokenServer) {
            this.tokenServer = tokenServer;
            return this;
        }

        public Builder downFolder(String downloadFolderPath) {
            this.downloadFolderPath = downloadFolderPath;
            return this;
        }

        public Builder downFolder(File downloadFolder) {
            this.downloadFolderPath = downloadFolder.getAbsolutePath();
            return this;
        }

        public Builder autoLogin() {
            this.autoLogin = true;
            return this;
        }

        public AliyunpanClientConfig build() {
            AliyunpanCredentials credentials = (tokenServer != null)
                ? new AliyunpanServerCredentials(context, appId, identifier, tokenServer)
                : new AliyunpanPKCECredentials(context, appId, identifier);

            return new AliyunpanClientConfig(
                context,
                appLevel,
                String.join(SCOPE_SEPARATOR, scopes),
                urlApi,
                credentials,
                downloadFolderPath,
                autoLogin
            );
        }
    }
}
