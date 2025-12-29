//package com.alicloud.databox.opensdk.io;
//
//import android.util.Base64;
//import com.alicloud.databox.opensdk.AliyunpanClient;
//import com.alicloud.databox.opensdk.AliyunpanException;
//import com.alicloud.databox.opensdk.Consumer;
//import com.alicloud.databox.opensdk.LLogger;
//import com.alicloud.databox.opensdk.http.OKHttpHelper;
//import com.alicloud.databox.opensdk.scope.AliyunpanFileScope;
//import org.json.JSONObject;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.*;
//
//public class AliyunpanUploader extends AliyunpanIO<AliyunpanUploader.UploadTask> {
//    private static final String TAG = "AliyunpanUploader";
//    private static final int MAX_CHUNK_COUNT = 1000;
//    private static final long PRE_HASH_SIZE_THRESHOLD = 500 * 1024L;
//    private static final String CODE_PRE_HASH_MATCHED = "PreHashMatched";
//    private static final String DEFAULT_UPLOAD_HASH_NAME = "sha1";
//    private static final String DEFAULT_UPLOAD_PROOF_VERSION = "v1";
//    private static final int DEFAULT_UPLOAD_TASK_SIZE = 2;
//
//    private final ThreadPoolExecutor uploadExecutor;
//
//    public AliyunpanUploader(AliyunpanClient client) {
//        super(client);
//        this.uploadExecutor = buildThreadGroupExecutor(DEFAULT_UPLOAD_TASK_SIZE, "upload");
//    }
//
//    public UploadTask buildTask(String driveId, String parentFileId, File file) {
//        return new UploadTask(driveId, null, parentFileId, file);
//    }
//
//    public class UploadTask extends BaseTask {
//        private final File uploadFile;
//        private String uploadUrl;
//        private Long expireSeconds;
//        private String uploadId;
//        private final List<TaskChunk> taskChunks = new ArrayList<>();
//        private final ConcurrentHashMap<Integer, String> chunkPartInfoMap = new ConcurrentHashMap<>();
//        private volatile Exception error;
//        private CountDownLatch countDownLatch;
//
//        private UploadTask(String driveId, String fileId, String parentFileId, File file) {
//            super(driveId, fileId, parentFileId);
//            this.uploadFile = file;
//        }
//
//        @Override
//        public String getTaskName() {
//            return uploadFile.getName();
//        }
//
//        @Override
//        public boolean start() {
//            if (runningTaskMap.containsKey(this)) {
//                return false;
//            }
//
//            for (Consumer<TaskState> consumer : stateChangeList) {
//                consumer.accept(TaskState.Waiting.INSTANCE);
//            }
//
//            Future<?> future = uploadExecutor.submit(new UploadRunnable(this));
//            runningTaskMap.put(this, future);
//            return true;
//        }
//
//        private String getValidUploadUrl() {
//            if (uploadUrl == null || uploadUrl.isEmpty()) {
//                return null;
//            }
//
//            if (expireSeconds != null && (System.currentTimeMillis() / 1000) > expireSeconds) {
//                return null;
//            }
//
//            return uploadUrl;
//        }
//    }
//
//    private class UploadRunnable implements Runnable {
//        private final UploadTask task;
//
//        private UploadRunnable(UploadTask task) {
//            this.task = task;
//            this.task.countDownLatch = new CountDownLatch(1);
//        }
//
//        @Override
//        public void run() {
//            try {
//                // Check file exists
//                if (!task.uploadFile.exists()) {
//                    throw AliyunpanException.buildError(AliyunpanException.CODE_UPLOAD_ERROR, "file not exists");
//                }
//
//                // Get file size and hash
//                long fileSize = task.uploadFile.length();
//                String filePath = task.uploadFile.getAbsolutePath();
//                String preHash = null;
//                if (fileSize <= PRE_HASH_SIZE_THRESHOLD) {
//                    preHash = MessageDigestHelper.getFilePreSHA1(filePath);
//                }
//
//                // Create file
//                JSONObject createFileRequest = new JSONObject();
//                createFileRequest.put("drive_id", task.getDriveId());
//                createFileRequest.put("parent_file_id", task.fileParentFileId);
//                createFileRequest.put("name", task.uploadFile.getName());
//                createFileRequest.put("type", SUPPORT_FILE_TYPE);
//                createFileRequest.put("size", fileSize);
//                if (preHash != null) {
//                    createFileRequest.put("pre_hash", preHash);
//                }
//
//                JSONObject createFileResponse = client.send(new AliyunpanFileScope.CreateFile(createFileRequest));
//                String rapidUpload = createFileResponse.optString("rapid_upload");
//                if (CODE_PRE_HASH_MATCHED.equals(rapidUpload)) {
//                    // Need check hash
//                    String hash = MessageDigestHelper.getFileSHA1(filePath);
//                    createFileRequest.put("content_hash", hash);
//                    createFileRequest.put("content_hash_name", DEFAULT_UPLOAD_HASH_NAME);
//                    createFileRequest.put("proof_version", DEFAULT_UPLOAD_PROOF_VERSION);
//                    createFileRequest.put("proof_code", Base64.encodeToString(hash.getBytes(), Base64.NO_WRAP));
//
//                    createFileResponse = client.send(new AliyunpanFileScope.CreateFile(createFileRequest));
//                }
//
//                String uploadId = createFileResponse.optString("upload_id");
//                if (uploadId == null || uploadId.isEmpty()) {
//                    // Rapid upload success
//                    postCompleted(task, createFileResponse.optString("file_id"));
//                    return;
//                }
//
//                task.uploadId = uploadId;
//                task.uploadUrl = createFileResponse.optString("upload_url");
//                task.expireSeconds = createFileResponse.optLong("upload_url_expire_sec");
//
//                // Build chunks
//                task.taskChunks.clear();
//                task.taskChunks.addAll(buildChunkList(fileSize, MAX_CHUNK_COUNT));
//
//                // Start upload chunks
//                ThreadPoolExecutor chunkExecutor = buildThreadGroupExecutor(
//                    AliyunpanClientConfig.DEFAULT_MAX_UPLOAD_CHUNK_TASK_SIZE,
//                    "upload-chunk"
//                );
//
//                for (TaskChunk chunk : task.taskChunks) {
//                    if (task.isCancel()) {
//                        break;
//                    }
//                    chunkExecutor.execute(new UploadChunkRunnable(task, chunk));
//                }
//
//                task.countDownLatch.await();
//                chunkExecutor.shutdown();
//
//                if (task.error != null) {
//                    throw task.error;
//                }
//
//                if (task.isCancel()) {
//                    postAbort(task);
//                    return;
//                }
//
//                // Complete upload
//                JSONObject completeRequest = new JSONObject();
//                completeRequest.put("drive_id", task.getDriveId());
//                completeRequest.put("file_id", createFileResponse.optString("file_id"));
//                completeRequest.put("upload_id", task.uploadId);
//
//                JSONObject completeResponse = client.send(new AliyunpanFileScope.CompleteFile(completeRequest));
//                postCompleted(task, completeResponse.optString("file_id"));
//
//            } catch (Exception e) {
//                LLogger.e(TAG, "upload error", e);
//                postFailed(task, AliyunpanException.buildError(AliyunpanException.CODE_UPLOAD_ERROR, e.getMessage()));
//            }
//        }
//    }
//
//    private class UploadChunkRunnable implements Runnable {
//        private final UploadTask task;
//        private final TaskChunk chunk;
//
//        private UploadChunkRunnable(UploadTask task, TaskChunk chunk) {
//            this.task = task;
//            this.chunk = chunk;
//        }
//
//        @Override
//        public void run() {
//            try {
//                String uploadUrl = task.getValidUploadUrl();
//                if (uploadUrl == null) {
//                    throw AliyunpanException.buildError(AliyunpanException.CODE_UPLOAD_ERROR, "upload url expired");
//                }
//
//                OKHttpHelper.upload(
//                    client.getHttpClient(),
//                    uploadUrl + "&partNumber=" + (chunk.getChunkIndex() + 1),
//                    chunk.getChunkStart(),
//                    chunk.getChunkStart() + chunk.getChunkSize(),
//                    task.uploadFile
//                );
//
//                task.chunkPartInfoMap.put(chunk.getChunkIndex(), String.valueOf(chunk.getChunkIndex() + 1));
//                long completedSize = task.chunkPartInfoMap.size() * chunk.getChunkSize();
//                postProgress(task, completedSize, task.uploadFile.length());
//
//            } catch (Exception e) {
//                LLogger.e(TAG, "upload chunk error", e);
//                task.cancel();
//                task.error = e;
//                task.countDownLatch.countDown();
//            }
//        }
//    }
//}
