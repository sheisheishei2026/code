package x.shei.fragment;

import static x.shei.util.ArrayUtil.notEmpty;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import x.shei.db.Bean;
import x.shei.adapter.NumberAdapter;
import x.shei.R;
import x.shei.adapter.VideoAdapter;

public class VideoFragment extends Fragment {
    private RecyclerView recyclerView;
    private VideoAdapter imageAdapter;
    private List<Bean> imageUris = new ArrayList<>();
    boolean single = true;
    private int videoType = 13;
    public boolean isDataLoaded = false;
    private View loadingLayout;

    public void setVideoType(int type) {
        this.videoType = type;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint() && !isDataLoaded) {
            loadData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed() && !isDataLoaded) {
            loadData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, container, false);

        initViews(view);

        if (getUserVisibleHint() && !isDataLoaded) {
            loadData();
        }

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        if (single){
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else{
            StaggeredGridLayoutManager layoutManager =
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
        }
        imageAdapter = new VideoAdapter(getActivity(), imageUris,false);
        recyclerView.setAdapter(imageAdapter);

        RecyclerView recyclerView2 = view.findViewById(R.id.recyclerView2);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
            LinearLayoutManager.VERTICAL, false);
        recyclerView2.setLayoutManager(layoutManager);

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            numbers.add(i);
        }

        loadingLayout = view.findViewById(R.id.loadingLayout);

        NumberAdapter adapter = new NumberAdapter(numbers, number -> {
            Toast.makeText(getActivity(),number+"页",Toast.LENGTH_SHORT).show();
            showLoading();
            new Thread(() -> {
                parse(videoType, number);
                hideLoading();
            }).start();
        });
        recyclerView2.setAdapter(adapter);
    }

    private void showLoading() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (loadingLayout != null) {
                    loadingLayout.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void hideLoading() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (loadingLayout != null) {
                    loadingLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    private void loadData() {
        if (!isDataLoaded && getActivity() != null) {
            showLoading();
            new Thread(() -> {
                parse(videoType, 1);
                hideLoading();
            }).start();
            isDataLoaded = true;
        }
    }

    public void reloadData() {
        isDataLoaded = false;
        loadData();
    }

    private static void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // 忽略主机名验证
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public List<Bean> parse(int type, int page) {
        Log.e("asd","parse:"+type);
        List<Bean> list = new ArrayList<>();
        try {
            trustAllCertificates();
            Document doc = Jsoup.connect("https://175.27.231.213:15588/index.php/vod/type/id/" + type + "/page/" + page + ".html")
                    .postDataCharset("GBK")  // 或者 "GBK"
                    .get();
            doc.outputSettings().charset("GBK");
            Log.e("asd",doc.toString());
            Elements content = doc.getElementsByClass("content-item content-item-2");
            if (notEmpty(content)) {
                for (int i = 4; i < content.size() - 4; i++) {
                    Element parse = content.get(i);
                    Bean bean = new Bean();
                    if (parse != null) {
                        Elements img = parse.getElementsByTag("img");
                        if (notEmpty(img)) {
                            Element im = img.first();
                            if (im != null && !TextUtils.isEmpty(im.attr("src"))) {
                                bean.src = im.attr("src");
                            }
                        }

                        Elements p = parse.getElementsByTag("h5");
                        if (notEmpty(p)) {
                            Element b = p.first();
                            if (b != null) {
                                Elements c = b.getElementsByTag("a");
                                if (notEmpty(c)) {
                                    Element x = c.first();
                                    if (x != null) {
                                        bean.title = x.text();
                                    }
                                }
                            }
                        }

                        Elements a = parse.getElementsByTag("a");
                        if (notEmpty(a)) {
                            Element b = a.first();
                            String urls = "https://175.27.231.213:15588" + b.attr("href").replace("'", "");
                            bean.a = urls;
                            bean.a2 = urls.replace("detail", "play").replace(".html", "") + "/sid/1/nid/1.html";
                        }
                        bean.type = type;
                        Log.e("asd",bean.a);
                        bean.id = Integer.parseInt(extractId(bean.a));
                        list.add(bean);
                    }
                }
            }
            Log.e("asd","list:"+list.size());
            imageUris.clear();
            imageUris.addAll(list);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    imageAdapter.notifyDataSetChanged();
                    try{
                        recyclerView.scrollToPosition(0);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            hideLoading(); // 发生错误时也要隐藏加载提示
        }
        return list;
    }

    public static String extractId(String s){
        String numberId = "";
        // 定义正则表达式来匹配 URL 中的数字 ID
        // 这里假设数字 ID 位于 /detial/ 之后，并且后面跟着一个连字符和另一个数字（如 -43），或者是 URL 的结尾
        String regex = "/detail/id/(\\d+)(?:-\\d+)?\\.html$";
        Pattern pattern = Pattern.compile(regex);

        // 创建匹配器并尝试匹配正则表达式
        Matcher matcher = pattern.matcher(s);

        if (matcher.find()) {
            // 提取并打印数字 ID
            numberId = matcher.group(1);
            System.out.println("提取的数字 ID: " + numberId);
        } else {
            System.out.println("未找到匹配的数字 ID。");
        }
        return numberId;
    }

    public static String extractM3U8Links(Bean bean){
        String m3u8Url = "";
        Log.e("asd","url:"+ "https://www.ke700.cc/play/" + bean.id + "-"+bean.type+"-1.html");

        try {
            Document doc = Jsoup.connect("https://www.ke700.cc/play/" + bean.id + "-"+bean.type+"-1.html").get();
            // 查找 <script> 标签中的 m3u8 链接
            Elements scripts = doc.select("script");
            for (Element script : scripts) {
                String scriptContent = script.html();
                // 使用正则表达式查找 m3u8 链接
                Pattern pattern = Pattern.compile("https?://[^\\s]+\\.m3u8");
                Matcher matcher = pattern.matcher(scriptContent);
                while (matcher.find()) {
                    m3u8Url = matcher.group();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bean.m3u8 = m3u8Url;
        Log.e("asd",m3u8Url);
        return m3u8Url;
    }
}
