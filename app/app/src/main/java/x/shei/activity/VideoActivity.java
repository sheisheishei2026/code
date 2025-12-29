package x.shei.activity;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import x.shei.R;
import x.shei.adapter.VideoPagerAdapter;
import x.shei.fragment.VideoFragment;
import x.shei.util.ImmersedUtil;
import com.google.android.material.tabs.TabLayout;

public class VideoActivity extends BaseActivity {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private View statusBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ImmersedUtil.setImmersedMode(this,false);
//        statusBarView = findViewById(R.id.statusBarView);
//        ViewGroup.LayoutParams lp = statusBarView.getLayoutParams();
//        lp.height = getStatusBarHeight();
//        statusBarView.setLayoutParams(lp);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // 设置ViewPager适配器
        VideoPagerAdapter pagerAdapter = new VideoPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // 将TabLayout和ViewPager关联起来
        tabLayout.setupWithViewPager(viewPager);

        // 预加载相邻页面
//        viewPager.setOffscreenPageLimit(1);

        // 添加页面切换监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(
                    "android:switcher:" + viewPager.getId() + ":" + position);
                if (fragment instanceof VideoFragment) {
                    VideoFragment videoFragment = (VideoFragment) fragment;
                    if (!videoFragment.isDataLoaded) {
                        videoFragment.reloadData();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }
}
