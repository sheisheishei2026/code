package x.shei.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import x.shei.R;
import x.shei.adapter.ImagePagerAdapter;
import x.shei.util.ImmersedUtil;

public class FullscreenImageActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImagePagerAdapter adapter;
    private List<MediaItem> imageList;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);
        ImmersedUtil.setImmersedMode(this, false);

        viewPager = findViewById(R.id.view_pager);

        // 获取传递的图片列表和当前位置
        Intent intent = getIntent();
        currentPosition = intent.getIntExtra("current_position", 0);

        // 获取图片列表（从ParcelableArrayListExtra）
        ArrayList<MediaItem> parcelableList = intent.getParcelableArrayListExtra("image_list");
        if (parcelableList != null) {
            imageList = new ArrayList<>(parcelableList);
        } else {
            // 兼容旧版本：如果没有列表，尝试从单个图片创建列表
            imageList = new ArrayList<>();
            Uri imageUri = intent.getParcelableExtra("image_uri");
            String imageUrl = intent.getStringExtra("image_url");
            if (imageUri != null) {
                MediaItem item = new MediaItem(imageUri, imageUrl != null ? imageUrl : imageUri.toString());
                imageList.add(item);
                currentPosition = 0;
            } else if (imageUrl != null) {
                MediaItem item = new MediaItem(null, imageUrl);
                imageList.add(item);
                currentPosition = 0;
            }
        }

        // 只显示图片，过滤掉视频
        List<MediaItem> filteredList = new ArrayList<>();
        for (MediaItem item : imageList) {
            if (!item.isVideo) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            finish();
            return;
        }

        // 找到当前图片在新列表中的位置
        if (currentPosition < imageList.size()) {
            MediaItem currentItem = imageList.get(currentPosition);
            for (int i = 0; i < filteredList.size(); i++) {
                if (filteredList.get(i).equals(currentItem)) {
                    currentPosition = i;
                    break;
        }
            }
        }

        // 设置适配器
        adapter = new ImagePagerAdapter(filteredList);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);

        // 设置返回过渡动画
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            viewPager.post(() -> startPostponedEnterTransition());
        }
    }

    @Override
    public void onBackPressed() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            supportFinishAfterTransition();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
