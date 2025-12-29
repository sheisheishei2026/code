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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import x.shei.util.GridSpacingItemDecoration;
import x.shei.R;
import x.shei.adapter.VideoAdapter;
import x.shei.db.AppDatabase2;
import x.shei.db.VideoDao;
import x.shei.db.VideoEntity;

public class AllFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private VideoAdapter adapter;
    private List<Bean> items = new ArrayList<>();

    private static final String FILE_NAME = "movie.json";

    public static void saveFavListToFile(Context context, List<VideoEntity> favList) {
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
    private void loadDataFromDatabase() {
        new Thread(() -> {
            try {
                VideoDao videoDao = AppDatabase2.getInstance(getActivity()).videoDao();
//                List<VideoEntity> entities = videoDao.getAllByType(type);  // 获取分类数据
                List<VideoEntity> entities = videoDao.getAll();  // 获取所有数据
//                saveFavListToFile(DatabaseListActivity.this, entities);

                // 将 VideoEntity 转换为 Bean
                List<Bean> beans = new ArrayList<>();
                for (VideoEntity entity : entities) {
                    Bean bean = new Bean();
                    bean.src = entity.src;
                    bean.m3u8 = entity.m3u8;
                    bean.a2 = entity.a2;
                    bean.type = entity.type;
                    beans.add(bean);
                }

                getActivity().runOnUiThread(() -> {
                    items.clear();
                    items.addAll(beans);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "加载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }
    private void loadDataFromDatabase2() {
        new Thread(() -> {
            try {
                List<Bean> beanList2 = readDataFromAssets(getActivity(), "movie.json");
                Log.e("asd","beanList:"+beanList2.size());

                getActivity().runOnUiThread(() -> {
                    items.clear();
                    items.addAll(beanList2);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "加载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("m3u8", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_database_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout =  view.findViewById(R.id.swipeRefreshLayout);

        // 使用网格布局
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);

        // 设置item间距
        int spacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
//        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        adapter = new VideoAdapter(getActivity(),items,false);
        adapter.setSql(true);
        adapter.setOnItemLongClickListener((position, bean) -> {
            copyToClipboard(bean.m3u8);
            return true;
        });
        recyclerView.setAdapter(adapter);

        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener(this::loadDataFromDatabase2);

        loadDataFromDatabase2();
        return view;
    }
}
