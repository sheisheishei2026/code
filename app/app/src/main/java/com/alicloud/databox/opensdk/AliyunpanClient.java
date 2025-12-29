package com.alicloud.databox.opensdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.alicloud.databox.opensdk.Consumer;

import androidx.annotation.RequiresApi;

import com.alicloud.databox.opensdk.auth.AliyunpanAuthorizeQRCodeStatus;
import com.alicloud.databox.opensdk.auth.AliyunpanQRCodeAuthTask;
import com.alicloud.databox.opensdk.auth.AliyunpanServerCredentials;
import com.alicloud.databox.opensdk.http.HttpHeaderInterceptor;
import com.alicloud.databox.opensdk.http.OKHttpHelper;
import com.alicloud.databox.opensdk.http.TokenAuthenticator;
import com.alicloud.databox.opensdk.io.AliyunpanDownloader;
//import com.alicloud.databox.opensdk.io.AliyunpanUploader;
import com.alicloud.databox.opensdk.io.BaseTask;
import com.alicloud.databox.opensdk.scope.AliyunpanFileScope;
import com.alicloud.databox.opensdk.scope.AliyunpanUserScope;
import com.alicloud.databox.opensdk.scope.AliyunpanVideoScope;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliyunpanClient implements AliyunpanBaseClient, TokenAuthenticator.TokenAuthenticatorConfig {
    private static final String TAG = "AliyunpanClient";
    public static final String CALLBACK_CODE = "code";
    public static final String CALLBACK_ERROR = "error";

    private final AliyunpanClientConfig config;
    private final Handler handler;
//    private final AliyunpanUploader uploader;
    private final OkHttpClient okHttpInstance;

//    private AliyunpanQRCodeAuthTask qrCodeAuthTask;

    private AliyunpanClient(AliyunpanClientConfig config) {
        this.config = config;
        this.handler = new Handler(Looper.myLooper());

//        this.uploader = new AliyunpanUploader(this, config.getCredentials());
        this.okHttpInstance = OKHttpHelper.buildOKHttpClient(this, config);

        Context context = config.getContext();
        AliyunpanCredentials credentials = config.getCredentials();

        if (credentials.preCheckTokenValid()) {
            AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_LOGIN_SUCCESS);
        } else {
            AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_RESET_STATUS);
        }
    }

    public static AliyunpanClient init(AliyunpanClientConfig config) {
        LLogger.log(TAG, "init");
        return new AliyunpanClient(config);
    }

    @Override
    public void clearOauth() {
        AliyunpanBroadcastHelper.sentBroadcast(config.getContext(), AliyunpanAction.NOTIFY_RESET_STATUS);
        config.getCredentials().clearToken();
    }

    public void oauthQRCode(Consumer<AliyunpanQRCodeAuthTask> onSuccess, Consumer<Exception> onFailure) {
        AliyunpanCredentials credentials = config.getCredentials();
        String[] scopes = config.getScope().split(",");
        JSONObject requestJson = credentials.getOAuthQRCodeRequest(Arrays.asList(scopes));

        if (requestJson != null) {
            oauthQRCode(requestJson, onSuccess, onFailure);
            return;
        }

        credentials.getOAuthQRCodeRequest(Arrays.asList(scopes), value -> {
            handler.post(() -> {
                if (value != null) {
                    oauthQRCode(value, onSuccess, onFailure);
                } else {
                    LLogger.log(TAG, "oauthQRCode TokenServer not implement getOAuthQRCodeRequest");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        onFailure.accept(AliyunpanException.buildError(
                            AliyunpanException.CODE_AUTH_QRCODE_ERROR,
                            "TokenServer not implement getOAuthQRCodeRequest"
                        ));
                    }
                }
            });
        });
    }

    private void oauthQRCode(JSONObject requestJson, Consumer<AliyunpanQRCodeAuthTask> onSuccess,
                           Consumer<Exception> onFailure) {
        Request request = new Request.Builder()
            .url(config.getUrlApi().buildUrl("oauth/authorize/qrcode"))
            .post(RequestBody.create(requestJson.toString(),
                  MediaType.parse("application/json")))
            .build();

        OKHttpHelper.enqueue(okHttpInstance, request, handler,
            response -> {
                JSONObject jsonObject = response.getData().asJSONObject();
                String qrcodeUrl = jsonObject.optString("qrCodeUrl");
                String sid = jsonObject.optString("sid");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    onSuccess.accept(new AliyunpanQRCodeAuthTask(this, config.getUrlApi(), qrcodeUrl, sid));
                }
            },
            error -> {
                LLogger.log(TAG, "oauth qrcode request failed", error);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    onFailure.accept(error);
                }
            });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void oauth(Consumer<Void> onSuccess, Consumer<Exception> onFailure) {
        Context context = config.getContext();
        AliyunpanCredentials credentials = config.getCredentials();

        if (credentials.preCheckTokenValid()) {
            AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_LOGIN_SUCCESS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                onSuccess.accept(null);
            }
            return;
        }

        boolean installApp = isInstanceYunpanApp();
        Map<String, String> requestQuery = new HashMap<>();
        requestQuery.put("source", installApp ? "app" : "appLink");

        if (!installApp && config.isAutoLogin()) {
            requestQuery.put("auto_login", "true");
        }

        requestQuery.putAll(credentials.getOAuthRequest(config.getScope()));

        Request request = new Request.Builder()
            .url(config.getUrlApi().buildUrl("oauth/authorize", requestQuery))
            .build();

        OKHttpHelper.enqueue(okHttpInstance, request, handler,
            response -> {
                JSONObject jsonObject = response.getData().asJSONObject();
                String originRedirectUri = jsonObject.optString("redirectUri");

                String redirectUri;
                if (installApp) {
                    if (preCheckAppScheme(originRedirectUri)) {
                        Exception exception = AliyunpanException.buildError(
                            AliyunpanException.CODE_AUTH_REDIRECT_INVALID,
                            "redirectUri is error uri = " + originRedirectUri
                        );
                        LLogger.log(TAG, "oauth redirectUri error", exception);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            onFailure.accept(exception);
                        }
                        return;
                    }
                    redirectUri = originRedirectUri;
                } else {
                    redirectUri = originRedirectUri.replace("alipan.com/applink/authorize",
                                                          "alipan.com/o/oauth/authorize")
                                                 + "&source=app_link&deep_link=true";
                }

                if (startAppScheme(context, redirectUri)) {
                    LLogger.log(TAG, "oauth redirectUri = " + redirectUri);
                    onSuccess.accept(null);
                } else {
                    Exception exception = AliyunpanException.buildError(
                        AliyunpanException.CODE_AUTH_REDIRECT_ERROR,
                        "start redirect failed"
                    );
                    LLogger.log(TAG, "oauth redirectUri error", exception);
                    onFailure.accept(exception);
                }
            },
            error -> {
                LLogger.log(TAG, "oauth request failed", error);
                onFailure.accept(error);
            });
    }

    private boolean preCheckAppScheme(String appUri) {
        if (appUri.isEmpty()) {
            return false;
        }
        return !appUri.startsWith("smartdrive");
    }

    private boolean startAppScheme(Context context, String appUri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appUri))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void fetchToken(Activity activity) {
        Intent intent = activity.getIntent();
        Uri data = intent.getData();

        if (data != null) {
            fetchToken(data.getQueryParameter(CALLBACK_CODE), data.getQueryParameter(CALLBACK_ERROR));
        } else {
            fetchToken(intent.getStringExtra(CALLBACK_CODE), intent.getStringExtra(CALLBACK_ERROR));
        }

        activity.finish();
    }

    void fetchToken(String code, String error) {
        Context context = config.getContext();

        if (error != null && !error.isEmpty()) {
            LLogger.log(TAG, "fetchToken error = " + error);
            AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_LOGIN_CANCEL, error);
            return;
        }

        if (code == null || code.isEmpty()) {
            LLogger.log(TAG, "fetchToken code is null or empty");
            AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_LOGIN_FAILED,
                "code is null or empty");
            return;
        }

        AliyunpanCredentials credentials = config.getCredentials();
        JSONObject requestJson = credentials.getTokenRequest(code);

        if (requestJson != null) {
            Request request = new Request.Builder()
                .url(config.getUrlApi().buildUrl("oauth/access_token"))
                .post(RequestBody.create(requestJson.toString(), MediaType.parse("application/json")))
                .build();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                OKHttpHelper.enqueue(okHttpInstance, request, handler,
                        new Consumer<ResultResponse>() {
                            @Override
                            public void accept(ResultResponse response) {
                                try {
                                    credentials.updateAccessToken(response.getData().asJSONObject());
                                    LLogger.log(TAG, "fetchToken getTokenRequest success");
                                    AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_LOGIN_SUCCESS);
                                } catch (Exception e) {
                                    LLogger.log(TAG, "fetchToken getTokenRequest failed", e);
                                    AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_LOGIN_FAILED,
                                            e.getMessage());
                                }
                            }
                        },
                        new Consumer<Exception>() {
                            @Override
                            public void accept(Exception error) {
                                LLogger.log(TAG, "fetchToken getTokenRequest failed", error);
                                AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_LOGIN_FAILED,
                                        error.getMessage());
                            }
                        }
                );
            }
            return;
        }

        credentials.getToken(code, value -> {
            handler.post(() -> {
                if (value != null) {
                    try {
                        credentials.updateAccessToken(value);
                        LLogger.log(TAG, "fetchToken getToken success");
                        AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_LOGIN_SUCCESS);
                    } catch (Exception e) {
                        LLogger.log(TAG, "fetchToken getToken failed", e);
                        AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_LOGIN_FAILED,
                            e.getMessage());
                    }
                } else {
                    LLogger.log(TAG, "fetchToken TokenServer not implement getTokenRequest or getToken");
                    AliyunpanBroadcastHelper.sentBroadcast(context, AliyunpanAction.NOTIFY_LOGIN_FAILED,
                        "TokenServer not implement getTokenRequest or getToken");
                }
            });
        });
    }

    @Override
    public void authInvalid() {
        AliyunpanBroadcastHelper.sentBroadcast(config.getContext(), AliyunpanAction.NOTIFY_LOGOUT);
    }

    public void send(AliyunpanScope scope, Consumer<ResultResponse> onSuccess, Consumer<Exception> onFailure) {
        Request request = buildRequest(scope);
        if (request == null) {
            Exception exception = AliyunpanException.buildError(
                AliyunpanException.CODE_REQUEST_INVALID,
                "build request failed"
            );
            LLogger.log(TAG, "send failed", exception);
            onFailure.accept(exception);
            return;
        }
        send(request, onSuccess, onFailure);
    }

    public void send(Request request, Consumer<ResultResponse> onSuccess, Consumer<Exception> onFailure) {
        OKHttpHelper.enqueue(okHttpInstance, request, handler, onSuccess, onFailure);
    }

    ResultResponse sendSync(AliyunpanScope scope) throws Exception {
        Request request = buildRequest(scope);
        if (request == null) {
            Exception exception = AliyunpanException.buildError(
                AliyunpanException.CODE_REQUEST_INVALID,
                "build request failed"
            );
            LLogger.log(TAG, "sendSync failed", exception);
            throw exception;
        }
        return sendSync(request);
    }

    public ResultResponse sendSync(Request request) throws Exception {
        return OKHttpHelper.execute(okHttpInstance, request);
    }

    private Request buildRequest(AliyunpanScope scope) {
        HttpUrl.Builder baseHttpUrl = config.getUrlApi().builder();

        switch (scope.getHttpMethod()) {
            case "POST":
                Map<String, Object> filteredRequest = new HashMap<>();
                for (Map.Entry<String, Object> entry : scope.getRequest().entrySet()) {
                    if (entry.getValue() != null) {
                        filteredRequest.put(entry.getKey(), entry.getValue());
                    }
                }
                return new Request.Builder()
                    .url(((HttpUrl.Builder) baseHttpUrl).addPathSegments(scope.getApi()).build())
                    .post(RequestBody.create(
                        new JSONObject(filteredRequest).toString(),
                        MediaType.parse("application/json")
                    ))
                    .build();

            case "GET":
                HttpUrl.Builder urlBuilder = baseHttpUrl.addPathSegments(scope.getApi());
                for (Map.Entry<String, Object> entry : scope.getRequest().entrySet()) {
                    if (entry.getValue() != null) {
                        urlBuilder.addQueryParameter(entry.getKey(), entry.getValue().toString());
                    }
                }
                return new Request.Builder()
                    .url(urlBuilder.build())
                    .get()
                    .build();

            default:
                return null;
        }
    }

    @Override
    public boolean isInstanceYunpanApp() {
        Context context = config.getContext();
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo("com.alicloud.databox", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public OkHttpClient getOkHttpInstance() {
        return okHttpInstance;
    }

//    public void buildDownload(String driveId, String fileId, Consumer<BaseTask> onSuccess,
//                            Consumer<Exception> onFailure) {
//        buildDownload(driveId, fileId, null, onSuccess, onFailure);
//    }
//
//    public void buildDownload(String driveId, String fileId, Integer expireSec,
//                            Consumer<BaseTask> onSuccess, Consumer<Exception> onFailure) {
//        if (downloader == null) {
//            onFailure.accept(AliyunpanException.ErrorCodes.buildError(
//                AliyunpanException.ErrorCodes.CODE_DOWNLOAD_ERROR,
//                "downloader is null, must be config download folder"
//            ));
//            return;
//        }
//
//        if (expireSec != null && expireSec <= 0) {
//            onFailure.accept(AliyunpanException.ErrorCodes.buildError(
//                AliyunpanException.ErrorCodes.CODE_DOWNLOAD_ERROR,
//                "expireSec must be more than 0"
//            ));
//            return;
//        }
//        downloader.buildDownload(driveId, fileId, expireSec, onSuccess, onFailure);
//    }
//
//    public void buildUpload(String driveId, String loadFilePath,
//                          Consumer<BaseTask> onSuccess, Consumer<Exception> onFailure) {
//        buildUpload(driveId, loadFilePath, null, null, onSuccess, onFailure);
//    }

//    public void buildUpload(String driveId, String loadFilePath,
//                          String parentFileId, String checkNameMode,
//                          Consumer<BaseTask> onSuccess, Consumer<Exception> onFailure) {
//        String actualParentFileId = parentFileId != null ? parentFileId : AliyunpanUploader.DEFAULT_UPLOAD_PARENT_FILE_ID;
//        String actualCheckNameMode = checkNameMode != null ? checkNameMode : AliyunpanUploader.DEFAULT_UPLOAD_CHECK_NAME_MODE;
//
//        uploader.buildUpload(driveId, loadFilePath, actualParentFileId, actualCheckNameMode, onSuccess, onFailure);
//    }
//
//    public void registerReceiver(Activity activity) {
//        AliyunpanBroadcastHelper.registerReceiver(activity);
//    }
//
//    public void unregisterReceiver(Activity activity) {
//        AliyunpanBroadcastHelper.unregisterReceiver(activity);
//    }

//    public void startQRCodeAuth(Consumer<AliyunpanAuthorizeQRCodeStatus> onStateChange) throws Exception {
//        if (qrCodeAuthTask != null) {
//            qrCodeAuthTask.addStateChange(onStateChange);
//            return;
//        }
//
//        List<String> scopes = new ArrayList<>();
//        scopes.add(AliyunpanUserScope.SCOPE_USER_BASE);
//        scopes.add(AliyunpanFileScope.SCOPE_FILE);
//        scopes.add(AliyunpanVideoScope.SCOPE_VIDEO);
//
//        JSONObject qrCodeRequest = config.getCredentials().getOAuthQRCodeRequest(scopes);
//        JSONObject qrCodeResponse = sendSync(new AliyunpanCommand() {
//            @Override
//            public String getUrl() {
//                return "oauth/authorize/qrcode";
//            }
//
//            @Override
//            public JSONObject getBody() {
//                return qrCodeRequest;
//            }
//        });
//
//        qrCodeAuthTask = new AliyunpanQRCodeAuthTask(
//            this,
//            config.getUrlApi(),
//            qrCodeResponse.optString("qrCodeUrl"),
//            qrCodeResponse.optString("sid")
//        );
//        qrCodeAuthTask.addStateChange(onStateChange);
//    }

//    public void stopQRCodeAuth() {
//        if (qrCodeAuthTask != null) {
//            qrCodeAuthTask.cancel();
//            qrCodeAuthTask = null;
//        }
//    }

    public void fetchToken(String authCode, Consumer<Exception> onError) {
        JSONObject tokenRequest = config.getCredentials().getTokenRequest(authCode);
        enqueue(new AliyunpanCommand() {
            @Override
            public String getUrl() {
                return "oauth/access_token";
            }

            @Override
            public JSONObject getBody() {
                return tokenRequest;
            }
        }, response -> {
            try {
                config.getCredentials().updateAccessToken(response.getData().asJSONObject());
                LLogger.log(TAG, "fetchToken getTokenRequest success");
                AliyunpanBroadcastHelper.sentBroadcast(config.getContext(), AliyunpanAction.NOTIFY_LOGIN_SUCCESS);
            } catch (Exception e) {
                LLogger.log(TAG, "fetchToken getTokenRequest failed", e);
                AliyunpanBroadcastHelper.sentBroadcast(config.getContext(), AliyunpanAction.NOTIFY_LOGIN_FAILED,
                    e.getMessage());
            }
        }, onError);
    }

    public ResultResponse send(AliyunpanCommand command) throws Exception {
        Request request = new Request.Builder()
            .url(config.getUrlApi().buildUrl(command.getUrl()))
            .post(buildRequestBody(command.getBody()))
            .build();
        return OKHttpHelper.execute(okHttpInstance, request);
    }

    public void enqueue(
        AliyunpanCommand command,
        Consumer<ResultResponse> onSuccess,
        Consumer<Exception> onFailure
    ) {
        Request request = new Request.Builder()
            .url(config.getUrlApi().buildUrl(command.getUrl()))
            .post(buildRequestBody(command.getBody()))
            .build();
        OKHttpHelper.enqueue(okHttpInstance, request, handler, onSuccess, onFailure);
    }

    private RequestBody buildRequestBody(JSONObject body) {
        return RequestBody.create(
            body.toString(),
            MediaType.parse("application/json")
        );
    }

    public Handler getHandler() {
        return handler;
    }

    public OkHttpClient getHttpClient() {
        return okHttpInstance;
    }

//    public AliyunpanDownloader getDownloader() {
//        return downloader;
//    }
//
//    public AliyunpanUploader getUploader() {
//        return uploader;
//    }

    public AliyunpanClientConfig getConfig() {
        return config;
    }

    public void destroy() {
//        if (qrCodeAuthTask != null) {
//            qrCodeAuthTask.cancel();
//            qrCodeAuthTask = null;
//        }
//
//        if (downloader != null) {
//            downloader.destroy();
//        }
//
//        if (uploader != null) {
//            uploader.destroy();
//        }
    }
}
