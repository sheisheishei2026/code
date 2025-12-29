package x.shei.fragment;


import static x.shei.fragment.FavFragment.readDataFromAssets;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import x.shei.db.Bean;
import x.shei.R;
import x.shei.adapter.VAdapter;

import java.util.ArrayList;
import java.util.List;

public class MovieFragment extends Fragment {
    public int type;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fav, container, false);
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        recyclerView =view. findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
//        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        beanList = new ArrayList<>();

//        for (int i = 0; i <20 ; i++) {
//            Bean bean =new Bean();
//            bean.m3u8 = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";
//            bean.src = "https://img0.baidu.com/it/u=1561407606,3384185231&fm=253&fmt=auto&app=120&f=JPEG?w=667&h=500";
//            beanList.add(bean);
//        }
        adapter = new VAdapter(getActivity(), beanList);
        recyclerView.setAdapter(adapter);

        // 设置长按监听器处理取消收藏
        adapter.setOnItemLongClickListener((position, bean) -> {
            ClipData clip = ClipData.newPlainText("url", bean.m3u8);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), "复制成功", Toast.LENGTH_SHORT).show();
            return true;
        });
        if (type == 1){
            loadFavorites2();
        }else{
            loadDataFromDatabase2();
        }
        return view;
    }

    private RecyclerView recyclerView;
    private TextView emptyView;
    private VAdapter adapter;
    private List<Bean> beanList;

    private ClipboardManager clipboard;

    private void loadDataFromDatabase2() {

//        beanList.clear();
//        {
//            Bean b  =new Bean();
//            b.m3u8 = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";
//            b.title = "123123";
//            beanList.add(b);
//        }
//        {
//            Bean b  =new Bean();
//            b.m3u8 = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";
//            b.title = "123123";
//            beanList.add(b);
//        }
//        {
//            Bean b  =new Bean();
//            b.m3u8 = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";
//            b.title = "123123";
//            beanList.add(b);
//        }
//        {
//            Bean b  =new Bean();
//            b.m3u8 = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";
//            b.title = "123123";
//            beanList.add(b);
//        }
//        adapter.notifyDataSetChanged();

        new Thread(() -> {
            try {
                List<Bean> beanList2 = readDataFromAssets(getActivity(), "movie.json");
                Log.e("asd","beanList:"+beanList2.size());

                getActivity().runOnUiThread(() -> {
                    beanList.clear();
                    beanList.addAll(beanList2);
                    adapter.notifyDataSetChanged();
                    checkEmpty();
                });

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "加载失败: " + e.getMessage(),
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
                getActivity().runOnUiThread(() -> {
                    beanList.clear();
                    beanList.addAll(beanList2);
                    adapter.notifyDataSetChanged();
                    checkEmpty();
                });
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "加载失败: " + e.getMessage(),
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

    public void copy(View v) {
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void takeScreenshot(View v) {
    }

    public void kuaijin(View v) {
    }

    public void download(View v) {
    }

    public void favorite(View v) {
    }
}
