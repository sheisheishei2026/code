package x.shei.todo;

import android.os.Parcel;
import android.os.Parcelable;


//@Parcelable
public class NotifyTODO implements Parcelable {

    String id;
    String item;

    public NotifyTODO () {
    }

    public NotifyTODO (String item, String id) {
        this.item = item;
        this.id = id;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }


    public String getItem () {
        return item;
    }

    public void setItem (String item) {
        this.item = item;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.item);
    }

    protected NotifyTODO(Parcel in) {
        this.id = in.readString();
        this.item = in.readString();
    }

    public static final Creator<NotifyTODO> CREATOR = new Creator<NotifyTODO>() {
        public NotifyTODO createFromParcel(Parcel source) {
            return new NotifyTODO(source);
        }

        public NotifyTODO[] newArray(int size) {
            return new NotifyTODO[size];
        }
    };
}
