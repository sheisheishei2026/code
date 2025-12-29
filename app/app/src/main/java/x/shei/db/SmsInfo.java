package x.shei.db;

public class SmsInfo {
    private String id;        // 短信ID，用于删除
    private String address;  // 发送者号码
    private String body;      // 短信内容
    private long date;        // 时间戳
    private int type;         // 类型：1-接收，2-发送

    public SmsInfo(String id, String address, String body, long date, int type) {
        this.id = id;
        this.address = address;
        this.body = body;
        this.date = date;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

