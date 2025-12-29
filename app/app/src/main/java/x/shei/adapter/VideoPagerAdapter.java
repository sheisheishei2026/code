package x.shei.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import x.shei.fragment.VideoFragment;

public class VideoPagerAdapter extends FragmentPagerAdapter {
    // type 43 ou  75 san  41guozi  40guozhu  76madou  50katong 47zimu 45qiang 42wu 38ya
    private final int[] videoTypes = {13,  20,   9,   7,  33,  6,   10, 22,  16,  15, 23,  8,  12,  14, 11, 21};
    private final String[] titles = {"欧", "三", "AI","无","卡","国","制","字","强","黑","S","探","巨","熟","日","女"};

    public VideoPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        VideoFragment fragment = new VideoFragment();
        fragment.setVideoType(videoTypes[position]);
        return fragment;
    }

    @Override
    public int getCount() {
        return videoTypes.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
