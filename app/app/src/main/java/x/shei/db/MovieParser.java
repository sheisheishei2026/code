package x.shei.db;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import x.shei.util.CsvGenerator;


public class MovieParser {
    public static final String TAG = "MovieParser";
    // 目标网页 URL 1340
    public static final String TARGET_URL = "https://www.sh-yxfhm.com/type/36-";
    // 网站根域名（用于拼接相对路径为完整 URL）
    private static final String BASE_DOMAIN = "https://www.sh-yxfhm.com";

    // 原有：列表页解析方法 parseBeanList()...（保持不变）

    /**
     * 解析详情页，补充 Bean 的详情字段（主演、导演、国家等）
     * @param bean 列表页已解析的基础 Bean 对象
     * @return 补充详情后的 Bean（失败则返回原 Bean）
     */
    public static Bean parseDetailPage(Bean bean) {
        // 1. 校验详情页链接是否有效
        if (bean == null || bean.a == null || bean.a.isEmpty()) {
            System.out.println("Bean 或详情页链接为空，无法解析详情");
            return bean;
        }

        try {
            // 2. 请求详情页 HTML
//            String detailHtml = getHtmlFromUrl(bean.a);
//            if (detailHtml == null || detailHtml.isEmpty()) {
//                System.out.println("详情页请求失败，URL：" + bean.a);
//                return bean;
//            }

            // 3. Jsoup 解析 HTML
//            Document doc = Jsoup.parse(detailHtml);

            Document doc = Jsoup.connect(bean.a).get();

            // 4. 定位核心信息容器：.details-info 下的 ul.info 列表
            Element infoUl = doc.selectFirst("div.details-info ul.info");
            if (infoUl == null) {
                System.out.println("详情页未找到信息列表 ul.info");
                return bean;
            }

            // 5. 提取字段（按 li 标签遍历，匹配关键词）
            Elements infoLis = infoUl.select("li");
            for (Element li : infoLis) {
                String liText = li.text().trim(); // 获取 li 纯文本（含标签内所有内容）
                String liHtml = li.html();       // 保留 HTML，用于提取主演链接文本

                // 5.1 提取主演（匹配“主演：”关键词，处理多个 a 标签）
                if (liText.contains("主演：")) {
                    // 提取 li 下所有 a 标签的文本（排除空文本）
                    Elements actorATags = li.select("a[href*=search]");
                    StringBuilder mainActorSb = new StringBuilder();
                    for (Element aTag : actorATags) {
                        String actorName = aTag.text().trim();
                        if (!actorName.isEmpty()) {
                            mainActorSb.append(actorName).append("、");
                        }
                    }
                    // 去除末尾多余的“、”
                    String mainActor = mainActorSb.length() > 0
                            ? mainActorSb.substring(0, mainActorSb.length() - 1)
                            : "";
                    bean.mainActor = mainActor;
                }

                // 5.2 提取导演（匹配“导演：”关键词）
                else if (liText.contains("导演：")) {
                    // 提取 a 标签文本（导演名）
                    Element directorATag = li.selectFirst("a[href*=mingxing]");
                    bean.director = directorATag != null ? directorATag.text().trim() : "";
                }

                // 5.3 提取国家/地区（匹配“国家/地区：”关键词）
                else if (liText.contains("国家/地区：")) {
                    // 截取“国家/地区：”后的内容（如“美国”）
                    bean.country = extractValueAfterKey(liText, "国家/地区：");
                }

                // 5.5 提取更新时间（匹配“更新时间：”关键词）
                else if (liText.contains("更新时间：")) {
                    bean.updateTime = extractValueAfterKey(liText, "更新时间：");
                }
                else if (liText.contains("年代：")) {
                    bean.updateTime = extractValueAfterKey(liText, "年代：");
                }
            }

            // 5.6 提取详细介绍（匹配 .details-content-all 或 .details-content-default）
            Element introAll = doc.selectFirst("span.details-content-all");
            Element introDefault = doc.selectFirst("span.details-content-default");
            if (introAll != null && !introAll.text().trim().isEmpty()) {
                bean.intro = introAll.text().trim(); // 优先完整介绍
            } else if (introDefault != null) {
                bean.intro = introDefault.text().trim(); // 备选简略介绍
            }

            // 5.7 提取播放页链接 a2（从 .details-pic 的 a 标签获取）
            Element playATag = doc.selectFirst("div.details-pic a.video-pic[href*=bofang]");
            if (playATag != null) {
                String playRelativeUrl = playATag.attr("href").trim();
                bean.a2 = BASE_DOMAIN + playRelativeUrl; // 拼接完整播放页 URL
            }


            System.out.println("详情页：" + bean.title );

        } catch (Exception e) {
            System.out.println("详情页解析异常，URL：" + bean.a);
        }

        return bean;
    }

