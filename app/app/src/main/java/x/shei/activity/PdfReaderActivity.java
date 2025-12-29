package x.shei.activity;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.io.IOException;

import x.shei.R;
import x.shei.util.ImmersedUtil;

public class PdfReaderActivity extends BaseActivity {

    private ViewPager viewPager;
    private ProgressBar progressBar;
    private TextView tvPageInfo;
    private String pdfPath;
    private PdfRenderer pdfRenderer;
    private ParcelFileDescriptor fileDescriptor;
    private int pageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);
        ImmersedUtil.setImmersedMode(this, false);

        pdfPath = getIntent().getStringExtra("pdf_path");
        String pdfName = getIntent().getStringExtra("pdf_name");

        if (pdfPath == null || pdfPath.isEmpty()) {
            Toast.makeText(this, "PDF路径无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        File file = new File(pdfPath);
        if (!file.exists()) {
            Toast.makeText(this, "PDF文件不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadPdf();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);
        tvPageInfo = findViewById(R.id.tvPageInfo);
    }

    private void loadPdf() {
        try {
            progressBar.setVisibility(View.VISIBLE);

            File file = new File(pdfPath);
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(fileDescriptor);
            pageCount = pdfRenderer.getPageCount();

            if (pageCount == 0) {
                Toast.makeText(this, "PDF文件为空", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            viewPager.setAdapter(new PdfPagerAdapter());
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    updatePageInfo(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            updatePageInfo(0);
            progressBar.setVisibility(View.GONE);

        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
            Toast.makeText(this, "加载PDF失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updatePageInfo(int position) {
        tvPageInfo.setText((position + 1) + " / " + pageCount);
    }

    private class PdfPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(android.view.ViewGroup container, int position) {
            ImageView imageView = new ImageView(PdfReaderActivity.this);
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            try {
                PdfRenderer.Page page = pdfRenderer.openPage(position);
                int width = page.getWidth();
                int height = page.getHeight();

                // 根据屏幕宽度缩放
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                float scale = (float) screenWidth / width;
                int scaledWidth = (int) (width * scale);
                int scaledHeight = (int) (height * scale);

                Bitmap bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(android.graphics.Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();

                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(android.view.ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
        if (fileDescriptor != null) {
            try {
                fileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
