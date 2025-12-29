package com.alicloud.databox.opensdk.http;

import android.os.Handler;
import androidx.annotation.WorkerThread;
import com.alicloud.databox.opensdk.AliyunpanException;
import com.alicloud.databox.opensdk.Consumer;
import com.alicloud.databox.opensdk.ResultResponse;
import com.alicloud.databox.opensdk.io.BufferRandomAccessFile;
import okhttp3.*;
import okio.BufferedSink;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class OKHttpHelper {

    public static OkHttpClient buildOKHttpClient(
            TokenAuthenticator.TokenAuthenticatorConfig authenticatorConfig,
            HttpHeaderInterceptor.HttpHeaderConfig httpHeaderConfig) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .authenticator(new TokenAuthenticator(authenticatorConfig))
                .addInterceptor(new HttpHeaderInterceptor(httpHeaderConfig));
        return builder.build();
    }

    public static ResultResponse execute(OkHttpClient client, Request request) throws Exception {
        Response response = client.newCall(request).execute();
        return buildResultResponse(response);
    }

    public static void enqueue(
            OkHttpClient client,
            Request request,
            Handler handler,
            Consumer<ResultResponse> onSuccess,
            Consumer<Exception> onFailure) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> onFailure.accept(e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResultResponse resultResponse = buildResultResponse(response);
                    handler.post(() -> onSuccess.accept(resultResponse));
                } catch (Exception e) {
                    handler.post(() -> onFailure.accept(e));
                }
            }
        });
    }

    private static AliyunpanHttpException buildHttpException(Response response) throws IOException {
        ResponseBody body = response.body();
        String bodyString = body != null ? body.string() : null;
        JSONObject jsonError = null;
        try {
            jsonError = bodyString != null ? new JSONObject(bodyString) : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new AliyunpanHttpException(
                jsonError != null ? jsonError.optString("code") : String.valueOf(response.code()),
                jsonError != null ? jsonError.optString("message") : ""
        );
    }

    public static ResultResponse buildResultResponse(Response response) throws Exception {
        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            byte[] bytes = body != null ? body.bytes() : new byte[0];
            return new ResultResponse(
                    response.code(),
                    new ResultResponse.Data(bytes)
            );
        }
        throw buildHttpException(response);
    }

    @WorkerThread
    public static void download(OkHttpClient client, String url, long start, long end, File file) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Range", "bytes=" + start + "-" + end)
                .build();

        Response response = null;
        InputStream inputStream = null;
        BufferRandomAccessFile randomAccessFile = null;

        try {
            response = client.newCall(request).execute();
            int code = response.code();
            ResponseBody responseBody = response.body();

            if (code == 206 || code == 200) {
                inputStream = responseBody.byteStream();
                randomAccessFile = new BufferRandomAccessFile(file);
                randomAccessFile.seek(start);

                byte[] buff = new byte[1024 * 100];
                int size;
                while ((size = inputStream.read(buff)) != -1) {
                    randomAccessFile.write(buff, 0, size);
                }

                randomAccessFile.flushAndSync();
            } else {
                String errorBodyString = responseBody != null ? responseBody.string() : null;
                if (code == 403 && errorBodyString != null) {
                    if (errorBodyString.contains("Request has expired")) {
                        throw new AliyunpanUrlExpiredException(
                                AliyunpanException.CODE_DOWNLOAD_ERROR,
                                "request has expired"
                        );
                    } else if (errorBodyString.contains("ExceedMaxConcurrency")) {
                        throw new AliyunpanExceedMaxConcurrencyException(
                                AliyunpanException.CODE_DOWNLOAD_ERROR,
                                "exceed max concurrency"
                        );
                    } else {
                        throw AliyunpanException.buildError(AliyunpanException.CODE_DOWNLOAD_ERROR, "forbidden");
                    }
                } else {
                    throw AliyunpanException.buildError(AliyunpanException.CODE_DOWNLOAD_ERROR, "download not success");
                }
            }
        } finally {
            if (randomAccessFile != null) randomAccessFile.close();
            if (response != null) response.close();
            if (inputStream != null) inputStream.close();
        }
    }

    @WorkerThread
    public static void upload(OkHttpClient client, String url, long start, long end, File uploadFile) throws Exception {
        RequestBody requestBody = new RequestBody() {
            private static final int BUFFER_SIZE = 1024;

            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public long contentLength() {
                return end - start;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                RandomAccessFile accessFile = null;
                try {
                    accessFile = new RandomAccessFile(uploadFile, "r");
                    accessFile.seek(start);
                    long completedSize = 0;
                    byte[] buffer = new byte[BUFFER_SIZE];

                    while (completedSize < contentLength()) {
                        if (completedSize + BUFFER_SIZE > contentLength()) {
                            buffer = new byte[(int)(contentLength() - completedSize)];
                        }
                        int size = accessFile.read(buffer);
                        sink.write(buffer, 0, size);
                        completedSize += size;
                    }
                } finally {
                    if (accessFile != null) accessFile.close();
                }
            }
        };

        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            int code = response.code();

            if (code == 200 || code == 409) {
                return;
            }
            if (code == 403) {
                throw AliyunpanException.buildError(AliyunpanException.CODE_UPLOAD_ERROR, "upload url expired");
            }
        } finally {
            if (response != null) response.close();
        }
    }
}
