package x.shei.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import x.shei.db.Bean;
import x.shei.db.MovieDao;
import x.shei.db.MovieDatabase;
import x.shei.db.MovieEntity;
import x.shei.db.MovieParser;

public final class Util {

    private static final String FILE_NAME = "fqq";

    private static final int DEFUALT_MODEL = Context.MODE_PRIVATE;

    private static Util sharePreferenceUtil;

    private static SharedPreferences preferences;

    private static Editor editor;

    private Util() {

    }

    public static void saveToFile2(String content) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(content);
        stringBuilder.append("\n");
        saveToFile(stringBuilder.toString());
    }

    public static void saveToFile(String content) {
        BufferedWriter out = null;

        //获取SD卡状态
        String state = Environment.getExternalStorageState();
        //判断SD卡是否就绪
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
//            Toast.makeText(this, "请检查SD卡", Toast.LENGTH_SHORT).show();
            return;
        }
        //取得SD卡根目录
        File file = Environment.getExternalStorageDirectory();
        try {
            Log.e("sss", "======SD卡根目录：" + file.getCanonicalPath());
            if (file.exists()) {
                Log.e("sss", "file.getCanonicalPath() == " + file.getCanonicalPath());
            } else {
                file.createNewFile();
            }
            //输出流的构造参数1：可以是File对象 也可以是文件路径,输出流的构造参数2：默认为False=>覆盖内容； true=>追加内容
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getCanonicalPath()
                    + "/test.txt", false)));
            out.newLine();
            out.write(content);
//            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public synchronized static Util get(Context context) {

        if (sharePreferenceUtil == null) {

            sharePreferenceUtil = new Util();

            if (preferences == null) {

                preferences = context.getSharedPreferences(FILE_NAME,

                        DEFUALT_MODEL);

                if (editor == null) {

                    editor = preferences.edit();

                }

            }

        }

        return sharePreferenceUtil;

    }

    public Util write(String key, String writeContent) {

        editor.putString(key, writeContent);

        editor.commit();

        return sharePreferenceUtil;

    }

    public Util write(String key, int writeContent) {

        editor.putInt(key, writeContent);

        editor.commit();

        return sharePreferenceUtil;

    }

    public Util write(String key, boolean writeContent) {

        editor.putBoolean(key, writeContent);

        editor.commit();

        return sharePreferenceUtil;

    }

    public Util write(String key, float writeContent) {

        editor.putFloat(key, writeContent);

        editor.commit();

        return sharePreferenceUtil;

    }

    public Util write(String key, long writeContent) {

        editor.putLong(key, writeContent);

        editor.commit();

        return sharePreferenceUtil;

    }

    public Util write(String key, Set writeContent) {

        editor.putStringSet(key, writeContent);

        editor.commit();

        return sharePreferenceUtil;

    }

    public String read(String key, String defaultValue) {

        return preferences.getString(key, defaultValue);

    }

    public int read(String key, int defaultValue) {

        return preferences.getInt(key, defaultValue);

    }

    public long read(String key, long defaultValue) {

        return preferences.getLong(key, defaultValue);

    }

    public float read(String key, float defaultValue) {

        return preferences.getFloat(key, defaultValue);

    }

    public Set read(String key, Set defaultValue) {

        return preferences.getStringSet(key, defaultValue);

    }

    public boolean read(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);

    }

//    private void readFromDB() {
//        new Thread(() -> {
//            MovieDatabase favDb = MovieDatabase.getInstance(getActivity());
//            MovieDao favDao = favDb.movieDao();
//            // 清理重复数据：删除 m3u8 和 title 相同的重复记录
////            favDao.deleteDuplicates();
//            List<MovieEntity> favList = favDao.getAllMovies();
//            for (MovieEntity bean : favList) {
//                newBeanList.add(bean.rec());
//                Log.e("asd",bean.rec().toString());
//            }
//            getActivity().runOnUiThread(() -> {
//                // 预计算筛选选项列表（缓存）
//                buildFilterOptionsCache();
//                // 重新应用筛选条件
//                String keyword = mSearchView != null ? mSearchView.getQuery().toString() : "";
//                filterData(keyword);
////                Toast.makeText(getActivity(),"共"+beanList.size()+"条数据",Toast.LENGTH_SHORT).show();
//            });
//        }).start();
//        //        new Thread(() -> {
////            MovieDatabase.getInstance(getActivity()).movieDao().deleteDuplicates();
////
////        }).start();
//    }

