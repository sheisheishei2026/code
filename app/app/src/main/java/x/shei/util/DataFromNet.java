//package com.example.myapplication.kan.util;
//
//import static com.example.myapplication.kan.util.IData.HOST_URL;
//import static com.example.myapplication.kan.util.IData.MOVIE_TYPES_1;
//import static com.example.myapplication.kan.util.IData.SEARCH_URL_PRE;
//import static com.example.myapplication.kan.util.IData.URL_SUFFIX;
//import static com.example.myapplication.kan.util.Util.saveToFile2;
//
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.example.myapplication.kan.bean.Movie;
//import com.example.myapplication.kan.bean.MovieType;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.litepal.LitePal;
//import org.litepal.crud.LitePalSupport;
//
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class DataFromNet {
//
//    public static boolean notEmpty(List list) {
//        return !(list == null || list.size() == 0);
//    }
//
//    private static List<Movie> updateCurrentPage(int start, int page, int type, boolean updateTime) {
//        List<Movie> list = new ArrayList<>();
//        try {
//            for (int j = page; j >= start; j--) {
//                String url = getUrlByTypeNumber(j, type);
//                Log.e("asd", "url" + url);
//                if (!TextUtils.isEmpty(url)) {
//                    Document doc = Jsoup.connect(url).get();
//                    Elements content = doc.getElementsByClass("list mb");
//                    if (notEmpty(content)) {
//                        Element el = content.first();
//                        if (el != null) {
//                            Elements li = el.getElementsByTag("li");
//                            if (notEmpty(li)) {
//                                for (int i = li.size() - 1; i >= 0; i--) {
//                                    boolean add = false;
//                                    Movie mainMovie = null;
//                                    Element parse = li.get(i);
//                                    if (parse != null) {
//                                        Elements a = parse.getElementsByTag("a");
//                                        if (notEmpty(a)) {
//                                            Element b = a.first();
//                                            String urls = HOST_URL + b.attr("href");
//                                            Log.e("asd", "详情：" + urls);
//
//                                            Movie temMovie = getBeanByUrl(urls);
//                                            if (temMovie != null) {
//                                                mainMovie = temMovie;
//                                                Log.e("asd", "bean1:id:" + mainMovie.getId());
//                                            } else {
//                                                add = true;
//                                                mainMovie = new Movie();
//                                                mainMovie.setUrl(urls);
//                                            }
//                                            mainMovie.setName(b.attr("title"));
//                                        }
//
//                                        Elements img = parse.getElementsByTag("img");
//                                        if (notEmpty(img)) {
//                                            Element im = img.first();
//                                            if (im != null && !TextUtils.isEmpty(im.attr("src"))) {
//                                                mainMovie.setImg(im.attr("src"));
//                                            }
//                                        }
//                                    }
//                                    parseContent(mainMovie, mainMovie.getUrl());
//                                    list.add(mainMovie);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//
//
//    public static List<Movie> parseBean(String url, boolean parseContent, boolean save) {
//        if (save) {
//            parseContent = true;
//        }
//        List<Movie> list = new ArrayList<>();
//        try {
//            Log.e("asd", "url:" + url);
//            if (!TextUtils.isEmpty(url)) {
//                Document doc = Jsoup.connect(url).get();
//                Elements content = doc.getElementsByClass("list mb");
//                if (notEmpty(content)) {
//                    Element el = content.first();
//                    if (el != null) {
//                        Elements li = el.getElementsByTag("li");
//                        if (notEmpty(li)) {
//                            for (int i = 0; i < li.size(); i++) {
//                                Movie mainMovie = new Movie();
//                                mainMovie.setMainType(1);
//                                Element parse = li.get(i);
//                                if (parse != null) {
//
//                                    Elements img = parse.getElementsByTag("img");
//                                    if (notEmpty(img)) {
//                                        Element im = img.first();
//                                        if (im != null && !TextUtils.isEmpty(im.attr("src"))) {
//                                            mainMovie.setImg("https://ins.lanku.cc:78"+im.attr("src"));
//                                        }
//                                    }
//
//                                    Elements a = parse.getElementsByTag("a");
//                                    if (notEmpty(a)) {
//                                        Element b = a.first();
//                                        mainMovie.setName(b.attr("title"));
//                                        String urls = HOST_URL + b.attr("href");
//                                        mainMovie.setUrl(urls);
//                                        Log.e("asd", "详情：" + urls);
//                                        if (parseContent) {
//                                            parseContent(mainMovie, urls);
//                                        }
//                                    }
//                                }
//                                list.add(mainMovie);
//                                if (save) {
//                                    saveSingleBean(mainMovie);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//
//    // 解析详情内容，传入详情页的url，只做解析
//    public static void parseContent(final Movie mainMovie, String url) {
//        try {
//            if (mainMovie != null) {
//                mainMovie.setUrl(url);
//                Document doc = Jsoup.connect(url).get();
//                parseBaseInfo(mainMovie, doc);
//                parseStar(mainMovie, doc);
//                parseDownWatchList(mainMovie, doc);
//                parseGuessList(mainMovie, doc);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // 解析基本属性
//    private static void parseBaseInfo(Movie mainMovie, Document doc) {
//        // TODO: 2020/9/5
//        mainMovie.setUpdateTime(System.currentTimeMillis());
//
//        Elements e0 = doc.getElementsByClass("description");
//        if (notEmpty(e0)) {
//            Element b = e0.first();
//            mainMovie.setContent(b.wholeText());
//            mainMovie.setContentHtml(b.html());
//        }
//
//        Elements asd = doc.getElementsByClass("box p15");
//        if (notEmpty(asd)) {
//            Element as = asd.first();
//            Elements sa = as.getElementsByTag("img");
//            if (notEmpty(sa)) {
//                Element iii = sa.first();
//                String src = iii.attr("src");
//                mainMovie.setImg(src);
//            }
//        }
//
//        Elements ex = doc.getElementsByClass("intro");
//        if (notEmpty(ex)) {
//            Element b = ex.first();
//
//            Elements fgh = b.getElementsByTag("h1");
//            if (notEmpty(fgh)) {
//                Element dfg = fgh.first();
//                if (dfg != null) {
//                    String ttt = dfg.text();
//                    mainMovie.setName(ttt);
//                }
//            }
//
//            Elements divs = b.getElementsByTag("div");
//            if (notEmpty(divs)) {
//                if (divs.size() > 2) {
//                    Element div = divs.get(2);
//                    if (div != null) {
//                        Elements elements = div.getElementsByClass("note");
//                        if (notEmpty(elements)) {
//                            Element element = elements.get(0);
//                            if (element != null) {
//                                mainMovie.setState(element.text());
//                            }
//                        }
//
//                        Elements elements22 = div.getElementsByTag("p");
//                        if (elements22.size() > 1) {
//                            Element element2 = elements22.get(1);
//                            if (element2 != null) {
//                                String dd = element2.text();
////                                Log.e("asd", "类别：" + element2.text());
//                                if (!TextUtils.isEmpty(dd)) {
//                                    String[] aaa = dd.split("：");
//                                    if (aaa.length > 1 && aaa[0].equals("类型")) {
//                                        String ttttt = aaa[1];
//                                        for (MovieType movieType : MOVIE_TYPES_1) {
//                                            if (movieType.getDesc().equals(ttttt)) {
//                                                mainMovie.setType(movieType.getType());
////                                                Log.e("asd", "setType：" + mainMovie.getType());
//                                                break;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                if (divs.size() > 3) {
//                    Element div = divs.get(3);
//                    if (div != null) {
//                        Elements elements = div.getElementsByTag("p");
//                        if (notEmpty(elements)) {
//                            Element element = elements.get(0);
//                            if (element != null) {
//                                String years = element.text();
//                                if (!TextUtils.isEmpty(years)) {
//                                    String[] array = years.split("：");
//                                    if (array.length > 1 && array[0].equals("年份")) {
//                                        mainMovie.setYear(array[1]);
//                                    }
//                                }
//                            }
//                            if (elements.size() > 1) {
//                                Element element2 = elements.get(1);
//                                if (element2 != null) {
//                                    Elements ts = element2.getElementsByTag("a");
//                                    if (notEmpty(ts)) {
////                                        Log.e("asd", "导演：" + ts.first().text());
//                                        mainMovie.setActor(ts.first().text());
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                if (divs.size() > 4) {
//                    Element div = divs.get(4);
//                    if (div != null) {
//                        Elements elements = div.getElementsByTag("p");
//                        if (notEmpty(elements)) {
//                            Element element = elements.first();
//                            if (element != null) {
//                                String years = element.text();
//                                if (!TextUtils.isEmpty(years)) {
//                                    String[] array = years.split("：");
//                                    if (array.length > 1 && array[0].equals("地区")) {
//                                        mainMovie.setArea(array[1]);
//                                    }
//                                }
//                            }
//                            if (elements.size() > 1) {
//                                Element element2 = elements.get(1);
//                                if (element2 != null) {
//                                    String years = element2.text();
//                                    if (!TextUtils.isEmpty(years)) {
//                                        String[] array = years.split("：");
//                                        if (array.length > 1 && array[0].equals("别名")) {
//                                            mainMovie.setAlias(array[1]);
//                                        }
//                                    }
//                                }
//                            }
//                        } else {
//                            String area = div.text();
//                            if (!TextUtils.isEmpty(area)) {
//                                String[] array = area.split("：");
//                                if (array.length > 1 && array[0].equals("地区")) {
//                                    mainMovie.setArea(array[1]);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private static void parseStar(Movie mainMovie, Document doc) {
//        Elements ex = doc.getElementsByClass("intro");
//        if (notEmpty(ex)) {
//            Element b = ex.first();
//            Elements divs = b.getElementsByTag("div");
//            if (notEmpty(divs)) {
//                if (divs.size() > 1) {
//                    Element div = divs.get(1);
//                    if (div != null) {
//                        Elements as = div.getElementsByTag("a");
//                        if (notEmpty(as)) {
//                            List<Star> list = new ArrayList<>();
//                            for (int i = 0; i < as.size(); i++) {
//                                Star yanyuan = new Star();
//                                String link2 = as.get(i).text();
//                                yanyuan.setName(link2);
//                                list.add(yanyuan);
//                            }
//                            mainMovie.setStarList(list);
//                        }
//                    }
//                }
//            }
//        }
//
//
//    }
//
//
//    // 解析下载列表
//    private static void parseDownWatchList(Movie mainMovie, Document doc) {
//        Elements ex3 = doc.getElementsByClass("playlist");
//        if (notEmpty(ex3)) {
//            Element b = ex3.get(0);
//            if (b != null) {
//                Elements li = b.getElementsByTag("li");
//                if (notEmpty(li)) {
//                    List<Movie.Download> list = new ArrayList<>();
//                    for (int i = 0; i < li.size(); i++) {
//                        Element el = li.get(i);
//                        if (el != null) {
//                            Movie.Download download = mainMovie.new Download();
//                            download.setName(el.text());
//                            Elements ee = el.getElementsByTag("a");
//                            if (notEmpty(ee)) {
//                                Element ef = ee.first();
//                                if (ef != null) {
//                                    download.setUrl(ef.attr("href"));
//                                }
//                            }
//                            list.add(download);
//                        }
//                    }
//                    mainMovie.setDownloadList(list);
//                }
//            }
//
//
//            if (ex3.size() > 1) {
//                Element b2 = ex3.get(1);
//                if (b2 != null) {
//                    Elements li = b2.getElementsByTag("li");
//                    if (notEmpty(li)) {
//                        List<Movie.WatchList> list = new ArrayList<>();
//                        for (int i = 0; i < li.size(); i++) {
//                            Element el = li.get(i);
//                            if (el != null) {
//                                Movie.WatchList watchList = mainMovie.new WatchList();
//                                watchList.setName(el.text());
//                                Elements ee = el.getElementsByTag("a");
//                                if (notEmpty(ee)) {
//                                    Element ef = ee.first();
//                                    if (ef != null) {
//                                        watchList.setUrl(ef.attr("href"));
//                                    }
//                                }
//                                list.add(watchList);
//                            }
//                        }
//                        mainMovie.setWatchList(list);
//                    }
//                }
//            }
//        }
//    }
//
//    public static boolean notEmpty(Elements elements) {
//        return elements != null && elements.size() > 0;
//    }
//
//
//    public static String pre = "https://kkjc6.com/mobile/video/index/cid/";
//    public static String middle = "/p/";
//    public static String last = "/";
////    https://kkjc6.com/mobile/video/index/cid/5/p/1/
//
//    //    https://kkjc6.com/video/show/id/37267
//    // 按页解析网络数据，用来制造数据库，n:第几页，type:类别，parseContent:是否解析详情，save:是否存到数据库
//    public static List<Movie> parseSexBean(int n, int type) {
//        List<Movie> list = new ArrayList<>();
//        try {
//            String url = pre + type + middle + n + last;
//            Log.e("asd", "url:" + url);
//            if (!TextUtils.isEmpty(url)) {
//                Document doc = Jsoup.connect(url).get();
//                Elements content = doc.getElementsByClass("video-item");
//                if (notEmpty(content)) {
//                    for (int i = 0; i < content.size(); i++) {
//                        Movie mainMovie = new Movie();
//                        Element parse = content.get(i);
//                        if (parse != null) {
//
//                            Elements img = parse.getElementsByTag("img");
//                            if (notEmpty(img)) {
//                                Element im = img.first();
//                                if (im != null && !TextUtils.isEmpty(im.attr("src"))) {
//                                    mainMovie.setImg(im.attr("data-original"));
//                                    mainMovie.setName(im.attr("title"));
//                                    Log.e("asd", "title:" + im.attr("title"));
////                                    Log.e("asd", "xxx:" + im.attr("data-original"));
//
////                                    Log.e("asd", "xxx:" + "https://kkjc6.com/" + im.attr("src"));
////                                    mainBean.setImg("https://kkjc6.com/" + im.attr("src"));
//                                }
//                            }
//
//                            Elements a = parse.getElementsByTag("a");
//                            if (notEmpty(a)) {
//                                Element b = a.first();
////                                mainBean.setTitle(b.attr("href"));
//                                String urls = "https://kkjc6.com" + b.attr("href");
//                                mainMovie.setUrl(urls);
//                                Log.e("asd", "详情：" + urls);
//                            }
//
//                        }
//                        list.add(mainMovie);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//
//
//    public static String parse2( ) {
//        String s = "";
//        try {
//            if (!TextUtils.isEmpty("https://www.hitprn.com/")) {
//                Document doc = Jsoup.connect("https://www.hitprn.com/").get();
////                Log.e("asd", "" + doc.outerHtml());
//                saveToFile2(doc.outerHtml());
////                Element content = doc.getElementById("video_html5_api");
////                Element content = doc.getElementById("video_html5_api");
//                Elements content = doc.getElementsByTag("script");
//                /*循环遍历script下面的JS变量*/
//                for (Element element : content) {
//                    /*取得JS变量数组*/
//                    String[] data = element.data().toString().split("var");
//                    /*取得单个JS变量*/
//                    for (String variable : data) {
//                        /*过滤variable为空的数据*/
//                        if (variable.contains("=")) {
//                            /*取到满足条件的JS变量*/
//                            if (variable.contains("playurl") || variable.contains("ipod_filename")) {
//                                String[] kvp = variable.split("=");
//                                s = kvp[1].replace("'", "").replace(";", "").replace(" ", "").trim();
//                                Log.e("asd", "" + kvp[0]);
//                                Log.e("asd", "" + kvp[1]);
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return s;
//    }
//
//
//    public static List<Movie> parseMan(int n, int type) {
//        List<Movie> list = new ArrayList<>();
//        try {
////            String url = "https://www.seancody.com/models?page=" + n + "&sortby=views";
////            String url = "https://www.seancody.com/scenes?models=17665&page=" + n;
//            String url = "https://site-api.project1service.com/v1/actors?gender=male&lastSceneReleaseDate=%3C2020-09-03&limit=24&offset=" + ((n - 1) * 24) + "&orderBy=-stats.views";
//            Log.e("asd", "url:" + url);
//            if (!TextUtils.isEmpty(url)) {
//                Map<String, String> map = new HashMap<>();
//                map.put("Content-Type", "application/json; charset=utf-8");
//                map.put("content type", "application/json; charset=utf-8");
//                map.put("Instance", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJtaW5kZ2VlayIsImF1ZCI6Im1pbmRnZWVrIiwic3ViIjoiaW5zdGFuY2UtYXBpIiwiZXhwIjoxNTk5MTc3NjAwLCJpZCI6MTE5ODIyLCJicmFuZCI6InNlYW5jb2R5IiwiaG9zdG5hbWUiOiJ3d3cuc2VhbmNvZHkuY29tIn0.TLFnkxcMc84iBt-0HEWCm7aIwVddTsI3ZIkdaWXpfo8");
//
//                Document doc = Jsoup.connect(url)
//                        .headers(map)
////                        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Safari/605.1.15")
//                        .get();
//                Log.e("asd", "" + doc.outerHtml());
//                Elements content2 = doc.getElementsByClass("aq1tgu-0 jFKPKs");
//                if (notEmpty(content2)) {
//                    for (int i = 0; i < content2.size(); i++) {
//                        Movie mainMovie = new Movie();
//                        Element ddd = content2.get(i);
//                        if (ddd != null) {
//                            {
//                                Elements parse = ddd.getElementsByClass("aq1tgu-1 hupvSc");
//                                if (notEmpty(parse)) {
//                                    Element fir = parse.first();
//                                    mainMovie.setName(fir.attr("title"));
//                                    mainMovie.setUrl("https://www.seancody.com" + fir.attr("href"));
//                                }
//                            }
//                            {
//                                Elements parse = ddd.getElementsByClass("sc-1p8qg4p-2 ibyLSN");
//                                if (notEmpty(parse)) {
//                                    Element fir = parse.first();
//                                    mainMovie.setImg(fir.attr("src"));
//                                }
//                            }
//                        }
//                        list.add(mainMovie);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//
//    private static final String url2 = SEARCH_URL_PRE + "?page=";
//    private static final String url3 = "&searchword=";
//    private static final String url4 = "&searchtype=";
//
//    private static final String url123 = "https://www.queermetv.com/stars?page=";
//
//    public static List<Movie> doSearch(String key, String mSearchType, int num) {
//        List<Movie> list3 = new ArrayList<>();
//        try {
//            String mUrl = url2 + num + url3 + URLEncoder.encode(key, "gbk") + url4 +
//                    (mSearchType == null ? "-1" : mSearchType);
//            Log.e("asd", "urls:" + mUrl);
//            Document doc2 = Jsoup.connect(mUrl).get();
//            Elements e0 = doc2.getElementsByClass("list mb bt");
//            if (notEmpty(e0)) {
//                Element b = e0.first();
//                if (b != null) {
//                    Elements xx = b.getElementsByTag("li");
//                    if (notEmpty(xx)) {
//                        for (int i = 0; i < xx.size(); i++) {
//                            Element li = xx.get(i);
//                            if (li != null) {
//                                Movie movie = new Movie();
//                                Elements aa = li.getElementsByTag("a");
//                                if (notEmpty(aa)) {
//                                    Element a = aa.first();
//                                    movie.setUrl(HOST_URL + a.attr("href"));
////                                    movie.setUrlSuffix(a.attr("href"));
//                                    movie.setName(a.attr("title"));
//                                    Elements img = a.getElementsByTag("img");
//                                    if (notEmpty(img)) {
//                                        Element im = img.first();
//                                        if (im != null) {
//                                            movie.setImg(im.attr("src"));
//                                        }
//                                    }
//                                }
//                                list3.add(movie);
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list3;
//    }
//
//    // 相似图片
//    public static List<Movie> parsePic2(int n) {
//        List<Movie> list = new ArrayList<>();
//        try {
//            String url = "https://www.vcg.com/creative/search?similar_id=817341457&sort=best_adv&image_url=//goss4.vcg.com/creative/vcg/800/version23/VCG21gic20043197.jpg" + "&productId=&page=" + n;
//            System.out.println("url:" + url);
////            Log.e("asd", "url:" + url);
////            if (textutil != null) {
//            if (url != null) {
//                Document doc = Jsoup.connect(url).get();
//                Elements content2 = doc.getElementsByClass("imgWaper");
//                if (notEmpty(content2)) {
//                    for (int i = 0; i < content2.size(); i++) {
//                        Movie mainMovie = new Movie();
//                        Element ddd = content2.get(i);
//                        if (ddd != null) {
//                            mainMovie.setName("相似图片");
//                            Log.e("asd", "url:" + ddd.attr("href"));
//                            mainMovie.setUrl(ddd.attr("href"));
//                            Elements parse = ddd.getElementsByTag("img");
//
//                            if (notEmpty(parse)) {
//                                Element fir = parse.first();
//                                mainMovie.setImg("https:" + fir.attr("data-src"));
//                                Log.e("asd", "img:" + fir.attr("data-src"));
//                            }
//                        }
//                        list.add(mainMovie);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//
//}
