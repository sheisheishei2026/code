//package com.example.myapplication.kan.util;
//
//import com.example.myapplication.kan.bean.Movie;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class IData {
//    public static final String HOST_URL = "https://m.xb84w.net";
//
//    public static List<Star> getStar() {
//        List<Star> mBeans = new ArrayList<>();
//        mBeans.add(new Star("玄兵", "https://ins.lanku.cc:78/pic/v/2019-3/20193612294551107.jpg"));
//        mBeans.add(new Star("Nick Fitt", "https://ins.lanku.cc:78/pic/v/2019-10/2019102913564335831.jpg"));
//        mBeans.add(new Star("Jax Thirio", "https://ins.lanku.cc:78/pic/v/2020-5/20205121120520892.jpg"));
//        mBeans.add(new Star("Ty Roderick", "https://ins.lanku.cc:78/pic/v/2014-9/20149313485676841.jpg"));
//        mBeans.add(new Star("Ryan Rose", "https://ins.lanku.cc:78/pic/v/2015-9/201592919465032488.jpg"));
//        mBeans.add(new Star("Zario Travezz", "https://ins.lanku.cc:78/pic/v/2020-1/202011410253130090.jpg"));
//        mBeans.add(new Star("Nic Sahara", "https://ins.lanku.cc:78/pic/v/2019-9/20199109244019239.jpg"));
//        mBeans.add(new Star("Ashton Summers", "https://ins.lanku.cc:78/pic/v/2019-10/2019101610473826611.jpg"));
//        mBeans.add(new Star("Brent Corrigan", "https://ins.lanku.cc:78/pic/v/2016-11/2016111414531055911.jpg"));
//        mBeans.add(new Star("Colby Tucker", "https://ins.lanku.cc:78/pic/v/2019-6/20196289523154269.jpg"));
//        mBeans.add(new Star("Jake Porter", "https://ins.lanku.cc:78/pic/v/2018-12/2018121915102534290.jpg"));
//        mBeans.add(new Star("Will Helm", "https://ins.lanku.cc:78/pic/v/2013-10/201310138345697715.jpg"));
//        mBeans.add(new Star("Klein Kerr", "https://ins.lanku.cc:78/pic/v/2016-7/20167715581325807.jpg"));
//        mBeans.add(new Star("Paddy OBrian", "https://ins.lanku.cc:78/pic/v/2016-8/20168171038962580.jpg"));
//        mBeans.add(new Star("Jess", "https://ins.lanku.cc:78/pic/v/2013-12/201312822175753449.jpg"));
//        mBeans.add(new Star("Dato Foland", "https://ins.lanku.cc:78/pic/v/2016-6/2016681524997279.jpg"));
//        return mBeans;
//    }
//
//    public static List<Movie> parseSearch(int num, String key, String searchType, boolean net) {
//        if (!net) {
//            List<FilterType> ll = new ArrayList();
//            ll.add(new FilterType(2, key));
//            return DataFromSQL.getData(num, 0, ll, null);
//        } else {
//            return DataFromNet.doSearch(key, searchType, num);
//        }
//    }
//
//    public static List<Video> getVideoList() {
//        List<Video> ll = new ArrayList<>();
//        for (int i = 0; i < alist.size() - 1; i++) {
//            Video video = new Video();
//            video.setImg(alist.get(i));
//            video.setVideoUrl(blist.get(i));
//            ll.add(video);
//        }
//        return ll;
//    }
//
//
//    final static List<String> alist = new ArrayList<>();
//    final static List<String> blist = new ArrayList<>();
//
//    static {
//        alist.add("https://images01-buddies.gammacdn.com/movies/78045/78045_01/previews/5/35/top_1_1200x650/78045_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/78182/78182_01/previews/5/35/top_1_1200x650/78182_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/79392/79392_01/previews/5/78/top_1_1200x650/79392_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/78947/78947_01/previews/5/74/top_1_1200x650/78947_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/79017/79017_01/previews/5/106/top_1_1200x650/79017_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/78719/78719_01/previews/5/83/top_1_1200x650/78719_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/79188/79188_01/previews/5/74/top_1_1200x650/79188_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/78543/78543_01/previews/5/83/top_1_1200x650/78543_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/77953/77953_01/previews/5/74/top_1_1200x650/77953_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/77957/77957_01/previews/5/83/top_1_1200x650/77957_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/77819/77819_01/previews/5/74/top_1_1200x650/77819_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/77836/77836_01/previews/5/35/top_1_1200x650/77836_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/78020/78020_01/previews/5/74/top_1_1200x650/78020_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/27919/27919_01/previews/5/7/top_1_1200x650/27919_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/28314/28314_01/previews/5/3/top_1_1200x650/28314_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/77713/77713_01/previews/5/74/top_1_1200x650/77713_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/79072/79072_01/previews/5/83/top_1_1200x650/79072_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/79188/79188_01/previews/5/74/top_1_1200x650/79188_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/79674/79674_01/previews/5/106/top_1_1200x650/79674_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/75104/75104_01/previews/5/3/top_1_1200x650/75104_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/21824/21824_01/previews/5/3/top_1_960x544/21824_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/25167/25167_01/previews/5/74/top_1_1200x650/25167_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/24213/24213_01/previews/5/74/top_1_1200x650/24213_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/10079/10079_01/previews/5/3/top_1_960x544/10079_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/22784/22784_01/previews/5/3/top_1_1200x650/22784_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/9439/9439_01/previews/5/1/top_1_960x544/9439_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/6745/6745_01/previews/5/1/top_1_960x544/6745_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/7626/7626_01/previews/5/1/top_1_960x544/7626_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/8810/8810_01/previews/5/1/top_1_960x544/8810_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/76190/76190_01/previews/5/83/top_1_1200x650/76190_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/77876/77876_01/previews/5/35/top_1_1200x650/77876_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/77967/77967_01/previews/5/83/top_1_1200x650/77967_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/0792/0792_01/previews/5/2/top_1_resized/0792_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/77917/77917_01/previews/5/35/top_1_1200x650/77917_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/76070/76070_01/previews/5/74/top_1_1200x650/76070_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/1753/1753_01/previews/5/2/top_1_resized/1753_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/1753/1753_01/previews/5/2/top_1_resized/1753_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/13179/13179_01/previews/5/38/top_1_960x544/13179_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/1673/1673_01/previews/5/3/top_1_resized/1673_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/1042/1042_01/previews/5/3/top_1_resized/1042_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/1678/1678_01/previews/5/2/top_1_resized/1678_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/1115/1115_01/previews/5/2/top_1_resized/1115_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/1923/1923_01/previews/5/3/top_1_resized/1923_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/0923/0923_01/previews/5/2/top_1_resized/0923_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/1613/1613_01/previews/5/2/top_1_resized/1613_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/1923/1923_01/previews/5/3/top_1_resized/1923_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/2145/2145_01/previews/5/2/top_1_resized/2145_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/3155/3155_01/previews/5/2/top_1_resized/3155_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/11160/11160_01/previews/5/38/top_1_960x544/11160_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/0791/0791_01/previews/5/2/top_1_resized/0791_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/9496/9496_01/previews/5/2/top_1_960x544/9496_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/2508/2508_01/previews/5/1/top_1_resized/2508_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/0788/0788_01/previews/5/2/top_1_resized/0788_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/9843/9843_01/previews/5/7/top_1_960x544/9843_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/78531/78531_01/previews/5/3/top_1_1200x650/78531_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/9855/9855_01/previews/5/1/top_1_960x544/9855_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/78612/78612_01/previews/5/74/top_1_1200x650/78612_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/13276/13276_01/previews/5/38/top_1_960x544/13276_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/12582/12582_01/previews/5/39/top_1_960x544/12582_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/11967/11967_01/previews/5/37/top_1_960x544/11967_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/12018/12018_01/previews/5/7/top_1_960x544/12018_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/13338/13338_01/previews/5/35/top_1_960x544/13338_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/11629/11629_01/previews/5/51/top_1_960x544/11629_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/13190/13190_01/previews/5/2/top_1_960x544/13190_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/1876/1876_01/previews/5/1/top_1_resized/1876_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/21205/21205_01/previews/5/7/top_1_960x544/21205_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/1860/1860_01/previews/5/1/top_1_resized/1860_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/1783/1783_01/previews/5/2/top_1_resized/1783_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/1764/1764_01/previews/5/1/top_1_resized/1764_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/0457/0457_01/previews/5/4/top_1_resized/0457_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/1404/1404_01/previews/5/2/top_1_resized/1404_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/7015/7015_01/previews/5/39/top_1_960x544/7015_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/2350/2350_01/previews/5/2/top_1_resized/2350_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/0951/0951_01/previews/5/3/top_1_resized/0951_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/1361/1361_01/previews/5/2/top_1_resized/1361_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/1395/1395_01/previews/5/2/top_1_resized/1395_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/1996/1996_01/previews/5/1/top_1_resized/1996_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/3154/3154_01/previews/5/2/top_1_resized/3154_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/2331/2331_01/previews/5/3/top_1_resized/2331_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/2327/2327_01/previews/5/3/top_1_resized/2327_01_01.jpg");
//        alist.add("https://images01-buddies.gammacdn.com/movies/3152/3152_01/previews/5/2/top_1_resized/3152_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/3143/3143_01/previews/5/2/top_1_resized/3143_01_01.jpg");
//        alist.add("https://images04-buddies.gammacdn.com/movies/3124/3124_01/previews/5/2/top_1_resized/3124_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/3135/3135_01/previews/5/2/top_1_resized/3135_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/6753/6753_01/previews/5/35/top_1_960x544/6753_01_01.jpg");
//        alist.add("https://images03-buddies.gammacdn.com/movies/1277/1277_01/previews/5/4/top_1_resized/1277_01_01.jpg");
//        alist.add("https://images02-buddies.gammacdn.com/movies/6800/6800_01/previews/5/7/top_1_960x544/6800_01_01.jpg");
//    }
//
//    static {
//        blist.add("https://trailers-buddies.gammacdn.com/5/4/0/8/c78045/trailers/tr_78045_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/2/8/1/8/c78182/trailers/tr_78182_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/2/9/3/9/c79392/trailers/tr_79392_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/7/4/9/8/c78947/trailers/tr_78947_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/7/1/0/9/c79017/trailers/tr_79017_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/9/1/7/8/c78719/trailers/tr_78719_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/8/8/1/9/c79188/trailers/tr_79188_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/3/4/5/8/c78543/trailers/tr_78543_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/3/5/9/7/c77953/trailers/tr_77953_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/7/5/9/7/c77957/trailers/tr_77957_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/9/1/8/7/c77819/trailers/tr_77819_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/6/3/8/7/c77836/trailers/tr_77836_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/2/0/8/c78020/trailers/tr_78020_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/9/1/9/7/c27919/trailers/tr_27919_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/4/1/3/8/c28314/trailers/tr_28314_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/3/1/7/7/c77713/trailers/tr_77713_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/2/7/0/9/c79072/trailers/tr_79072_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/8/8/1/9/c79188/trailers/tr_79188_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/4/7/6/9/c79674/trailers/tr_79674_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/4/0/1/5/c75104/trailers/tr_75104_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/4/2/8/1/c21824/trailers/tr_21824_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/7/6/1/5/c25167/trailers/tr_25167_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/3/1/2/4/c24213/trailers/tr_24213_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/9/7/0/0/c10079/trailers/10079_01/tr_10079_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/4/8/7/2/c22784/trailers/tr_22784_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/9/94/9439/trailers/9439_01/tr_9439_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/6/67/6745/trailers/tr_6745_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/7/76/7626/trailers/7626_01/tr_7626_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/8/88/8810/trailers/tr_8810_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/9/1/6/c76190/trailers/tr_76190_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/6/7/8/7/c77876/trailers/tr_77876_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/7/6/9/7/c77967/trailers/tr_77967_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/07/0792/trailers/tr_0792_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/7/1/9/7/c77917/trailers/tr_77917_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/7/0/6/c76070/trailers/tr_76070_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/17/1753/trailers/tr_1753_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/17/1753/trailers/tr_1753_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/9/7/1/3/c13179/trailers/13179_01/tr_13179_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/16/1673/trailers/tr_1673_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/10/1042/trailers/tr_1042_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/16/1678/trailers/tr_1678_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/11/1115/trailers/tr_1115_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/19/1923/trailers/tr_1923_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/09/0923/trailers/tr_0923_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/16/1613/trailers/tr_1613_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/19/1923/trailers/tr_1923_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/2/21/2145/trailers/tr_2145_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/3/31/3155/trailers/tr_3155_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/6/1/1/c11160/trailers/11160_01/tr_11160_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/07/0791/trailers/tr_0791_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/9/94/9496/trailers/9496_01/tr_9496_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/2/25/2508/trailers/tr_2508_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/07/0788/trailers/tr_0788_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/9/98/9843/trailers/9843_01/tr_9843_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/3/5/8/c78531/trailers/tr_78531_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/9/98/9855/trailers/9855_01/tr_9855_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/2/1/6/8/c78612/trailers/tr_78612_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/6/7/2/3/c13276/trailers/13276_01/tr_13276_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/2/8/5/2/c12582/trailers/12582_01/tr_12582_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/7/6/9/1/c11967/trailers/11967_01/tr_11967_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/8/1/0/2/c12018/trailers/12018_01/tr_12018_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/8/3/3/3/c13338/trailers/13338_01/tr_13338_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/9/2/6/1/c11629/trailers/11629_01/tr_11629_01_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/9/1/3/c13190/trailers/13190_01/tr_13190_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/18/1876/trailers/tr_1876_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/5/0/2/1/c21205/trailers/21205_01/tr_21205_01_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/18/1860/trailers/tr_1860_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/17/1783/trailers/tr_1783_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/17/1764/trailers/tr_1764_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/04/0457/trailers/tr_0457_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/14/1404/trailers/tr_1404_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/7/70/7015/trailers/tr_7015_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/2/23/2350/trailers/tr_2350_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/0/09/0951/trailers/tr_0951_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/13/1361/trailers/tr_1361_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/13/1395/trailers/tr_1395_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/19/1996/trailers/tr_1996_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/3/31/3154/trailers/tr_3154_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/2/23/2331/trailers/tr_2331_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/2/23/2327/trailers/tr_2327_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/3/31/3152/trailers/tr_3152_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/3/31/3143/trailers/tr_3143_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/3/31/3124/trailers/tr_3124_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/3/31/3135/trailers/tr_3135_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/6/67/6753/trailers/tr_6753_hd.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/1/12/1277/trailers/tr_1277_big.mp4");
//        blist.add("https://trailers-buddies.gammacdn.com/6/68/6800/trailers/tr_6800_hd.mp4");
//    }
//
//
//}
