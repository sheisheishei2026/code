package x.shei.db;

import java.io.Serializable;
import java.util.List;

public class PlaceItem implements Serializable {
        public String id;
    public String title;
    public String longitude;
    public String latitude;
    public String price;
    public String rate;
    public String officalId;
    public String type;
    public String appId;
    public String recomend;
    public String voice;
    public String time;
    public String go;
    public String img;
    public String camera;
    public String video;
    public String pricenum;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getOfficalId() {
        return officalId;
    }

    public void setOfficalId(String officalId) {
        this.officalId = officalId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getRecomend() {
        return recomend;
    }

    public void setRecomend(String recomend) {
        this.recomend = recomend;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGo() {
        return go;
    }

    public void setGo(String go) {
        this.go = go;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public String getVideoUrl() {
        return video;
    }

    public void setVideoUrl(String videoUrl) {
        this.video = videoUrl;
    }

}
