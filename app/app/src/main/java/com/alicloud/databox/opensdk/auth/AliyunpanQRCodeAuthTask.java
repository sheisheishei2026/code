package com.alicloud.databox.opensdk.auth;

import android.os.Handler;
import com.alicloud.databox.opensdk.AliyunpanClient;
import com.alicloud.databox.opensdk.AliyunpanUrlApi;
import com.alicloud.databox.opensdk.Consumer;
import com.alicloud.databox.opensdk.ResultResponse;
import okhttp3.Request;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AliyunpanQRCodeAuthTask {
    private static final long REQUEST_GAP_TIME = 1500L;

    private final AliyunpanClient client;
    private final AliyunpanUrlApi urlApi;
    private final String qrcodeUrl;
    private final String sid;
    private AliyunpanAuthorizeQRCodeStatus currentStatus;
    private final Handler handler;
    private final ExecutorService loopExecutor;
    private final List<Consumer<AliyunpanAuthorizeQRCodeStatus>> stateChangeList;

    public AliyunpanQRCodeAuthTask(AliyunpanClient client, AliyunpanUrlApi urlApi, 
                                  String qrcodeUrl, String sid) {
        this.client = client;
        this.urlApi = urlApi;
        this.qrcodeUrl = qrcodeUrl;
        this.sid = sid;
        this.handler = client.getHandler();
        this.loopExecutor = Executors.newSingleThreadExecutor();
        this.stateChangeList = new ArrayList<>();
        
        loopExecutor.submit(new LoopFetchAuth());
    }

    private class LoopFetchAuth implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(REQUEST_GAP_TIME);

                Request request = new Request.Builder()
                    .url(urlApi.buildUrl("oauth/qrcode/" + sid + "/status"))
                    .build();
                ResultResponse response = client.sendSync(request);
                JSONObject jsonObject = response.getData().asJSONObject();
                String status = jsonObject.optString("status");
                String authCode = jsonObject.optString("authCode");

                AliyunpanAuthorizeQRCodeStatus authorizeQRCodeStatus = null;
                for (AliyunpanAuthorizeQRCodeStatus value : AliyunpanAuthorizeQRCodeStatus.values()) {
                    if (value.getStateName().equals(status)) {
                        authorizeQRCodeStatus = value;
                        break;
                    }
                }
                if (authorizeQRCodeStatus == null) {
                    authorizeQRCodeStatus = AliyunpanAuthorizeQRCodeStatus.WAIT_LOGIN;
                }

                postState(authorizeQRCodeStatus);

                if (authorizeQRCodeStatus == AliyunpanAuthorizeQRCodeStatus.LOGIN_SUCCESS) {
                    client.fetchToken(authCode, null);
                    loopExecutor.shutdown();
                    return;
                }

                if (authorizeQRCodeStatus == AliyunpanAuthorizeQRCodeStatus.QRCODE_EXPIRED) {
                    loopExecutor.shutdown();
                    return;
                }

                loopExecutor.submit(this);
            } catch (Exception e) {
                // Handle exception
            }
        }
    }

    private void postState(final AliyunpanAuthorizeQRCodeStatus authorizeQRCodeStatus) {
        handler.post(() -> {
            if (currentStatus != authorizeQRCodeStatus) {
                for (Consumer<AliyunpanAuthorizeQRCodeStatus> consumer : stateChangeList) {
                    consumer.accept(authorizeQRCodeStatus);
                }
                currentStatus = authorizeQRCodeStatus;
            }
        });
    }

    public String getQRCodeUrl() {
        return qrcodeUrl;
    }

    public void addStateChange(Consumer<AliyunpanAuthorizeQRCodeStatus> onChange) {
        stateChangeList.add(onChange);
    }

    public void removeStateChange(Consumer<AliyunpanAuthorizeQRCodeStatus> onChange) {
        stateChangeList.remove(onChange);
    }
} 