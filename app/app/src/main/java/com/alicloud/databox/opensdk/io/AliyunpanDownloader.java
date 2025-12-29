package com.alicloud.databox.opensdk.io;

import android.os.Handler;
import com.alicloud.databox.opensdk.AliyunpanClient;
import com.alicloud.databox.opensdk.AliyunpanClientConfig;
import com.alicloud.databox.opensdk.AliyunpanException;
import com.alicloud.databox.opensdk.Consumer;
import com.alicloud.databox.opensdk.LLogger;
import com.alicloud.databox.opensdk.http.AliyunpanExceedMaxConcurrencyException;
import com.alicloud.databox.opensdk.http.AliyunpanUrlExpiredException;
import com.alicloud.databox.opensdk.http.OKHttpHelper;
import com.alicloud.databox.opensdk.scope.AliyunpanFileScope;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AliyunpanDownloader extends AliyunpanIO<AliyunpanDownloader.DownloadTask> {
    private static final String TAG = "AliyunpanDownloader";
    private static final int MAX_CHUNK_COUNT = 10000;
    private static final int DEFAULT_DOWNLOAD_TASK_SIZE = 2;

    private final String downloadFolderPath;
    private final ThreadPoolExecutor downloadExecutor;

    public AliyunpanDownloader(AliyunpanClient client, String downloadFolderPath) {
        super(client);
        this.downloadFolderPath = downloadFolderPath;
        this.downloadExecutor = buildThreadGroupExecutor(DEFAULT_DOWNLOAD_TASK_SIZE, "download");
    }

    public DownloadTask buildTask(String driveId, String fileId, String fileName) {
        return new DownloadTask(driveId, fileId, fileName);
    }

    public class DownloadTask extends BaseTask {
        private final String fileName;
        private String downloadUrl;
        private Long expireSeconds;
        private final List<TaskChunk> taskChunks = new ArrayList<>();
        private final ConcurrentHashMap<Integer, Long> chunkCompletedSizeMap = new ConcurrentHashMap<>();
        private long totalSize;
        private File downloadFile;
        private volatile Exception error;
        private CountDownLatch countDownLatch;

        private DownloadTask(String driveId, String fileId, String fileName) {
            super(driveId, fileId, null);
            this.fileName = fileName;
        }

        @Override
        public String getTaskName() {
            return fileName;
        }

        @Override
        public boolean start() {
            if (runningTaskMap.containsKey(this)) {
                return false;
            }

            for (Consumer<TaskState> consumer : stateChangeList) {
                consumer.accept(TaskState.Waiting.INSTANCE);
            }

            Future<?> future = downloadExecutor.submit(new DownloadRunnable(this));
            runningTaskMap.put(this, future);
            return true;
        }

        private String getValidDownloadUrl() {
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                return null;
            }

            if (expireSeconds != null && (System.currentTimeMillis() / 1000) > expireSeconds) {
                return null;
            }

            return downloadUrl;
        }
    }

    private class DownloadRunnable implements Runnable {
        private final DownloadTask task;

        private DownloadRunnable(DownloadTask task) {
            this.task = task;
            this.task.countDownLatch = new CountDownLatch(1);
        }

        @Override
        public void run() {
            try {
                // Get file info
//                JSONObject fileInfo = client.send(new AliyunpanFileScope.GetFile(task.getDriveId(), task.getFileId()));
//                String type = fileInfo.optString("type");
//                if (!SUPPORT_FILE_TYPE.equals(type)) {
//                    throw AliyunpanException.buildError(AliyunpanException.CODE_DOWNLOAD_ERROR, "not support download type");
//                }
//
//                task.totalSize = fileInfo.optLong("size");
//                String downloadUrl = fileInfo.optString("download_url");
//                Long expireSeconds = fileInfo.optLong("download_url_expire_sec");
//
//                task.downloadUrl = downloadUrl;
//                task.expireSeconds = expireSeconds;
//
//                // Create download file
//                File downloadFolder = new File(downloadFolderPath);
//                if (!downloadFolder.exists() && !downloadFolder.mkdirs()) {
//                    throw AliyunpanException.buildError(AliyunpanException.CODE_DOWNLOAD_ERROR, "create download folder failed");
//                }
//
//                task.downloadFile = new File(downloadFolder, task.fileName);
//
//                // Build chunks
//                task.taskChunks.clear();
//                task.taskChunks.addAll(buildChunkList(task.totalSize, MAX_CHUNK_COUNT));

                // Start download chunks
//                ThreadPoolExecutor chunkExecutor = buildThreadGroupExecutor(
//                    AliyunpanClientConfig.DEFAULT_MAX_DOWNLOAD_CHUNK_TASK_SIZE,
//                    "download-chunk"
//                );

//                for (TaskChunk chunk : task.taskChunks) {
//                    if (task.isCancel()) {
//                        break;
//                    }
//                    chunkExecutor.execute(new DownloadChunkRunnable(task, chunk));
//                }
//
//                task.countDownLatch.await();
//                chunkExecutor.shutdown();

                if (task.error != null) {
                    throw task.error;
                }

                if (task.isCancel()) {
                    postAbort(task);
                    return;
                }

                postCompleted(task, task.downloadFile.getAbsolutePath());

            } catch (Exception e) {
//                LLogger.e(TAG, "download error", e);
                if (e instanceof AliyunpanUrlExpiredException || e instanceof AliyunpanExceedMaxConcurrencyException) {
                    postFailed(task, e);
                } else {
                    postFailed(task, AliyunpanException.buildError(AliyunpanException.CODE_DOWNLOAD_ERROR, e.getMessage()));
                }
            }
        }
    }

    private class DownloadChunkRunnable implements Runnable {
        private final DownloadTask task;
        private final TaskChunk chunk;

        private DownloadChunkRunnable(DownloadTask task, TaskChunk chunk) {
            this.task = task;
            this.chunk = chunk;
        }

        @Override
        public void run() {
            try {
                String downloadUrl = task.getValidDownloadUrl();
                if (downloadUrl == null) {
                    throw new AliyunpanUrlExpiredException(
                        AliyunpanException.CODE_DOWNLOAD_ERROR,
                        "download url expired"
                    );
                }

                OKHttpHelper.download(
                    client.getHttpClient(),
                    downloadUrl,
                    chunk.getChunkStart(),
                    chunk.getChunkStart() + chunk.getChunkSize() - 1,
                    task.downloadFile
                );

                task.chunkCompletedSizeMap.put(chunk.getChunkIndex(), chunk.getChunkSize());
                long completedSize = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    completedSize = task.chunkCompletedSizeMap.values().stream()
                        .mapToLong(Long::longValue)
                        .sum();
                }
                postProgress(task, completedSize, task.totalSize);

            } catch (Exception e) {
//                LLogger.e(TAG, "download chunk error", e);
                task.cancel();
                task.error = e;
                task.countDownLatch.countDown();
            }
        }
    }
}
