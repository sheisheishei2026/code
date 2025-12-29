package com.alicloud.databox.opensdk.scope;

import com.alicloud.databox.opensdk.AliyunpanScope;
import java.util.HashMap;
import java.util.Map;

public interface AliyunpanFileScope {

    /**
     * 获取文件列表
     */
    abstract class GetFileList implements AliyunpanScope {
        private final String driveId;
        private final Integer limit;
        private final String marker;
        private final String orderBy;
        private final String orderDirection;
        private final String parentFileId;
        private final String category;
        private final String type;
        private final Long videoThumbnailTime;
        private final Long videoThumbnailWidth;
        private final Long imageThumbnailWidth;
        private final String fields;

        public GetFileList(String driveId, String parentFileId) {
            this(driveId, null, null, null, null, parentFileId, null, null,
                 null, null, null, null);
        }

        public GetFileList(String driveId, Integer limit, String marker, String orderBy,
                         String orderDirection, String parentFileId, String category,
                         String type, Long videoThumbnailTime, Long videoThumbnailWidth,
                         Long imageThumbnailWidth, String fields) {
            this.driveId = driveId;
            this.limit = limit;
            this.marker = marker;
            this.orderBy = orderBy;
            this.orderDirection = orderDirection;
            this.parentFileId = parentFileId;
            this.category = category;
            this.type = type;
            this.videoThumbnailTime = videoThumbnailTime;
            this.videoThumbnailWidth = videoThumbnailWidth;
            this.imageThumbnailWidth = imageThumbnailWidth;
            this.fields = fields;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/list";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("drive_id", driveId);
            map.put("limit", limit);
            map.put("marker", marker);
            map.put("order_by", orderBy);
            map.put("order_direction", orderDirection);
            map.put("parent_file_id", parentFileId);
            map.put("category", category);
            map.put("type", type);
            map.put("video_thumbnail_time", videoThumbnailTime);
            map.put("video_thumbnail_width", videoThumbnailWidth);
            map.put("image_thumbnail_width", imageThumbnailWidth);
            map.put("fields", fields);
            return map;
        }
    }

    /**
     * 文件搜索
     */
    abstract class SearchFile implements AliyunpanScope {
        private final String driveId;
        private final Integer limit;
        private final String marker;
        private final String query;
        private final String orderBy;
        private final Long videoThumbnailTime;
        private final Long videoThumbnailWidth;
        private final Long imageThumbnailWidth;
        private final Boolean returnTotalCount;

        public SearchFile(String driveId, String query) {
            this(driveId, null, null, query, null, null, null, null, null);
        }

        public SearchFile(String driveId, Integer limit, String marker, String query,
                        String orderBy, Long videoThumbnailTime, Long videoThumbnailWidth,
                        Long imageThumbnailWidth, Boolean returnTotalCount) {
            this.driveId = driveId;
            this.limit = limit;
            this.marker = marker;
            this.query = query;
            this.orderBy = orderBy;
            this.videoThumbnailTime = videoThumbnailTime;
            this.videoThumbnailWidth = videoThumbnailWidth;
            this.imageThumbnailWidth = imageThumbnailWidth;
            this.returnTotalCount = returnTotalCount;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/search";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("drive_id", driveId);
            map.put("limit", limit);
            map.put("marker", marker);
            map.put("query", query);
            map.put("order_by", orderBy);
            map.put("video_thumbnail_time", videoThumbnailTime);
            map.put("video_thumbnail_width", videoThumbnailWidth);
            map.put("image_thumbnail_width", imageThumbnailWidth);
            map.put("return_total_count", returnTotalCount);
            return map;
        }
    }

    /**
     * 获取收藏文件列表
     */
    abstract class GetStarredList implements AliyunpanScope {
        private final String driveId;
        private final Integer limit;
        private final String marker;
        private final String orderBy;
        private final Long videoThumbnailTime;
        private final Long videoThumbnailWidth;
        private final Long imageThumbnailWidth;
        private final String orderDirection;
        private final String type;

        public GetStarredList(String driveId) {
            this(driveId, null, null, null, null, null, null, null, null);
        }

        public GetStarredList(String driveId, Integer limit, String marker, String orderBy,
                            Long videoThumbnailTime, Long videoThumbnailWidth,
                            Long imageThumbnailWidth, String orderDirection, String type) {
            this.driveId = driveId;
            this.limit = limit;
            this.marker = marker;
            this.orderBy = orderBy;
            this.videoThumbnailTime = videoThumbnailTime;
            this.videoThumbnailWidth = videoThumbnailWidth;
            this.imageThumbnailWidth = imageThumbnailWidth;
            this.orderDirection = orderDirection;
            this.type = type;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/starredList";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("drive_id", driveId);
            map.put("limit", limit);
            map.put("marker", marker);
            map.put("order_by", orderBy);
            map.put("video_thumbnail_time", videoThumbnailTime);
            map.put("video_thumbnail_width", videoThumbnailWidth);
            map.put("image_thumbnail_width", imageThumbnailWidth);
            map.put("order_direction", orderDirection);
            map.put("type", type);
            return map;
        }
    }

