package com.alicloud.databox.opensdk.io;

import android.os.Handler;
import com.alicloud.databox.opensdk.AliyunpanClient;
import com.alicloud.databox.opensdk.Consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AliyunpanIO<T extends BaseTask> {
    protected static final String SUPPORT_FILE_TYPE = "file";
    protected static final long MAX_CHUNK_SIZE = 2 * 1024 * 1024L;

    protected final AliyunpanClient client;
    protected final Handler handler;
    protected final ConcurrentHashMap<T, Future<?>> runningTaskMap;

    protected AliyunpanIO(AliyunpanClient client) {
        this.client = client;
        this.handler = client.getHandler();
        this.runningTaskMap = new ConcurrentHashMap<>();
    }

    protected ThreadPoolExecutor buildThreadGroupExecutor(int maximumPoolSize, String threadNamePrefix) {
        return new ThreadPoolExecutor(
            maximumPoolSize,
            maximumPoolSize,
            0,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactory() {
                private final ThreadGroup group = Thread.currentThread().getThreadGroup();
                private final AtomicInteger poolNumber = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(group, r, threadNamePrefix + "-" + poolNumber.getAndIncrement(), 0);
                }
            }
        );
    }

    protected void postSuccess(Consumer<BaseTask> onSuccess, T task) {
        handler.post(() -> onSuccess.accept(task));
    }

    protected void postFailure(Consumer<Exception> onFailure, Exception t) {
        handler.post(() -> onFailure.accept(t));
    }

    protected void postProgress(BaseTask task, long completedSize, long totalSize) {
        handler.post(() -> {
            for (Consumer<BaseTask.TaskState> consumer : task.stateChangeList) {
                consumer.accept(new BaseTask.TaskState.Running(completedSize, totalSize));
            }
        });
    }

    protected void postCompleted(BaseTask task, String path) {
        handler.post(() -> {
            for (Consumer<BaseTask.TaskState> consumer : task.stateChangeList) {
                consumer.accept(new BaseTask.TaskState.Completed(path));
            }
            runningTaskMap.remove(task);
        });
    }

    protected void postFailed(BaseTask task, Exception e) {
        handler.post(() -> {
            for (Consumer<BaseTask.TaskState> consumer : task.stateChangeList) {
                consumer.accept(new BaseTask.TaskState.Failed(e));
            }
            runningTaskMap.remove(task);
        });
    }

    protected void postAbort(BaseTask task) {
        handler.post(() -> {
            for (Consumer<BaseTask.TaskState> consumer : task.stateChangeList) {
                consumer.accept(BaseTask.TaskState.Abort.INSTANCE);
            }
            runningTaskMap.remove(task);
        });
    }

    public static class TaskChunk {
        private final int chunkIndex;
        private final long chunkStart;
        private final long chunkSize;

        public TaskChunk(int chunkIndex, long chunkStart, long chunkSize) {
            this.chunkIndex = chunkIndex;
            this.chunkStart = chunkStart;
            this.chunkSize = chunkSize;
        }

        public int getChunkIndex() {
            return chunkIndex;
        }

        public long getChunkStart() {
            return chunkStart;
        }

        public long getChunkSize() {
            return chunkSize;
        }
    }

    protected static List<TaskChunk> buildChunkList(long size, int maxChunkSize) {
        if (size <= MAX_CHUNK_SIZE) {
            return Collections.singletonList(new TaskChunk(0, 0, size));
        }

        List<TaskChunk> taskChunks = new ArrayList<>();
        long fixedChunkSize = Math.max(MAX_CHUNK_SIZE, size / maxChunkSize);
        int index = (int) (size / fixedChunkSize);
        long remainChunkSize = size % fixedChunkSize;

        for (int i = 0; i < index; i++) {
            boolean isLast = i == (index - 1);
            long chunkSize = isLast ? fixedChunkSize + remainChunkSize : fixedChunkSize;
            taskChunks.add(new TaskChunk(i, i * fixedChunkSize, chunkSize));
        }

        return taskChunks;
    }
} 