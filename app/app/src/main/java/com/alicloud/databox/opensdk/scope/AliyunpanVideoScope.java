package com.alicloud.databox.opensdk.scope;

import com.alicloud.databox.opensdk.AliyunpanScope;
import java.util.HashMap;
import java.util.Map;

public interface AliyunpanVideoScope {

    /**
     * 获取文件播放详情
     */
    abstract class GetVideoPreviewPlayInfo implements AliyunpanScope {
        private final String driveId;
        private final String fileId;
        private final String category;
        private final Boolean getSubtitleInfo;
        private final String templateId;
        private final Integer urlExpireSec;
        private final Boolean onlyVip;
        private final Boolean withPlayCursor;

        public GetVideoPreviewPlayInfo(String driveId, String fileId) {
            this(driveId, fileId, null, null, null, null, null, null);
        }

        public GetVideoPreviewPlayInfo(String driveId, String fileId, String category,
                                     Boolean getSubtitleInfo, String templateId,
                                     Integer urlExpireSec, Boolean onlyVip,
                                     Boolean withPlayCursor) {
            this.driveId = driveId;
            this.fileId = fileId;
            this.category = category;
            this.getSubtitleInfo = getSubtitleInfo;
            this.templateId = templateId;
            this.urlExpireSec = urlExpireSec;
            this.onlyVip = onlyVip;
            this.withPlayCursor = withPlayCursor;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/getVideoPreviewPlayInfo";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("drive_id", driveId);
            map.put("file_id", fileId);
            map.put("category", category);
            map.put("get_subtitle_info", getSubtitleInfo);
            map.put("template_id", templateId);
            map.put("url_expire_sec", urlExpireSec);
            map.put("only_vip", onlyVip);
            map.put("with_play_cursor", withPlayCursor);
            return map;
        }
    }

    /**
     * 获取文件播放元数据
     */
    abstract class GetVideoPreviewPlayMeta implements AliyunpanScope {
        private final String driveId;
        private final String fileId;
        private final String category;
        private final String templateId;

        public GetVideoPreviewPlayMeta(String driveId, String fileId) {
            this(driveId, fileId, null, null);
        }

        public GetVideoPreviewPlayMeta(String driveId, String fileId, String category, String templateId) {
            this.driveId = driveId;
            this.fileId = fileId;
            this.category = category;
            this.templateId = templateId;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/getVideoPreviewPlayMeta";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("drive_id", driveId);
            map.put("file_id", fileId);
            map.put("category", category);
            map.put("template_id", templateId);
            return map;
        }
    }

    /**
     * 更新播放进度
     */
    abstract class UpdateVideoRecord implements AliyunpanScope {
        private final String driveId;
        private final String fileId;
        private final String playCursor;
        private final String duration;

        public UpdateVideoRecord(String driveId, String fileId, String playCursor) {
            this(driveId, fileId, playCursor, null);
        }

        public UpdateVideoRecord(String driveId, String fileId, String playCursor, String duration) {
            this.driveId = driveId;
            this.fileId = fileId;
            this.playCursor = playCursor;
            this.duration = duration;
        }

        @Override
        public String getHttpMethod() {
            return "POST";
        }

        @Override
        public String getApi() {
            return "adrive/v1.0/openFile/video/updateRecord";
        }

        @Override
        public Map<String, Object> getRequest() {
            Map<String, Object> map = new HashMap<>();
            map.put("drive_id", driveId);
            map.put("file_id", fileId);
            map.put("play_cursor", playCursor);
            map.put("duration", duration);
            return map;
        }
    }

}