    /**
     * 工具方法：从文本中截取“关键词”后的内容（如“国家/地区：美国”→ 截取“美国”）
     */
    private static String extractValueAfterKey(String text, String key) {
        if (text == null || key == null || !text.contains(key)) {
            return "";
        }
        int keyIndex = text.indexOf(key);
        return text.substring(keyIndex + key.length()).trim();
    }
    public static String parseM3u8FromPlayPage(String playPageUrl) {
        if (playPageUrl == null || playPageUrl.isEmpty()) {
            System.out.println("播放页 URL 为空");
            return "";
        }

        try {
            // 1. 请求播放页 HTML 源码
            Document doc = Jsoup.connect(playPageUrl).get();
            String playHtml =doc.outerHtml();

            if (playHtml == null || playHtml.isEmpty()) {
                System.out.println("播放页请求失败，URL：" + playPageUrl);
                return "";
            }

            // ==============================================
            // 新增：优先解析 player_aaaa 变量中的 m3u8（精准匹配）
            // ==============================================
            String m3u8 = extractM3u8FromPlayerAaaa(playHtml);
            if (!m3u8.isEmpty()) {
//                System.out.println("从 player_aaaa 提取 m3u8 成功：" + m3u8);
                return m3u8;
            }

            // （可选保留）原有通用解析逻辑（防止其他页面结构变化）
            m3u8 = extractM3u8FromPlainText(playHtml);
            if (!m3u8.isEmpty()) {
                System.out.println("方式1（明文）获取 m3u8：" + m3u8);
                return m3u8;
            }
            // ... 其他原有通用逻辑（如 JS 变量、video 标签）...

        } catch (Exception e) {
            System.out.println("播放页解析 m3u8 异常，URL：" + playPageUrl);
        }

        return "";
    }