//private void parseData() {
//    new Thread(() -> {
//        // 120
//        MovieParser.parseMovieList(getActivity(),1340,1341);
////            List<Bean> movieList = MovieParser.parseMovieList(getActivity(),20,25);
////            getActivity().runOnUiThread(() -> {
////                beanList.clear();
////                beanList.addAll(movieList);
////                adapter.notifyDataSetChanged();
////                checkEmpty();
////                if (movieList.isEmpty()) {
////                    Log.e(TAG,"未解析到影片数据");
////                } else {
////                    Log.e(TAG,"成功解析 " + movieList.size() + " 部影片");
////                    for (int i = 0; i <movieList.size() ; i++) {
////                        Log.e(TAG,movieList.get(i).toString());
////                    }
////                }
////            });
//    }).start();
//}


    /**
     * 显示删除确认对话框
     */
//    private void showDeleteConfirmDialog(Bean bean, int position) {
//        String title = bean.title != null ? bean.title : "未知标题";
//        new AlertDialog.Builder(getActivity())
//                .setTitle("确认删除")
//                .setMessage("确定要删除《" + title + "》吗？")
//                .setPositiveButton("确定", (dialog, which) -> {
//                    deleteMovie(bean, position);
//                })
//                .setNegativeButton("取消", null)
//                .show();
//    }

    /**
     * 从数据库删除影片
     */
//    private void deleteMovie(Bean bean, int position) {
//        new Thread(() -> {
//            try {
//                MovieDatabase favDb = MovieDatabase.getInstance(getActivity());
//                MovieDao favDao = favDb.movieDao();
//                String m3u8 = bean.m3u8 != null ? bean.m3u8 : "";
//                String title = bean.title != null ? bean.title : "";
//                // 根据 m3u8 和 title 删除
//                favDao.deleteByM3u8AndTitle(m3u8, title);
//
//                getActivity().runOnUiThread(() -> {
//                    // 从当前显示列表中移除
//                    if (position >= 0 && position < beanList.size()) {
//                        beanList.remove(position);
//                    }
//                    // 从原始数据列表中移除（通过 m3u8 和 title 匹配）
//                    newBeanList.removeIf(b ->
//                            (b.m3u8 != null ? b.m3u8 : "").equals(m3u8) &&
//                                    (b.title != null ? b.title : "").equals(title));
//                    // 清除筛选选项缓存，下次需要重新构建
//                    cachedCountryList = null;
//                    cachedYearList = null;
//                    adapter.notifyDataSetChanged();
//                    checkEmpty();
//                    Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//                getActivity().runOnUiThread(() -> {
//                    Toast.makeText(getActivity(), "删除失败: " + e.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                });
//            }
//        }).start();
//    }

    /**
     * 导出数据库内容到JSON文件
     */
//    private void exportToJson() {
//        new Thread(() -> {
//            try {
//                MovieDatabase favDb = MovieDatabase.getInstance(getActivity());
//                MovieDao favDao = favDb.movieDao();
//                List<MovieEntity> movieList = favDao.getAllMovies();
//
//                // 转换为Bean列表
//                List<Bean> beanList = new ArrayList<>();
//                for (MovieEntity entity : movieList) {
//                    beanList.add(entity.rec());
//                }
//
//                // 检查外部存储是否可写
//                if (!isExternalStorageWritable()) {
//                    getActivity().runOnUiThread(() -> {
//                        Toast.makeText(getActivity(), "外部存储不可用，无法导出", Toast.LENGTH_SHORT).show();
//                    });
//                    return;
//                }
//
//                // 使用Gson序列化为JSON
//                Gson gson = new Gson();
//                String json = gson.toJson(beanList);
//
//                // 获取外部存储路径
//                File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                File dir = new File(externalStorageDir, "db2");
//                if (!dir.exists()) {
//                    dir.mkdirs();
//                }
//
//                // 生成文件名（带时间戳）
//                String fileName = "movie_export_" + System.currentTimeMillis() + ".json";
//                File file = new File(dir, fileName);
//
//                // 写入文件
//                FileOutputStream fos = null;
//                try {
//                    fos = new FileOutputStream(file);
//                    fos.write(json.getBytes("UTF-8"));
//                    fos.flush();
//
//                    final String filePath = file.getAbsolutePath();
//                    getActivity().runOnUiThread(() -> {
//                        Toast.makeText(getActivity(), "导出成功！\n文件路径: " + filePath, Toast.LENGTH_LONG).show();
//                        Log.e(TAG, "导出成功，文件路径: " + filePath);
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    getActivity().runOnUiThread(() -> {
//                        Toast.makeText(getActivity(), "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });
//                } finally {
//                    if (fos != null) {
//                        try {
//                            fos.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                getActivity().runOnUiThread(() -> {
//                    Toast.makeText(getActivity(), "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//            }
//        }).start();
//    }

}


