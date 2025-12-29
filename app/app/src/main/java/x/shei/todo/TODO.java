package x.shei.todo;

import android.os.Parcel;
import android.os.Parcelable;


//@Parcelable
public class TODO implements Parcelable {

    String id;
    String item;
    String time;
    boolean isdone;

    public TODO() {
    }

    public TODO(String item, String time, boolean isdone) {
        this.time = time;
        this.item = item;
        this.isdone = isdone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public boolean isdone() {
        return isdone;
    }

    public void setIsdone(boolean isdone) {
        this.isdone = isdone;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.item);
        dest.writeString(this.time);
        dest.writeByte(isdone ? (byte) 1 : (byte) 0);
    }

    protected TODO(Parcel in) {
        this.id = in.readString();
        this.item = in.readString();
        this.time = in.readString();
        this.isdone = in.readByte() != 0;
    }

    public static final Creator<TODO> CREATOR = new Creator<TODO>() {
        public TODO createFromParcel(Parcel source) {
            return new TODO(source);
        }

        public TODO[] newArray(int size) {
            return new TODO[size];
        }
    };
}
