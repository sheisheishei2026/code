package x.shei.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import x.shei.R;
import x.shei.adapter.LocationCategoryAdapter;
import x.shei.db.LocationCategory;
import x.shei.db.PlaceItem;
import x.shei.db.PlacesData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LocationListFragment extends Fragment {
    private RecyclerView recyclerView;
    private LocationCategoryAdapter adapter;
    private List<PlaceItem> allPlaces;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化数据
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_location_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 加载所有地点数据
        loadPlacesData();

        // 创建分类数据
        List<LocationCategory> categories = createCategories();

        // 为每个分类匹配地点数据
        for (LocationCategory category : categories) {
            List<PlaceItem> matchedPlaces = category.getNav().stream()
                    .map(id -> findPlaceById(String.valueOf(id)))
                    .filter(place -> place != null)
                    .collect(Collectors.toList());
            category.setNavList(matchedPlaces);
        }

        adapter = new LocationCategoryAdapter(getActivity(), categories);
        recyclerView.setAdapter(adapter);
        return view;
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("places.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.e("asd", "Error reading JSON file: " + ex.getMessage());
            return null;
        }
        return json;
    }

    private void loadPlacesData() {
        try {
            String jsonData = loadJSONFromAsset();
            if (jsonData == null) {
                Toast.makeText(getActivity(), "无法加载地点数据", Toast.LENGTH_SHORT).show();
                return;
            }
           PlacesData placesData = new Gson().fromJson(jsonData, PlacesData.class);

            if (placesData != null && placesData.items != null) {
                allPlaces = placesData.items;
            }

            if (allPlaces == null || allPlaces.isEmpty()) {
                Toast.makeText(getActivity(), "地点数据为空", Toast.LENGTH_SHORT).show();
                allPlaces = new ArrayList<>();
            } else {
                Log.d("LocationList", "成功加载 " + allPlaces.size() + " 个地点");
            }
        } catch (Exception e) {
            Log.e("LocationList", "加载地点数据失败: " + e.getMessage());
            Toast.makeText(getActivity(), "加载地点数据失败", Toast.LENGTH_SHORT).show();
            allPlaces = new ArrayList<>();
        }
    }

    private PlaceItem findPlaceById(String id) {
        if (allPlaces == null) return null;
        return allPlaces.stream()
                .filter(place -> place.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private List<LocationCategory> createCategories() {
        List<LocationCategory> categories = new ArrayList<>();


        categories.add(new LocationCategory(
                "天安门景区",
                Arrays.asList(58, 59, 61, 170, 106, 1, 155),
                "天安门景区"
        ));


        categories.add(new LocationCategory(
                "前门景区",
                Arrays.asList(56, 482, 415, 588, 587, 551, 555, 554, 69, 452, 454),
                "前门景区"
        ));


        categories.add(new LocationCategory(
                "牛街景区",
                Arrays.asList(402, 12, 546, 403, 603),
                "牛街景区"
        ));
        categories.add(new LocationCategory(
                "后海景区",
                Arrays.asList(84,179,180,72,95,96,496,430,71),
                "后海景区"
        ));
        categories.add(new LocationCategory(
                "奥林匹克景区",
                Arrays.asList(109,220,219,24,17,221,149),
                "奥林匹克景区"
        ));
        categories.add(new LocationCategory(
                "奥林匹克博物馆群",
                Arrays.asList(7,201,8,48,19),
                "奥林匹克博物馆群"
        ));

        categories.add(new LocationCategory(
                "西直门景区",
                Arrays.asList(28,27,168,100,35,239,172,99),
                "西直门景区"
        ));
        categories.add(new LocationCategory(
                "海淀景区",
                Arrays.asList(51,164,52,169,161),
                "海淀景区"
        ));
        categories.add(new LocationCategory(
                "国贸景区",
                Arrays.asList(248,249,313,314,372,498),
                "国贸景区"
        ));

        categories.add(new LocationCategory(
                "通州一日游",
                Arrays.asList(204,207,11,238,182,687,195,196),
                "通州一日游"
        ));
        categories.add(new LocationCategory(
                "香山植物园一日游",
                Arrays.asList(171,593,557,558,83,282,175,137),
                "香山植物园一日游"
        ));
        categories.add(new LocationCategory(
                "西四CityWalk",
                Arrays.asList(134,103,663,104,115,42,245,518,563,118),
                "西四CityWalk"
        ));
        categories.add(new LocationCategory(
                "雍和宫CityWalk",
                Arrays.asList(157,117,97,451,630,373),
                "雍和宫CityWalk"
        ));

        categories.add(new LocationCategory(
                "北京工会卡",
                Arrays.asList(106,103,105,101,102,100,99,97,98),
                "北京工会卡"
        ));

        categories.add(new LocationCategory(
                "和家人一起去",
                Arrays.asList(60,61,33,633,44,617,328,62,253,333,185,249,50,213,130,634,161,411,142,95,434,73,396,648,118,202,354,385,384,177),
                "和家人一起去"
        ));

        // 北京值得拍照的地铁
        categories.add(new LocationCategory(
                "和家人一起去",
                Arrays.asList(60,61,33,633,44,617,328,62,253,333,185,249,50,213,130,634,161,411,142,95,434,73,396,648,118,202,354,385,384,177),
                "和家人一起去"
        ));

        categories.add(new LocationCategory(
                "看表演，沉浸式体验",
                Arrays.asList(12, 130, 213,272,273,660,95,269,270,271,206,435,639,671),
                "看表演，沉浸式体验"
        ));

        // 拍照绝美的地方
        categories.add(new LocationCategory(
                "拍照绝美的地方",
                Arrays.asList(209, 208, 187, 206, 337, 95, 116, 138, 142, 194, 161, 178, 166, 118, 119,306, 333, 373, 516, 481, 484, 482, 1, 106),
                "拍照绝美"
        ));

        // 看城墙，摸砖瓦
        categories.add(new LocationCategory(
                "夜景美丽",
                Arrays.asList(487,261,95,180,219,260,220,221,253,262,272,273,329,330,628,505,660,213),
                "夜景美丽"
        ));
        // 看城墙，摸砖瓦
        categories.add(new LocationCategory(
                "影视拍摄地",
                Arrays.asList(179,166,337,1,169,193,263,372,418,266,552),
                "影视拍摄地"
        ));
        // 看城墙，摸砖瓦
        categories.add(new LocationCategory(
                "看城墙，摸砖瓦",
                Arrays.asList(65, 91, 174, 160, 163, 173, 175, 317, 56, 552, 61),
                "看城墙"
        ));

        // 看巨幕，球幕，天幕，地幕
        categories.add(new LocationCategory(
                "看巨幕，球幕，天幕，地幕",
                Arrays.asList(27, 260, 8, 16, 19, 18, 48, 7),
                "看巨幕"
        ));

        // 看表演，听讲解，沉浸式游览
        categories.add(new LocationCategory(
                "听讲解，沉浸式游览",
                Arrays.asList(2, 3, 7, 4, 5, 6, 1,  8, 9, 10, 11, 16, 18, 19, 20, 23, 24, 26, 28, 29, 31, 32, 34, 35, 43, 44, 45, 50, 51, 52, 56, 61, 63, 66, 76, 77, 92, 93, 95, 97, 98, 99, 100, 101, 102, 105, 133, 134, 135, 137, 156, 175, 179, 203, 205, 209 , 283, 373, 437, 438, 442, 459, 460, 464, 12, 130, 213),
                "讲解表演"
        ));

        // 可以畅玩的博物馆，互动体验棒
        categories.add(new LocationCategory(
                "可以畅玩的博物馆，互动体验棒",
                Arrays.asList(24, 4, 8, 11, 16, 36, 48, 50, 66),
                "互动乐园"
        ));

        // 动物乐园，恐龙，海洋，标本
        categories.add(new LocationCategory(
                "动物乐园，恐龙，海洋，标本",
                Arrays.asList(83, 86, 87, 28, 84, 150, 26, 31, 42, 85, 168, 176, 269, 270, 271, 177, 272, 273, 5, 178),
                "动物乐园"
        ));

        // 看飞机大炮，火车轮船，导弹坦克，机枪武器
        categories.add(new LocationCategory(
                "看飞机大炮，火车轮船，导弹坦克，机枪武器",
                Arrays.asList(224, 23, 25, 22, 31, 36, 45, 50, 88, 89, 11, 4, 193, 307),
                "车船机炮"
        ));

        // 北京城建都考古历史专题展馆
        categories.add(new LocationCategory(
                "北京城建都考古历史专题展馆",
                Arrays.asList(1, 3, 10, 9, 54, 383, 443, 450, 459),
                "北京历史"
        ));

        // 爬长城，有山有水有长城
        categories.add(new LocationCategory(
                "有山有水有长城",
                Arrays.asList(326, 329, 330, 327, 57, 447),
                "长城"
        ));

        // 逛教堂，去拍照
        categories.add(new LocationCategory(
                "逛教堂，去拍照",
                Arrays.asList(118, 119, 120, 121, 122, 244, 209, 449),
                "逛教堂"
        ));

        // 看看北京中轴线上景观
        categories.add(new LocationCategory(
                "看看北京中轴线上景观",
                Arrays.asList(98, 163, 1, 56, 61, 95, 96, 156, 552),
                "中轴线"
        ));

        // 逛逛中轴线延伸奥林匹克风景区
        categories.add(new LocationCategory(
                "逛逛中轴线延伸奥林匹克风景区",
                Arrays.asList(149, 181, 221, 219, 220, 7, 8, 16, 17, 19, 24, 33, 201, 48, 109),
                "奥林匹克"
        ));

        // 逛寺庙，既是寺庙，也是博物馆
        categories.add(new LocationCategory(
                "逛寺庙，既是寺庙，也是博物馆",
                Arrays.asList(111, 112, 113, 114, 115, 343, 407, 409, 109, 320, 403, 101, 102, 104, 105, 107, 108, 110, 319, 12, 97, 98, 99, 100, 103, 106, 116, 117, 137, 156),
                "寺庙博物馆"
        ));

        // 逛大学和大学里的博物馆
        categories.add(new LocationCategory(
                "逛大学和大学里的博物馆",
                Arrays.asList(51, 52, 53, 67, 68, 77, 78, 82, 85, 86, 87, 88, 92, 93, 94, 189, 286, 287, 288, 289, 290, 291, 293, 294, 297, 298, 344, 350, 392, 462, 463, 464, 465, 466, 467, 468, 469, 470),
                "大学景点"
        ));

        // 去美术馆，看看画展
        categories.add(new LocationCategory(
                "去美术馆，看看画展",
                Arrays.asList(186, 188, 189, 194, 196, 197, 198, 201, 203, 209, 210, 211, 226, 246, 274, 339, 438, 204, 473, 475),
                "逛美术馆"
        ));

        // 逛胡同，感受生活风情
        categories.add(new LocationCategory(
                "逛胡同，感受生活风情",
                Arrays.asList(71, 72, 73, 135, 136, 430, 449, 451, 452, 454, 500, 501, 504),
                "逛胡同"
        ));

        // 逛名人故居，感受四合院
        categories.add(new LocationCategory(
                "逛名人故居，感受四合院",
                Arrays.asList(38, 49, 76, 123, 125, 126, 127, 128, 129, 130, 131, 132, 133, 135, 136, 179, 245, 404, 434, 437, 439, 479, 483),
                "名人故居"
        ));

        // 逛皇家园林，祭祀坛庙
        categories.add(new LocationCategory(
                "逛皇家园林，祭祀坛庙",
                Arrays.asList(137, 156, 160, 170, 169, 164, 306, 155, 98, 106),
                "皇家园林"
        ));

        // 看民俗文化，回味过去
        categories.add(new LocationCategory(
                "看民俗文化，回味过去",
                Arrays.asList(218, 185, 184, 3, 201, 9, 108, 41, 12, 47, 400, 135, 136),
                "民俗文化"
        ));

        // 逛逛北京最美最有艺术气息的商场
        categories.add(new LocationCategory(
                "逛逛北京最美最有艺术气息的商场",
                Arrays.asList(249, 372, 384, 385, 412, 246, 247, 250, 252, 260, 261, 263, 264, 280, 304),
                "艺术商场"
        ));

        // 读书，去书苑，图书馆
        categories.add(new LocationCategory(
                "读书，去书苑，图书馆",
                Arrays.asList(243, 340, 375, 376, 377, 404, 244, 245, 338, 361, 238, 240, 241, 242, 441, 239, 518),
                "去书苑"
        ));

        // 去看雕塑，欣赏艺术
        categories.add(new LocationCategory(
                "去看雕塑，欣赏艺术",
                Arrays.asList(108, 141, 151, 190, 193, 195, 204, 211, 214, 216, 296, 316, 178, 142),
                "雕塑艺术"
        ));

        // 离开市中心，去五环外看看
        categories.add(new LocationCategory(
                "离开市中心，去五环外看看",
                Arrays.asList(44, 53, 82, 83, 84, 86, 137, 165, 171, 175, 185, 194, 197, 198, 200, 204, 212, 264, 267, 273, 282, 283, 295, 296, 297, 298, 301, 306, 318, 319, 320, 333, 334, 337, 342, 353, 354, 355, 356, 357, 358, 368, 369, 382, 427, 460, 464, 465),
                "五环外"
        ));

        // 去北京郊区看看壮丽山河
        categories.add(new LocationCategory(
                "去北京郊区看看壮丽山河",
                Arrays.asList(11, 45, 57, 81, 105, 176, 177, 182, 195, 196, 207, 238, 257, 280, 281, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 335, 336, 444, 445, 446, 447, 448, 450, 455, 456, 457, 458),
                "六环外"
        ));

        // 办张公园年卡，经常逛逛公园
        categories.add(new LocationCategory(
                "办张公园年卡，经常逛逛公园",
                Arrays.asList(137, 138, 139, 140, 141, 155, 156, 157, 166, 167, 168, 169, 170, 171, 220),
                "公园年卡"
        ));

        // 逛逛医学相关的博物馆
        categories.add(new LocationCategory(
                "逛逛医学相关的博物馆",
                Arrays.asList(77, 80, 79, 75, 464),
                "医学相关"
        ));

        // 去拍白塔
        categories.add(new LocationCategory(
                "去拍白塔",
                Arrays.asList(167, 103, 116, 100, 210),
                "白塔"
        ));

        // 去看园林建筑，欣赏建筑艺术
        categories.add(new LocationCategory(
                "去看园林建筑，欣赏建筑艺术",
                Arrays.asList(178, 44, 33, 617),
                "园林建筑"
        ));

        // 室内游览，地铁直达，适合冬天
        categories.add(new LocationCategory(
                "室内游览，地铁直达，适合冬天",
                Arrays.asList(2, 4, 5, 16, 17, 35, 42, 41, 82, 202, 204, 206),
                "适合冬天"
        ));

        return categories;
    }

}
