package x.shei.activity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.File;

public class MediaItem implements Parcelable {
    public File file;
    public Uri uri;
    public String name;
    public boolean isVideo;

    public MediaItem(File file) {
        this.file = file;
        this.uri = null;
        this.name = file != null ? file.getName() : null;
        this.isVideo = isVideoFile(this.name);
    }

    public MediaItem(Uri uri, String name) {
        this.file = null;
        this.uri = uri;
        // 如果name是完整路径，提取文件名；否则直接使用name
        if (name != null && name.contains("/")) {
            this.name = name.substring(name.lastIndexOf("/") + 1);
        } else {
            this.name = name != null ? name : (uri != null ? uri.getLastPathSegment() : null);
        }
        this.isVideo = isVideoFile(this.name);
    }

    public Object getSource() {
        if (file != null) {
            return file;
        }
        return uri;
    }

    public String getPath() {
        if (file != null) {
            return file.getAbsolutePath();
        }
        if (uri != null) {
            return uri.toString();
        }
        return null;
    }
    
    public Uri getUri() {
        if (uri != null) {
            return uri;
        }
        if (file != null) {
            return Uri.fromFile(file);
        }
        return null;
    }

    private boolean isVideoFile(String fileName) {
        if (fileName == null) return false;
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".mp4") || lowerName.endsWith(".avi") ||
               lowerName.endsWith(".mov") || lowerName.endsWith(".mkv") ||
               lowerName.endsWith(".3gp") || lowerName.endsWith(".wmv");
    }

    // Parcelable implementation
    protected MediaItem(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        name = in.readString();
        isVideo = in.readByte() != 0;
        String filePath = in.readString();
        if (filePath != null) {
            file = new File(filePath);
        }
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeString(name);
        dest.writeByte((byte) (isVideo ? 1 : 0));
        dest.writeString(file != null ? file.getAbsolutePath() : null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MediaItem mediaItem = (MediaItem) obj;
        if (uri != null && mediaItem.uri != null) {
            return uri.equals(mediaItem.uri);
        }
        if (file != null && mediaItem.file != null) {
            return file.equals(mediaItem.file);
        }
        return false;
    }
}

