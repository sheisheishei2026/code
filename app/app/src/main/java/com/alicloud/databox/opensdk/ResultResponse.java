package com.alicloud.databox.opensdk;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultResponse {
    private final int code;
    private final Data data;

    public ResultResponse(int code, Data data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ResultResponse(code=" + code + ", data=" + data + ")";
    }

    public static class Data {
        private final byte[] rawData;

        public Data(byte[] rawData) {
            this.rawData = rawData;
        }

        public byte[] getRawBytes() {
            return rawData;
        }

        public String asString() {
            return new String(rawData);
        }

        public JSONObject asJSONObject() {
            try {
                return new JSONObject(asString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public String toString() {
            return asString();
        }
    }
}
