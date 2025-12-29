package x.shei.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import x.shei.db.PlaceItem;

public class PlaceDataLoader {
    private static final String TAG = "PlaceDataLoader";
    private static Map<Integer, PlaceItem> placeItemMap = new HashMap<>();
    private static boolean isInitialized = false;

    public static void initialize(Context context) {
        if (isInitialized) return;

        try {
            String json = loadJSONFromAsset(context, "places.json");
            Gson gson = new Gson();

            // 首先解析整个JSON对象
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            // 然后从items数组中获取景点列表
            Type type = new TypeToken<List<PlaceItem>>(){}.getType();
            List<PlaceItem> placeItems = gson.fromJson(jsonObject.getAsJsonArray("items"), type);

            // 将景点数据存入Map中，以ID为键
            for (PlaceItem item : placeItems) {
                try {
                    int id = Integer.parseInt(item.id);
                    placeItemMap.put(id, item);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid ID format: " + item.id);
                }
            }

            isInitialized = true;
            Log.d(TAG, "Successfully loaded " + placeItemMap.size() + " places");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing PlaceDataLoader", e);
        }
    }

    public static List<PlaceItem> getPlaceItems(List<Integer> navIds) {
        List<PlaceItem> items = new ArrayList<>();
        for (Integer id : navIds) {
            PlaceItem item = placeItemMap.get(id);
            if (item != null) {
                items.add(item);
            } else {
                Log.w(TAG, "Place not found for ID: " + id);
            }
        }
        return items;
    }

    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e(TAG, "Error loading JSON file: " + fileName, ex);
            return null;
        }
        return json;
    }
}
