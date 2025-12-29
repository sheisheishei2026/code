package com.alicloud.databox.opensdk.io;

import com.alicloud.databox.opensdk.Consumer;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseTask {
    private final String driveId;
    private final String fileId;
    private final String fileParentFileId;
    private final AtomicBoolean isCancel;
    protected final CopyOnWriteArrayList<Consumer<TaskState>> stateChangeList;

    protected BaseTask(String driveId, String fileId, String fileParentFileId) {
        this.driveId = driveId;
        this.fileId = fileId;
        this.fileParentFileId = fileParentFileId;
        this.isCancel = new AtomicBoolean(false);
        this.stateChangeList = new CopyOnWriteArrayList<>();
    }

    public abstract String getTaskName();

    /**
     * Start
     *
     * @return 正在运行中返回false 否则返回true
     */
    public abstract boolean start();

    /**
     * Cancel
     * 取消任务
     */
    public void cancel() {
        isCancel.set(true);
    }

    public boolean isCancel() {
        return isCancel.get();
    }

    public void addStateChange(Consumer<TaskState> onChange) {
        stateChangeList.add(onChange);
    }

    public void removeStateChange(Consumer<TaskState> onChange) {
        stateChangeList.remove(onChange);
    }

    public String getDriveId() {
        return driveId;
    }

    public String getFileId() {
        return fileId;
    }

    /**
     * Task state
     * 密封类封装各运行状态 和关联数据
     */
    public static abstract class TaskState {
        private TaskState() {}

        /**
         * 等待
         */
        public static final class Waiting extends TaskState {
            public static final Waiting INSTANCE = new Waiting();
            private Waiting() {}
        }

        /**
         * 正在运行
         */
        public static final class Running extends TaskState {
            private final long completedSize;
            private final long totalSize;

            public Running(long completedSize, long totalSize) {
                this.completedSize = completedSize;
                this.totalSize = totalSize;
            }

            /**
             * Get progress
             *
             * @return 返回值范围 带小数点 0.0..1.0
             */
            public float getProgress() {
                return (float) completedSize / totalSize;
            }

            public long getCompletedSize() {
                return completedSize;
            }

            public long getTotalSize() {
                return totalSize;
            }
        }

        /**
         * 已完成
         */
        public static final class Completed extends TaskState {
            private final String filePath;

            public Completed(String filePath) {
                this.filePath = filePath;
            }

            public String getFilePath() {
                return filePath;
            }
        }

        /**
         * 失败
         */
        public static final class Failed extends TaskState {
            private final Exception exception;

            public Failed(Exception exception) {
                this.exception = exception;
            }

            public Exception getException() {
                return exception;
            }
        }

        /**
         * 取消
         */
        public static final class Abort extends TaskState {
            public static final Abort INSTANCE = new Abort();
            private Abort() {}
        }
    }
} 