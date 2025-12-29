//package com.example.myapplication;
//
//import static com.example.myapplication.ArrayUtil.notEmpty;
//
//import android.app.Activity;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.text.TextUtils;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import com.google.android.exoplayer2.ExoPlayer;
//import com.google.android.exoplayer2.MediaItem;
//import com.google.android.exoplayer2.ui.PlayerView;
//
//import com.example.myapplication.db.AppDatabase;
//import com.example.myapplication.db.VideoDao;
//import com.example.myapplication.db.VideoEntity;
//
//public class ParseActivity extends Activity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.video);
////        new Thread(() -> {
////            for (int i = 1; i < 200; i++) {
////                Log.e("asd", "-------------第" + i + "页--------------------");
////                parse(76, i);
////                try {
////                    Thread.sleep(6000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
////            }
////
////        }).start();
//    }
//
//
//    public void parse(int type, int page) {
//        try {
//            Document doc = Jsoup.connect("https://www.ke700.cc/type/" + type + "-" + page + ".html").get();
//            Elements content = doc.getElementsByClass("video");
//            if (notEmpty(content)) {
//                for (int i = 0; i < content.size(); i++) {
////                for (int i = 0; i < 2; i++) {
//                    Element parse = content.get(i);
//                    Bean bean = new Bean();
//                    if (parse != null) {
////                        Log.e("asd",  ""+i);
//                        Elements img = parse.getElementsByTag("img");
//                        if (notEmpty(img)) {
//                            Element im = img.first();
//                            if (im != null && !TextUtils.isEmpty(im.attr("src"))) {
//                                bean.src = im.attr("src");
//
//                                // 检查是否已存在
////                                VideoDao videoDao = AppDatabase.getInstance(this).videoDao();
////                                VideoEntity existingVideo = videoDao.findBySrc(bean.src);
////                                if (existingVideo != null) {
////                                    Log.e("asd",i+" 数据已存在");
////                                    continue;
////                                }
//                            }
//                        }
//
//                        Elements a = parse.getElementsByTag("a");
//                        if (notEmpty(a)) {
//                            Element b = a.first();
//                            String urls = "https://www.ke700.cc" + b.attr("href").replace("'", "");
//                            bean.a = urls;
//                            bean.a2 = urls.replace("detial", "play").replace(".html", "") + "-1.html";
////                            Log.e("asd", bean.a2);
//                        }
//                        bean.type = type;
//                        bean.id = Integer.parseInt(extractId(bean.a));
//                        // 解析详情页
//                        extractM3U8Links(bean);
//                        // 写数据库
//                        saveToDatabase(bean);
//                        Thread.sleep(500);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static String extractId(String s) {
//        String numberId = "";
//        // 定义正则表达式来匹配 URL 中的数字 ID
//        // 这里假设数字 ID 位于 /detial/ 之后，并且后面跟着一个连字符和另一个数字（如 -43），或者是 URL 的结尾
//        String regex = "/detial/(\\d+)(?:-\\d+)?\\.html$";
//        Pattern pattern = Pattern.compile(regex);
//
//        // 创建匹配器并尝试匹配正则表达式
//        Matcher matcher = pattern.matcher(s);
//
//        if (matcher.find()) {
//            // 提取并打印数字 ID
//            numberId = matcher.group(1);
//            System.out.println("提取的数字 ID: " + numberId);
//        } else {
//            System.out.println("未找到匹配的数字 ID。");
//        }
//        return numberId;
//    }
//
//    public static String extractM3U8Links(Bean bean) {
//        String m3u8Url = "";
//        try {
//            Document doc = Jsoup.connect("https://www.ke700.cc/play/" + bean.id + "-" + bean.type + "-1.html").get();
//            // 查找 <script> 标签中的 m3u8 链接
//            Elements scripts = doc.select("script");
//            for (Element script : scripts) {
//                String scriptContent = script.html();
//                // 使用正则表达式查找 m3u8 链接
//                Pattern pattern = Pattern.compile("https?://[^\\s]+\\.m3u8");
//                Matcher matcher = pattern.matcher(scriptContent);
//                while (matcher.find()) {
//                    m3u8Url = matcher.group();
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        bean.m3u8 = m3u8Url;
////        Log.e("asd", m3u8Url);
//        return m3u8Url;
//    }
//
//    private void saveToDatabase(Bean bean) {
//        new Thread(() -> {
//            try {
//                VideoDao videoDao = AppDatabase.getInstance(this).videoDao();
//
//                // 检查是否已存在
//                VideoEntity existingVideo = videoDao.findByM3u8(bean.m3u8);
//                if (existingVideo == null) {
//                    // 创建新的实体
//                    VideoEntity videoEntity = new VideoEntity(
//                            bean.src,
//                            bean.m3u8,
//                            bean.a2,
//                            bean.type
//                    );
//
//                    // 插入数据
//                    long result = videoDao.insert(videoEntity);
//                    if (result != -1) {
//                        Log.e("asd", "保存成功");
//                    } else {
//                        Log.e("asd", "保存失败");
//                    }
//                } else {
//                    Log.e("asd", "数据已存在");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e("asd", "保存失败：");
//            }
//        }).start();
//    }
//
//    private void saveListToDatabase(List<Bean> beans) {
//        new Thread(() -> {
//            try {
//                VideoDao videoDao = AppDatabase.getInstance(this).videoDao();
//
//                // 转换为实体列表
//                List<VideoEntity> entities = new ArrayList<>();
//                for (Bean bean : beans) {
//                    entities.add(new VideoEntity(
//                            bean.src,
//                            bean.m3u8,
//                            bean.a2,
//                            bean.type
//                    ));
//                }
//
//                // 批量插入（会自动忽略重复数据）
//                videoDao.insertAll(entities);
//
//                runOnUiThread(() -> {
//                    Toast.makeText(this, "批量保存完成", Toast.LENGTH_SHORT).show();
//                });
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() -> {
//                    Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//            }
//        }).start();
//    }
//
//    private void logDatabasePath() {
//        File dbFile = getDatabasePath("video_database");
//        Log.d("Database", "数据库路径: " + dbFile.getAbsolutePath());
//    }
//
//    private void exportDatabase() {
//        try {
//            File dbFile = getDatabasePath("video_database");
//            if (dbFile.exists()) {
//                File exportDir = new File(Environment.getExternalStorageDirectory(), "DatabaseExport");
//                if (!exportDir.exists()) {
//                    exportDir.mkdirs();
//                }
//                File exportFile = new File(exportDir, "video_database");
//
//                FileInputStream fis = new FileInputStream(dbFile);
//                FileOutputStream fos = new FileOutputStream(exportFile);
//
//                byte[] buffer = new byte[1024];
//                int length;
//                while ((length = fis.read(buffer)) > 0) {
//                    fos.write(buffer, 0, length);
//                }
//
//                fos.flush();
//                fos.close();
//                fis.close();
//
//                Toast.makeText(this, "数据库已导出到: " + exportFile.getAbsolutePath(),
//                        Toast.LENGTH_LONG).show();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//}
