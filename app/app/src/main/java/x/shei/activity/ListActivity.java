//package x.shei.activity;
//
//import static x.shei.util.ArrayUtil.notEmpty;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
////import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import x.shei.R;
//import x.shei.adapter.ListAdapter;
//import x.shei.adapter.NumberAdapter;
//import x.shei.util.GridSpacingItemDecoration;
//import x.shei.util.ImmersedUtil;
//import x.shei.db.ListItem;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ListActivity extends BaseActivity {
//    private RecyclerView recyclerView;
//    //    private SwipeRefreshLayout swipeRefreshLayout;
//    private ListAdapter adapter;
//    private List<ListItem> items = new ArrayList<>();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_list);
//        ImmersedUtil.setImmersedMode(this, false);
//
//        recyclerView = findViewById(R.id.recyclerView);
////        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
//
//        // 使用 GridLayoutManager 设置3列
//        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
//        recyclerView.setLayoutManager(layoutManager);
//
//        // 设置item间距
//        int spacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
//        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing, true));
//
//        adapter = new ListAdapter(this, items);
//        recyclerView.setAdapter(adapter);
//
//        adapter.setOnItemClickListener(item -> {
//            Intent intent = new Intent(ListActivity.this, WebAct.class);
//            intent.putExtra("url", item.getLink());
//            ListActivity.this.startActivity(intent);
////            DetailActivity.start(this,item.getLink(),item.getLink());
//        });
//
//
//        RecyclerView recyclerView2 = findViewById(R.id.recyclerView2);
//        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this,
//                LinearLayoutManager.VERTICAL, false);
//        recyclerView2.setLayoutManager(layoutManager2);
//
//        List<Integer> numbers = new ArrayList<>();
//        for (int i = 1; i <= 708; i++) {
//            numbers.add(i);
//        }
//        new Thread(() -> {
//            loadData(0);
//        }).start();
//
//
//        NumberAdapter adapter = new NumberAdapter(numbers, number -> {
//            new Thread(() -> {
//                loadData(number);
//            }).start();
//        });
//        recyclerView2.setAdapter(adapter);
//    }
//
//    private void loadData(int index) {
//        String index2 = "";
//        if (index >= 2 && index <= 708) {
//            index2 = index + "";
//        }
//        try {
//            Document doc = Jsoup.connect("https://m.xb84w.net/vod/newbl" + index2 + ".html").get();
//            Elements elements = doc.getElementsByClass("list mb");
//            List<ListItem> newItems = new ArrayList<>();
//            if (notEmpty(elements)) {
//                Element el = elements.first();
//                if (el != null) {
//                    Elements li = el.getElementsByTag("li");
//                    if (notEmpty(li)) {
//                        for (Element element : li) {
//                            String title = element.select("a").text();
//                            String imageUrl = "https://m.xb84w.net/" + element.select("img").attr("src");
//                            String link = "https://m.xb84w.net/" + element.select("a").attr("href");
//                            Log.e("asd", "title:" + title);
//                            Log.e("asd", "imageUrl:" + imageUrl);
//                            Log.e("asd", "link:" + link);
//
//                            newItems.add(new ListItem(title, imageUrl, link));
//                        }
//                    }
//                }
//            }
//
//            runOnUiThread(() -> {
//                items.clear();
//                items.addAll(newItems);
//                adapter.notifyDataSetChanged();
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            runOnUiThread(() -> {
//                Toast.makeText(this, "加载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
//            });
//        }
//    }
//}