    /**
     * 方式1：从 HTML 明文直接提取 m3u8（匹配包含 .m3u8 的链接）
     */
    private static String extractM3u8FromPlainText(String html) {
        // 正则表达式：匹配 http/https 开头、以 .m3u8 结尾的链接（含参数）
        Pattern pattern = Pattern.compile("(https?://[\\w\\-./?&=]+\\.m3u8)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // 匹配相对路径的 m3u8（如 /video/xxx.m3u8）
        pattern = Pattern.compile("(\\/[\\w\\-./?&=]+\\.m3u8)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(html);
        if (matcher.find()) {
            return BASE_DOMAIN + matcher.group(1).trim();
        }
        return "";
    }

    /**
     * 精准解析 player_aaaa 变量中的 m3u8 链接（针对目标页面结构）
     * 匹配格式：var player_aaaa={"flag":"play",..."url":"https://xxx.m3u8"...}
     */
    private static String extractM3u8FromPlayerAaaa(String html) {
        // 1. 正则表达式：精准匹配 player_aaaa 变量的 url 属性
        // 说明：匹配 "var player_aaaa=" 开头，然后找到 "url":"xxx"，提取 xxx（支持单引号/双引号，兼容格式差异）
        Pattern pattern = Pattern.compile(
                "var\\s+player_aaaa\\s*=\\s*\\{[\\s\\S]*?\"url\"\\s*:\\s*['\"]([^'\"]+\\.m3u8)['\"]",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(html);

        // 2. 提取并验证 m3u8 链接
        if (matcher.find()) {
            String m3u8Url = matcher.group(1).trim();
            // 验证是否为合法 m3u8 链接（防止匹配错误）
            if (m3u8Url.startsWith("http") && m3u8Url.endsWith(".m3u8")) {
                return m3u8Url;
            }
            System.out.println("player_aaaa 匹配到的 url 不是合法 m3u8：" + m3u8Url);
        } else {
            System.out.println("未找到 player_aaaa 变量，或变量中无 url 属性");
        }
        return "";
    }

    /**
     * 解析网页，获取影片列表
     * @return 影片列表（Movie 集合）
     */
    public static List<Bean> parseMovieList(Context context,int start,int end) {
        List<Bean> movieList2 = new ArrayList<>();
        for (int count = start; count < end; count++) {
            movieList2.addAll(parseMovieList1(count));
            for (int i = 0; i < movieList2.size(); i++) {
                Bean bean = movieList2.get(i);
                parseDetailPage(bean);
                String m3u8 = MovieParser.parseM3u8FromPlayPage(bean.a2);
                bean.m3u8 = unescapeM3u8Url(m3u8) ;
            }
            List<MovieEntity> movieEntities = new ArrayList<>();
            for (Bean bean : movieList2) {
                movieEntities.add(new MovieEntity(bean));
            }
            new Thread(() -> {
                MovieDatabase db = MovieDatabase.getInstance(context);
                db.movieDao().insertAll(movieEntities);
//                Log.d("Database", "数据插入成功，共 " + movieEntities.size() + " 条");
            }).start();
            movieList2.clear();
        }
        return movieList2;
    }

    public static List<Bean> test3() {
        List<Bean> movieList2 = new ArrayList<>();
        try {
            Document document = Jsoup.connect("https://www.jyquan.xyz/j-34396-28-25").get();

            // 3. 定位列表容器：ul#content（id 为 content 的 ul 标签）
//            Element contentUl = document.getElementById("content");
//            if (contentUl == null) {
//                System.out.println("未找到列表容器 ul#content");
//                return movieList2;
//            }
            System.out.println(document.toString());

            // 4. 遍历所有 li 子节点（每个 li 对应一部影片）
            // 遍历所有 li 子节点（每个 li 对应一部影片）
//            Elements movieLis = contentUl.select("li.col-md-2.col-sm-3.col-xs-4"); // 精准匹配 li 的 class，过滤无效节点
//            for (Element li : movieLis) {
//                try {
//                    // 1. 精准获取 a 标签（匹配 class="video-pic loading"，避免其他 a 标签干扰）
//                    Element aTag = li.selectFirst("a.video-pic.loading");
//                    if (aTag == null) {
//                        System.out.println("当前 li 缺少 a.video-pic.loading 标签，跳过：" + li.outerHtml());
//                        continue;
//                    }
//
//                    // 2. 提取标题（优先用 a 标签的 title，备选从 div.title 中获取，双重保障）
//                    String title = aTag.attr("title").trim();
//                    // 备选方案：如果 a 标签 title 为空，从下方 div.title 的 a 标签取
//                    if (title.isEmpty()) {
//                        Element titleATag = li.selectFirst("div.title h5 a");
//                        if (titleATag != null) {
//                            title = titleATag.attr("title").trim();
//                        }
//                    }
//                    if (title.isEmpty()) {
//                        System.out.println("影片标题为空，跳过当前 li");
//                        continue;
//                    }
//
//                    // 3. 提取图片链接（优先 data-original，备选从 style 中截取，避免懒加载导致的空值）
//                    String picRelativeUrl = aTag.attr("data-original").trim();
//                    if (picRelativeUrl.isEmpty()) {
//                        // 从 style="background-image: url('xxx')" 中截取图片地址
//                        String style = aTag.attr("style").trim();
//                        if (style.contains("background-image: url(")) {
//                            // 截取 url() 中的内容，处理双引号/单引号情况
//                            int startIdx = style.indexOf("(") + 1;
//                            int endIdx = style.lastIndexOf(")");
//                            if (startIdx < endIdx) {
//                                picRelativeUrl = style.substring(startIdx, endIdx).replace("\"", "").replace("'", "").trim();
//                            }
//                        }
//                    }
//                    if (picRelativeUrl.isEmpty()) {
//                        System.out.println("图片链接为空，跳过当前 li");
//                        continue;
//                    }
//                    String picFullUrl = BASE_DOMAIN + picRelativeUrl; // 拼接完整 URL
//
//                    // 4. 提取详情页链接（a 标签的 href 属性）
//                    String detailRelativeUrl = aTag.attr("href").trim();
//                    if (detailRelativeUrl.isEmpty()) {
//                        // 备选方案：从下方 div.title 的 a 标签取 href
//                        Element titleATag = li.selectFirst("div.title h5 a");
//                        if (titleATag != null) {
//                            detailRelativeUrl = titleATag.attr("href").trim();
//                        }
//                    }
//                    if (detailRelativeUrl.isEmpty()) {
//                        System.out.println("详情页链接为空，跳过当前 li");
//                        continue;
//                    }
//                    String detailFullUrl = BASE_DOMAIN + detailRelativeUrl; // 拼接完整 URL
//
//                    // 5. 所有数据正常，添加到列表
//                    Bean movie = new Bean();
//                    movie.title = title;
//                    movie.src = picFullUrl;
//                    movie.a = detailFullUrl;
//                    movieList2.add(movie);
//                    count ++;
////                    if (count>2){
////                        return movieList2;
////                    }
////                    System.out.println("解析成功：" + movie.toString());
//                } catch (Exception e) {
//                    // 捕获所有异常，打印完整 li 内容，方便排查问题
//                    System.out.println("单个影片解析失败，li 内容：" + li.outerHtml());
//                    continue; // 跳过异常 li，不影响整体解析
//                }
//            }

        } catch (Exception e) {
            System.out.println("整体解析异常");
        }

        return movieList2;
    }
    public static List<Bean> parseMovieList1(int count2) {
        List<Bean> movieList2 = new ArrayList<>();
        int count = 0;
        System.out.println("解析第"+count2+"页");

        try {
            Document document = Jsoup.connect(TARGET_URL+count2+".html").get();

            // 3. 定位列表容器：ul#content（id 为 content 的 ul 标签）
            Element contentUl = document.getElementById("content");
            if (contentUl == null) {
                System.out.println("未找到列表容器 ul#content");
                return movieList2;
            }

            // 4. 遍历所有 li 子节点（每个 li 对应一部影片）
            // 遍历所有 li 子节点（每个 li 对应一部影片）
            Elements movieLis = contentUl.select("li.col-md-2.col-sm-3.col-xs-4"); // 精准匹配 li 的 class，过滤无效节点
            for (Element li : movieLis) {
                try {
                    // 1. 精准获取 a 标签（匹配 class="video-pic loading"，避免其他 a 标签干扰）
                    Element aTag = li.selectFirst("a.video-pic.loading");
                    if (aTag == null) {
                        System.out.println("当前 li 缺少 a.video-pic.loading 标签，跳过：" + li.outerHtml());
                        continue;
                    }

                    // 2. 提取标题（优先用 a 标签的 title，备选从 div.title 中获取，双重保障）
                    String title = aTag.attr("title").trim();
                    // 备选方案：如果 a 标签 title 为空，从下方 div.title 的 a 标签取
                    if (title.isEmpty()) {
                        Element titleATag = li.selectFirst("div.title h5 a");
                        if (titleATag != null) {
                            title = titleATag.attr("title").trim();
                        }
                    }
                    if (title.isEmpty()) {
                        System.out.println("影片标题为空，跳过当前 li");
                        continue;
                    }

                    // 3. 提取图片链接（优先 data-original，备选从 style 中截取，避免懒加载导致的空值）
                    String picRelativeUrl = aTag.attr("data-original").trim();
                    if (picRelativeUrl.isEmpty()) {
                        // 从 style="background-image: url('xxx')" 中截取图片地址
                        String style = aTag.attr("style").trim();
                        if (style.contains("background-image: url(")) {
                            // 截取 url() 中的内容，处理双引号/单引号情况
                            int startIdx = style.indexOf("(") + 1;
                            int endIdx = style.lastIndexOf(")");
                            if (startIdx < endIdx) {
                                picRelativeUrl = style.substring(startIdx, endIdx).replace("\"", "").replace("'", "").trim();
                            }
                        }
                    }
                    if (picRelativeUrl.isEmpty()) {
                        System.out.println("图片链接为空，跳过当前 li");
                        continue;
                    }
                    String picFullUrl = BASE_DOMAIN + picRelativeUrl; // 拼接完整 URL

                    // 4. 提取详情页链接（a 标签的 href 属性）
                    String detailRelativeUrl = aTag.attr("href").trim();
                    if (detailRelativeUrl.isEmpty()) {
                        // 备选方案：从下方 div.title 的 a 标签取 href
                        Element titleATag = li.selectFirst("div.title h5 a");
                        if (titleATag != null) {
                            detailRelativeUrl = titleATag.attr("href").trim();
                        }
                    }
                    if (detailRelativeUrl.isEmpty()) {
                        System.out.println("详情页链接为空，跳过当前 li");
                        continue;
                    }
                    String detailFullUrl = BASE_DOMAIN + detailRelativeUrl; // 拼接完整 URL

                    // 5. 所有数据正常，添加到列表
                    Bean movie = new Bean();
                    movie.title = title;
                    movie.src = picFullUrl;
                    movie.a = detailFullUrl;
                    movieList2.add(movie);
                    count ++;
//                    if (count>2){
//                        return movieList2;
//                    }
//                    System.out.println("解析成功：" + movie.toString());
                } catch (Exception e) {
                    // 捕获所有异常，打印完整 li 内容，方便排查问题
                    System.out.println("单个影片解析失败，li 内容：" + li.outerHtml());
                    continue; // 跳过异常 li，不影响整体解析
                }
            }

        } catch (Exception e) {
            System.out.println("整体解析异常");
        }

        return movieList2;
    }

    private static String unescapeM3u8Url(String escapedM3u8) {
        if (escapedM3u8 == null || escapedM3u8.isEmpty()) {
            return "";
        }
        // 替换所有反斜杠转义符（\ 需用双反斜杠 \\ 表示，因为 Java 中 \ 本身是转义符）
        return escapedM3u8.replace("\\/", "/");
    }

}
