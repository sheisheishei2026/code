package x.shei.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import x.shei.db.PlaceItem;
import x.shei.fragment.MapFragment;
import x.shei.R;
import x.shei.fragment.PlaceListFragment;
import x.shei.fragment.ScenicFragment;
import x.shei.adapter.TabPagerAdapter;
import x.shei.fragment.LocationListFragment;
import x.shei.util.ImmersedUtil;

public class PoiActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TabPagerAdapter adapter;
    MapFragment mapFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_main);
        ImmersedUtil.setImmersedMode(this, false);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        // 禁用ViewPager2的滑动
        viewPager.setUserInputEnabled(false);

        // 创建Fragment列表
        adapter = new TabPagerAdapter(this);

        mapFragment = new MapFragment();
        adapter.addFragment(mapFragment, "地图");
        adapter.addFragment(new PlaceListFragment(), "景点");
        adapter.addFragment(new ScenicFragment(), "VR");
        adapter.addFragment(new LocationListFragment(), "专辑");


        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);

        // 连接TabLayout和ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    // 创建自定义Tab视图
                    View customView = LayoutInflater.from(this).inflate(R.layout.item_tab, null);
                    TextView textView = customView.findViewById(R.id.tab_text);
                    textView.setText(adapter.getPageTitle(position));
                    tab.setCustomView(customView);
                }
        ).attach();

        // 设置初始选中状态
        updateTabTextColor(0);

        // 监听TabLayout的选中事件
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(position, false);
                updateTabTextColor(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 不需要处理
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 不需要处理
            }
        });
    }

    private void updateTabTextColor(int position) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null && tab.getCustomView() != null) {
                TextView textView = tab.getCustomView().findViewById(R.id.tab_text);
                if (textView != null) {
                    textView.setTextColor(i == position ? getResources().getColor(R.color.colorPrimary) : 0xFF999999);
                }
            }
        }
    }

    public void change(View v){
    }

    public void switchMap(){
        viewPager.setCurrentItem(0, false);
    }

    public void moveTo(PlaceItem item){
        if (mapFragment!=null){
            mapFragment.moveTo(item);
        }
    }
}
