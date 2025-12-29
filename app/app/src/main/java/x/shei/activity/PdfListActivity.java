package x.shei.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import x.shei.R;
import x.shei.adapter.PdfAdapter;
import x.shei.db.PdfInfo;
import x.shei.util.ImmersedUtil;

public class PdfListActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private PdfAdapter pdfAdapter;
    private List<PdfInfo> pdfList;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_list);
        ImmersedUtil.setImmersedMode(this, false);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pdfList = new ArrayList<>();
        pdfAdapter = new PdfAdapter(pdfList, this);
        recyclerView.setAdapter(pdfAdapter);
        mainHandler = new Handler(Looper.getMainLooper());

        // 设置点击监听
        pdfAdapter.setOnPdfClickListener(pdfInfo -> {
            // 打开PDF阅读器
            android.content.Intent intent = new android.content.Intent(PdfListActivity.this, PdfReaderActivity.class);
            intent.putExtra("pdf_path", pdfInfo.getPath());
            intent.putExtra("pdf_name", pdfInfo.getName());
            startActivity(intent);
        });

        // 检查并请求权限
        if (checkStoragePermission()) {
            scanPdfFiles();
        } else {
            requestStoragePermission();
        }
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(android.net.Uri.parse(String.format("package:%s", getPackageName())));
                startActivity(intent);
            } catch (Exception e) {
                android.content.Intent intent = new android.content.Intent();
                intent.setAction(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanPdfFiles();
            } else {
                Toast.makeText(this, "需要存储权限才能扫描PDF文件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新检查权限并扫描
        if (checkStoragePermission()) {
            scanPdfFiles();
        }
    }

    private void scanPdfFiles() {
        new Thread(() -> {
            List<PdfInfo> foundPdfs = new ArrayList<>();

            // 扫描 /sdcard/download 目录
            File downloadDir = new File(Environment.getExternalStorageDirectory(), "Download");
            if (downloadDir.exists() && downloadDir.isDirectory()) {
                scanDirectory(downloadDir, foundPdfs);
            }

            // 也尝试扫描标准的Download目录
            File standardDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (standardDownloadDir.exists() && standardDownloadDir.isDirectory()
                    && !standardDownloadDir.getAbsolutePath().equals(downloadDir.getAbsolutePath())) {
                scanDirectory(standardDownloadDir, foundPdfs);
            }

            // 按修改时间排序（最新的在前）
            Collections.sort(foundPdfs, (o1, o2) -> Long.compare(o2.getLastModified(), o1.getLastModified()));

            mainHandler.post(() -> {
                pdfList.clear();
                pdfList.addAll(foundPdfs);
                pdfAdapter.notifyDataSetChanged();

                if (pdfList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private void scanDirectory(File directory, List<PdfInfo> pdfList) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归扫描子目录
                scanDirectory(file, pdfList);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                pdfList.add(new PdfInfo(file));
            }
        }
    }
}

