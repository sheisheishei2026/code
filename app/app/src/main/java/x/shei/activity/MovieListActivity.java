//package x.shei.activity;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import x.shei.R;
//import x.shei.adapter.MovieAdapter;
//import x.shei.db.movie.MovieDownload;
//import x.shei.db.movie.MovieEntity;
//import x.shei.db.movie.MovieWithDownloads;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class MovieListActivity extends Activity {
//    private static final int PERMISSION_REQUEST_CODE = 123;
//    private RecyclerView recyclerView;
//    private TextView emptyView;
//    private MovieAdapter adapter;
//    private List<MovieWithDownloads> movieList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_movie_list);
//
//        recyclerView = findViewById(R.id.recyclerView);
//        emptyView = findViewById(R.id.emptyView);
//
//        // 设置RecyclerView
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        movieList = new ArrayList<>();
//        adapter = new MovieAdapter(this, movieList);
//        recyclerView.setAdapter(adapter);
//
//        checkPermissionsAndLoadData();
//    }
//
//    private void loadMoviesFromFile() {
//        new Thread(() -> {
//            try {
//                File dbFile = new File(Environment.getExternalStorageDirectory(),
//                    "Download/db/video_database");
//
//                if (!dbFile.exists()) {
//                    runOnUiThread(() -> Toast.makeText(this, "数据库文件不存在",
//                        Toast.LENGTH_LONG).show());
//                    return;
//                }
//
//                Log.e("asd", dbFile.getAbsolutePath());
//                SQLiteDatabase db = SQLiteDatabase.openDatabase(
//                    dbFile.getAbsolutePath(),
//                    null,
//                    SQLiteDatabase.OPEN_READWRITE
//                );
//
////                db.execSQL("PRAGMA integrity_check;");
//
//                // 读取电影数据
//                Map<Long, MovieWithDownloads> movieMap = new HashMap<>();
//
//                Cursor movieCursor = db.rawQuery(
//                    "SELECT * FROM movie WHERE id IN (SELECT DISTINCT movie_id FROM movie$download)",
//                    null
//                );
//
//                while (movieCursor.moveToNext()) {
//                    MovieEntity movie = new MovieEntity();
//                    movie.id = movieCursor.getLong(movieCursor.getColumnIndex("id"));
//                    movie.area = movieCursor.getString(movieCursor.getColumnIndex("area"));
//                    movie.img = movieCursor.getString(movieCursor.getColumnIndex("img"));
//                    movie.title = movieCursor.getString(movieCursor.getColumnIndex("title"));
//                    movie.content = movieCursor.getString(movieCursor.getColumnIndex("content"));
//                    movie.contentHtml = movieCursor.getString(movieCursor.getColumnIndex("contentHtml"));
//                    movie.actor = movieCursor.getString(movieCursor.getColumnIndex("actor"));
//
//                    MovieWithDownloads movieWithDownloads = new MovieWithDownloads();
//                    movieWithDownloads.movie = movie;
//                    movieWithDownloads.downloads = new ArrayList<>();
//                    movieMap.put(movie.id, movieWithDownloads);
//                }
//                movieCursor.close();
//
//                // 读取下载数据
//                for (Long movieId : movieMap.keySet()) {
//                    Cursor downloadCursor = db.rawQuery(
//                        "SELECT * FROM movie$download WHERE movie_id = ?",
//                        new String[]{String.valueOf(movieId)}
//                    );
//
//                    MovieWithDownloads movieWithDownloads = movieMap.get(movieId);
//                    while (downloadCursor.moveToNext()) {
//                        MovieDownload download = new MovieDownload();
//                        download.movie_id = movieId;
//                        download.name = downloadCursor.getString(downloadCursor.getColumnIndex("name"));
//                        download.url = downloadCursor.getString(downloadCursor.getColumnIndex("url"));
//                        movieWithDownloads.downloads.add(download);
//                    }
//                    downloadCursor.close();
//                }
//
//                db.close();
//
//                // 更新UI
//                runOnUiThread(() -> {
//                    movieList.clear();
//                    movieList.addAll(movieMap.values());
//                    adapter.notifyDataSetChanged();
//
//                    if (movieList.isEmpty()) {
//                        emptyView.setVisibility(View.VISIBLE);
//                        recyclerView.setVisibility(View.GONE);
//                    } else {
//                        emptyView.setVisibility(View.GONE);
//                        recyclerView.setVisibility(View.VISIBLE);
//                    }
//                });
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() -> {
//                    Toast.makeText(this, "加载失败: " + e.getMessage(),
//                        Toast.LENGTH_SHORT).show();
//                });
//            }
//        }).start();
//    }
//
//    private void checkPermissionsAndLoadData() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                PERMISSION_REQUEST_CODE);
//        } else {
//            loadMoviesFromFile();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                         @NonNull int[] grantResults) {
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                loadMoviesFromFile();
//            } else {
//                Toast.makeText(this, "需要存储权限才能访问数据库", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }
//    }
//}
