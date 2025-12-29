package x.shei.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import x.shei.db.Bean;
import x.shei.R;
import x.shei.adapter.VideoAdapter;
import x.shei.db.FavDao;
import x.shei.db.FavDatabase;
import x.shei.db.FavEntity;

public class FavFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private VideoAdapter adapter;
    private List<Bean> beanList;
    private FavDatabase favDb;
    private FavDao favDao;
    private ClipboardManager clipboard;

    public static List<Bean> readDataFromAssets(Context context, String fileName) {
        List<Bean> data = null;
        BufferedReader reader = null;
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            Gson gson = new Gson();
            java.lang.reflect.Type listType = new TypeToken<List<Bean>>() {}.getType();
            data = gson.fromJson(sb.toString(), listType);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }

    private static final String FILE_NAME = "favList.json";

    public static void saveFavListToFile(Context context, List<FavEntity> favList) {
        // 检查外部存储是否可写
        if (!isExternalStorageWritable()) {
            return;
        }
        Log.e("asd",favList.size()+"");

        Gson gson = new Gson();
        // 将 favList 序列化为 JSON 字符串
        String json = gson.toJson(favList);

        // 获取外部存储路径
        File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dir = new File(externalStorageDir, "db2");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, FILE_NAME);

        // 将 JSON 字符串写入文件
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(json.getBytes());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 检查外部存储是否可写
    private static boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    List<FavEntity> favList2;

    private void loadFavorites() {
        favDb = FavDatabase.getInstance(getActivity());
        favDao = favDb.favDao();
        new Thread(() -> {
            try {
                List<FavEntity> favList = favDao.getAll();
//                saveFavListToFile(this, favList);
                favList2 = favList;
                List<Bean> newBeanList = new ArrayList<>();

                for (FavEntity fav : favList) {
                    Bean bean = new Bean();
                    bean.m3u8 = fav.m3u8;
                    bean.src = fav.src;
                    bean.a2 = fav.a2;
                    bean.type = fav.type;
                    newBeanList.add(bean);
                }

                getActivity().runOnUiThread(() -> {
                    beanList.clear();
                    beanList.addAll(newBeanList);
                    adapter.notifyDataSetChanged();
                    checkEmpty();
                });
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "加载收藏失败: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadFavorites2() {
        new Thread(() -> {
            try {
                List<Bean> beanList2 = readDataFromAssets(getActivity(), "favList.json");
                Log.e("asd","beanList:"+beanList2.size());
                getActivity(). runOnUiThread(() -> {
                    beanList.clear();
                    beanList.addAll(beanList2);
                    adapter.notifyDataSetChanged();
                    checkEmpty();
                });
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "加载收藏失败: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void checkEmpty() {
        if (beanList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fav, container, false);
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        beanList = new ArrayList<>();
        adapter = new VideoAdapter(getActivity(), beanList,false);
        adapter.setSql(true);
        recyclerView.setAdapter(adapter);

        // 设置长按监听器处理取消收藏
        adapter.setOnItemLongClickListener((position, bean) -> {
            ClipData clip = ClipData.newPlainText("url", bean.m3u8);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), "复制成功", Toast.LENGTH_SHORT).show();
//            new android.app.AlertDialog.Builder(this)
//                .setTitle("取消收藏")
//                .setMessage("确定要取消收藏吗？")
//                .setPositiveButton("确定", (dialog, which) -> {
//                    // 在后台线程中删除数据
//                    new Thread(() -> {
//                        try {
//                            favDao.deleteByM3u8(bean.m3u8);
//                            runOnUiThread(() -> {
//                                beanList.remove(position);
//                                adapter.notifyItemRemoved(position);
//                                checkEmpty();
//                                Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
//                            });
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            runOnUiThread(() -> {
//                                Toast.makeText(this, "取消收藏失败: " + e.getMessage(),
//                                    Toast.LENGTH_SHORT).show();
//                            });
//                        }
//                    }).start();
//                })
//                .setNegativeButton("取消", null)
//                .show();
            return true;
        });

        // 加载收藏数据
//        loadFavorites();
        loadFavorites2();

        return view;
    }
}