    /**
     * 获取文件信息
     */
    abstract class GetFile implements AliyunpanScope {
        private final String driveId;
        private final String fileId;
        private final Long videoThumbnailTime;
        private final Long videoThumbnailWidth;
        private final Long imageThumbnailWidth;
        private final String fields;

        public GetFile(String driveId, String fileId) {
            this(driveId, fileId, null, null, null, null);
        }

        public GetFile(String driveId, String fileId, Long videoThumbnailTime,
                      Long videoThumbnailWidth, Long imageThumbnailWidth, String fields) {
            this.driveId = driveId;
            this.fileId = fileId;
            this.videoThumbnailTime = videoThumbnailTime;
            this.videoThumbnailWidth = videoThumbnailWidth;
            this.imageThumbnailWidth = imageThumbnailWidth;
            this.fields = fields;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/get";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("drive_id", driveId);
            map.put("file_id", fileId);
            map.put("video_thumbnail_time", videoThumbnailTime);
            map.put("video_thumbnail_width", videoThumbnailWidth);
            map.put("image_thumbnail_width", imageThumbnailWidth);
            map.put("fields", fields);
            return map;
        }
    }

    /**
     * 创建文件夹
     */
    abstract class CreateFolder implements AliyunpanScope {
        private final String driveId;
        private final String parentFileId;
        private final String name;
        private final String checkNameMode;

        public CreateFolder(String driveId, String parentFileId, String name) {
            this(driveId, parentFileId, name, null);
        }

        public CreateFolder(String driveId, String parentFileId, String name, String checkNameMode) {
            this.driveId = driveId;
            this.parentFileId = parentFileId;
            this.name = name;
            this.checkNameMode = checkNameMode;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/createFolder";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("drive_id", driveId);
            map.put("parent_file_id", parentFileId);
            map.put("name", name);
            map.put("check_name_mode", checkNameMode);
            return map;
        }
    }

    /**
     * 更新文件
     */
    abstract class UpdateFile implements AliyunpanScope {
        private final String driveId;
        private final String fileId;
        private final String name;
        private final String checkNameMode;
        private final Boolean starred;
        private final String customIndexKey;
        private final String description;

        public UpdateFile(String driveId, String fileId) {
            this(driveId, fileId, null, null, null, null, null);
        }

        public UpdateFile(String driveId, String fileId, String name, String checkNameMode,
                        Boolean starred, String customIndexKey, String description) {
            this.driveId = driveId;
            this.fileId = fileId;
            this.name = name;
            this.checkNameMode = checkNameMode;
            this.starred = starred;
            this.customIndexKey = customIndexKey;
            this.description = description;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/update";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("drive_id", driveId);
            map.put("file_id", fileId);
            map.put("name", name);
            map.put("check_name_mode", checkNameMode);
            map.put("starred", starred);
            map.put("custom_index_key", customIndexKey);
            map.put("description", description);
            return map;
        }
    }

    /**
     * 移动文件或文件夹
     */
    abstract class MoveFile implements AliyunpanScope {
        private final String driveId;
        private final String fileId;
        private final String toDriveId;
        private final String toParentFileId;
        private final String checkNameMode;
        private final String newName;

        public MoveFile(String driveId, String fileId, String toParentFileId) {
            this(driveId, fileId, null, toParentFileId, null, null);
        }

        public MoveFile(String driveId, String fileId, String toDriveId,
                       String toParentFileId, String checkNameMode, String newName) {
            this.driveId = driveId;
            this.fileId = fileId;
            this.toDriveId = toDriveId;
            this.toParentFileId = toParentFileId;
            this.checkNameMode = checkNameMode;
            this.newName = newName;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/move";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("drive_id", driveId);
            map.put("file_id", fileId);
            map.put("to_drive_id", toDriveId);
            map.put("to_parent_file_id", toParentFileId);
            map.put("check_name_mode", checkNameMode);
            map.put("new_name", newName);
            return map;
        }
    }


    /**
     * 获取异步任务状态
     */
    abstract class GetAsyncTask implements AliyunpanScope {
        private final String asyncTaskId;

        public GetAsyncTask(String asyncTaskId) {
            this.asyncTaskId = asyncTaskId;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/async_task/get";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("async_task_id", asyncTaskId);
            return map;
        }
    }
}
