package x.shei.fragment;

import static x.shei.util.PlaceDataLoader.loadJSONFromAsset;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import x.shei.R;
import x.shei.adapter.PlaceListAdapter;
import x.shei.adapter.FilterCategoryAdapter;
import x.shei.db.PlaceItem;
import x.shei.db.PlacesData;
import x.shei.util.FilterCategories;

public class PlaceListFragment extends Fragment {
    private EditText searchEditText;
    private Button btnSort;
    private Button btnFilter;
    private RecyclerView recyclerView;
    private PlaceListAdapter adapter;
    private List<PlaceItem> placeList = new ArrayList<>();
    private List<PlaceItem> filteredList = new ArrayList<>();
    private View sortPanel;
    private View filterPanel;
    private View filterheader;
    private RecyclerView rvAreaCategories;
    private RecyclerView rvFunctionCategories;
    private RecyclerView rvTypeCategories;
    private FilterCategoryAdapter areaAdapter;
    private FilterCategoryAdapter functionAdapter;
    private FilterCategoryAdapter typeAdapter;
    private boolean isSortPanelVisible = false;
    private boolean isFilterPanelVisible = false;

    // Add sorting state fields
    private String currentSortOption = "";
    private boolean isPriceAscending = false;
    private boolean isRatingAscending = false;
    private boolean isDistanceAscending = false;
    private String currentFilter = "";  // Track current filter

    // Add scroll tracking fields
    private int lastScrollY = 0;
    private boolean isControlsVisible = true;
    private static final int SCROLL_THRESHOLD = 20;
    private static final int ANIMATION_DURATION = 200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_list, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        // 初始化视图
        recyclerView = view.findViewById(R.id.recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
        btnSort = view.findViewById(R.id.btn_sort);
        btnFilter = view.findViewById(R.id.btn_filter);
        sortPanel = view.findViewById(R.id.sort_panel);
        filterPanel = view.findViewById(R.id.filter_panel);
        filterheader = view.findViewById(R.id.filter_header);

        // 初始化排序选项
        TextView tvSortByPrice = view.findViewById(R.id.tv_sort_by_price);
//        TextView tvSortByDistance = view.findViewById(R.id.tv_sort_by_distance);
        TextView tvSortByRating = view.findViewById(R.id.tv_sort_by_rating);

        // 初始化筛选列表
        rvAreaCategories = view.findViewById(R.id.rv_area_categories);
        rvFunctionCategories = view.findViewById(R.id.rv_function_categories);
        rvTypeCategories = view.findViewById(R.id.rv_type_categories);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlaceListAdapter(placeList);
        recyclerView.setAdapter(adapter);

        // 设置筛选列表
        setupFilterLists();

        // 搜索框监听
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPlaces(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 排序按钮点击事件
        btnSort.setOnClickListener(v -> toggleSortPanel());

        // 筛选按钮点击事件
        btnFilter.setOnClickListener(v -> toggleFilterPanel());

        // 排序选项点击事件
        tvSortByPrice.setOnClickListener(v -> {
            btnSort.setText("按价格排序");
            sortPlacesByPrice();
            hideSortPanel();
        });

//        tvSortByDistance.setOnClickListener(v -> {
//            btnSort.setText("按距离排序");
//            sortPlacesByDistance();
//            hideSortPanel();
//        });

        tvSortByRating.setOnClickListener(v -> {
            btnSort.setText("按评分排序");
            sortPlacesByRating();
            hideSortPanel();
        });

        // Add scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Hide/show controls based on scroll direction
                if (Math.abs(dy) > SCROLL_THRESHOLD) {
                    if (dy > 0 && isControlsVisible) {
                        // Scrolling down, hide controls
                        hideControls();
                    } else if (dy < 0 && !isControlsVisible) {
                        // Scrolling up, show controls
                        showControls();
                    }
                }

                lastScrollY = dy;
            }
        });

        loadMarkers();
    }


    private void hideControls() {
//        if (isControlsVisible) {
//            isControlsVisible = false;
//            filterheader.animate()
//                    .translationY(-filterheader.getHeight())
//                    .setDuration(ANIMATION_DURATION)
//                    .start();
//        }
    }

    private void showControls() {
//        if (!isControlsVisible) {
//            isControlsVisible = true;
//            filterheader.animate()
//                    .translationY(0)
//                    .setDuration(ANIMATION_DURATION)
//                    .start();
//        }
    }




    private void setupFilterLists() {
        // 设置布局管理器
        rvAreaCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFunctionCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTypeCategories.setLayoutManager(new LinearLayoutManager(getContext()));

        // 创建适配器
        areaAdapter = new FilterCategoryAdapter(requireContext(),
            FilterCategories.AREA_CATEGORIES, FilterCategories.COLOR_AREA);
        functionAdapter = new FilterCategoryAdapter(requireContext(),
            FilterCategories.FUNCTION_CATEGORIES, FilterCategories.COLOR_FUNCTION);
        typeAdapter = new FilterCategoryAdapter(requireContext(),
            FilterCategories.TYPE_CATEGORIES, FilterCategories.COLOR_TYPE);

        // 设置点击监听
        areaAdapter.setOnItemClickListener((position, category) -> {
            Log.d("Filter", "Selected Area: " + category);
            btnFilter.setText(category);
            filterPlacesByType(category);
            hideFilterPanel();
        });

        functionAdapter.setOnItemClickListener((position, category) -> {
            Log.d("Filter", "Selected Function: " + category);
            btnFilter.setText(category);
            filterPlacesByType(category);
            hideFilterPanel();
        });

        typeAdapter.setOnItemClickListener((position, category) -> {
            Log.d("Filter", "Selected Type: " + category);
            btnFilter.setText(category);
            filterPlacesByType(category);
            hideFilterPanel();
        });

        // 设置适配器
        rvAreaCategories.setAdapter(areaAdapter);
        rvFunctionCategories.setAdapter(functionAdapter);
        rvTypeCategories.setAdapter(typeAdapter);
    }

    private void toggleSortPanel() {
        if (isSortPanelVisible) {
            hideSortPanel();
        } else {
            showSortPanel();
        }
    }

    private void toggleFilterPanel() {
        if (isFilterPanelVisible) {
            hideFilterPanel();
        } else {
            showFilterPanel();
        }
    }

    private void showSortPanel() {
        hideFilterPanel();
        sortPanel.setVisibility(View.VISIBLE);
        isSortPanelVisible = true;
        showControls();
    }

    private void hideSortPanel() {
        sortPanel.setVisibility(View.GONE);
        isSortPanelVisible = false;
    }

    private void showFilterPanel() {
        hideSortPanel();
        filterPanel.setVisibility(View.VISIBLE);
        isFilterPanelVisible = true;
        showControls();
    }

    private void hideFilterPanel() {
        filterPanel.setVisibility(View.GONE);
        isFilterPanelVisible = false;
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = requireContext().getAssets().open("places.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.e("Asd", "Error reading JSON file: " + ex.getMessage());
            return null;
        }
        return json;
    }

    private void loadMarkers() {
        String jsonData = loadJSONFromAsset();
        if (jsonData == null) {
            Toast.makeText(requireContext(), "无法加载地点数据", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Gson gson = new Gson();
            PlacesData placesData = gson.fromJson(jsonData, PlacesData.class);

            if (placesData != null && placesData.items != null) {
                placeList.addAll(placesData.items);
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e("Asd", "Error parsing JSON: " + e.getMessage());
            Toast.makeText(requireContext(), "加载标记点失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterPlaces(String query) {
        filteredList.clear();
        for (PlaceItem place : placeList) {
            if (place.title.toLowerCase().contains(query.toLowerCase()) ||
                    (place.recomend != null && place.recomend.toLowerCase().contains(query.toLowerCase())) ||
                    (place.time != null && place.time.toLowerCase().contains(query.toLowerCase())) ||
                    (place.price != null && place.time.toLowerCase().contains(query.toLowerCase())) ||
                    (place.voice != null && place.voice.toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(place);
            }
        }

        // Apply current sort if any
        if (!currentSortOption.isEmpty()) {
            if (currentSortOption.equals("按价格排序")) {
                filteredList.sort((a, b) -> {
                    float priceA = getPrice(a);
                    float priceB = getPrice(b);
                    return isPriceAscending ? Float.compare(priceA, priceB) : Float.compare(priceB, priceA);
                });
            } else if (currentSortOption.equals("按评分排序")) {
                filteredList.sort((a, b) -> {
                    float ratingA = Float.parseFloat(a.rate);
                    float ratingB = Float.parseFloat(b.rate);
                    return isRatingAscending ? Float.compare(ratingA, ratingB) : Float.compare(ratingB, ratingA);
                });
            }
        }

        adapter.updateData(filteredList);
    }

    private void sortPlacesByPrice() {
        currentSortOption = "按价格排序";
        btnSort.setText(currentSortOption);

        // Use filtered list if there's an active filter, otherwise use the full list
        List<PlaceItem> listToSort = currentFilter.isEmpty() ? placeList : filteredList;

        listToSort.sort((a, b) -> {
            float priceA = getPrice(a);
            float priceB = getPrice(b);
            return isPriceAscending ? Float.compare(priceA, priceB) : Float.compare(priceB, priceA);
        });

        isPriceAscending = !isPriceAscending;
        adapter.updateData(listToSort);
        hideSortPanel();
    }

    private void sortPlacesByDistance() {
        currentSortOption = "按距离排序";
        btnSort.setText(currentSortOption);

        List<PlaceItem> listToSort = filteredList.isEmpty() ? placeList : filteredList;
        // TODO: Implement distance calculation and sorting
        // For now, just update the UI
        adapter.updateData(listToSort);
        hideSortPanel();
    }

    private void sortPlacesByRating() {
        currentSortOption = "按评分排序";
        btnSort.setText(currentSortOption);

        // Use filtered list if there's an active filter, otherwise use the full list
        List<PlaceItem> listToSort = currentFilter.isEmpty() ? placeList : filteredList;

        listToSort.sort((a, b) -> {
            float ratingA = Float.parseFloat(a.rate);
            float ratingB = Float.parseFloat(b.rate);
            return isRatingAscending ? Float.compare(ratingA, ratingB) : Float.compare(ratingB, ratingA);
        });

        isRatingAscending = !isRatingAscending;
        adapter.updateData(listToSort);
        hideSortPanel();
    }

    private float getPrice(PlaceItem item) {
        try {
            if (!TextUtils.isEmpty(item.pricenum)) {
                return Float.parseFloat(item.pricenum);
            } else if (!TextUtils.isEmpty(item.price)) {
                // Extract numeric value from price string
                String priceStr = item.price.replaceAll("[^0-9.]", "");
                if (!TextUtils.isEmpty(priceStr)) {
                    return Float.parseFloat(priceStr);
                }
            }
            return 0f;
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    private void filterPlacesByArea(String area) {
        Log.d("Filter", "Filtering by area: " + area);
        List<PlaceItem> filteredList = new ArrayList<>();
        for (PlaceItem place : placeList) {
            if (place.title != null && place.title.equals(area)) {
                filteredList.add(place);
            }
        }
        adapter.updateData(filteredList);
    }

    private void filterPlacesByFunction(String function) {
        Log.d("Filter", "Filtering by function: " + function);
        List<PlaceItem> filteredList = new ArrayList<>();
        for (PlaceItem place : placeList) {
            if (place.title != null && place.title.equals(function)) {
                filteredList.add(place);
            }
        }
        adapter.updateData(filteredList);
    }

    private void filterPlacesByType(String type) {
        Log.d("Filter", "Filtering by type: " + type);
        currentFilter = type;  // Update current filter

        filteredList.clear();
        for (PlaceItem place : placeList) {
            boolean matches = false;

            try {
                if (type.equals("全部")) {
                    matches = true;
                } else if (type.equals("有视频") && !TextUtils.isEmpty(place.video)) {
                    matches = true;
                } else if (type.equals("有VR") && place.recomend != null && place.recomend.contains("VR")) {
                    matches = true;
                } else if (type.equals("5A级景区") && place.recomend != null && place.recomend.contains("5A")) {
                    matches = true;
                } else if (type.equals("持证") && place.price != null && place.price.contains("持证")) {
                    matches = true;
                } else if (type.equals("换票") && place.price != null && place.price.contains("换票")) {
                    matches = true;
                } else if (type.equals("运动健身") && (
                        (place.type != null && place.type.contains("p")) ||
                        (place.recomend != null && (place.recomend.contains("肌肉力量") || place.recomend.contains("体育")))
                )) {
                    matches = true;
                } else if (type.contains("夜景") && place.recomend != null && place.recomend.contains("夜景")) {
                    matches = true;
                } else if (type.contains("四合院") && place.type != null && place.type.contains("q")) {
                    matches = true;
                } else if (type.contains("看巨幕") && place.recomend != null && place.recomend.contains("幕")) {
                    matches = true;
                } else if (type.contains("去书苑") && place.type != null && place.type.contains("e")) {
                    matches = true;
                } else if (type.contains("民俗文化") && place.type != null && place.type.contains("o")) {
                    matches = true;
                } else if (type.contains("爬长城") && place.title != null && place.title.contains("长城")) {
                    matches = true;
                } else if (type.contains("逛美术馆") && place.title != null && place.recomend != null &&
                        (place.title.contains("美术") || place.recomend.contains("美术"))) {
                    matches = true;
                } else if (type.contains("逛胡同") && place.title != null && place.recomend != null &&
                        (place.title.contains("胡同") || place.recomend.contains("胡同"))) {
                    matches = true;
                } else if (type.contains("名人故居") && place.title != null && place.recomend != null &&
                        (place.title.contains("故居") || place.recomend.contains("故居") ||
                         place.title.contains("纪念馆") || place.recomend.contains("纪念馆"))) {
                    matches = true;
                } else if (type.contains("逛教堂") && place.title != null && place.recomend != null &&
                        (place.title.contains("教堂") || place.recomend.contains("教堂"))) {
                    matches = true;
                } else if (type.contains("五环外") && place.longitude != null && place.latitude != null) {
                    double longitude = Double.parseDouble(place.longitude);
                    double latitude = Double.parseDouble(place.latitude);
                    if (longitude > 116.543421 || longitude < 116.211053 ||
                        latitude > 40.021542 || latitude < 39.777469) {
                        matches = true;
                    }
                } else if (type.contains("六环外") && place.longitude != null && place.latitude != null) {
                    double longitude = Double.parseDouble(place.longitude);
                    double latitude = Double.parseDouble(place.latitude);
                    if (longitude > 116.706554 || longitude < 116.139816 ||
                        latitude > 40.158419 || latitude < 39.72192) {
                        matches = true;
                    }
                } else if (type.contains("夏日时光") && place.recomend != null &&
                        (place.recomend.contains("肌肉") || place.recomend.contains("游泳"))) {
                    matches = true;
                } else if (type.contains("秋日银杏") && place.recomend != null &&
                        (place.recomend.contains("秋天") || place.recomend.contains("银杏"))) {
                    matches = true;
                } else if (type.contains("地铁直达") && place.recomend != null &&
                        place.recomend.contains("地铁直达")) {
                    matches = true;
                } else if (type.equals("工作日开") && place.time != null &&
                        place.time.contains("工作日开放")) {
                    matches = true;
                } else if (type.equals("需要预约") && place.recomend != null &&
                        place.recomend.contains("需预约")) {
                    matches = true;
                } else if (type.equals("讲解表演") && !TextUtils.isEmpty(place.voice)) {
                    matches = true;
                } else if (type.equals("人文·历史·社会") && place.type != null && place.type.contains("a"))  {
                    matches = true;
                } else if (type.equals("艺术·展馆·书苑") && place.type != null && place.type.contains("b"))  {
                    matches = true;
                } else if (type.equals("行业·科学·技术") && place.type != null && place.type.contains("c"))  {
                    matches = true;
                } else if (type.equals("寺庙·坛观·宗教") && place.type != null && place.type.contains("d"))  {
                    matches = true;
                } else if (type.equals("学院·大学·景点") && place.type != null && place.type.contains("f"))  {
                    matches = true;
                } else if (type.equals("商场·商圈·零售") && place.type != null && place.type.contains("g"))  {
                    matches = true;
                } else if (type.equals("园区·大厦·酒店") && place.type != null && place.type.contains("h"))  {
                    matches = true;
                } else if (type.equals("公园·山水·自然") && place.type != null && place.type.contains("i"))  {
                    matches = true;
                } else if (type.equals("街道·美食·小吃") && place.type != null && place.type.contains("k"))  {
                    matches = true;
                } else if (type.equals("三星以上") && !TextUtils.isEmpty(place.rate)) {
                    float rating = Float.parseFloat(place.rate);
                    if (rating > 2) {
                        matches = true;
                    }
                } else if (type.equals("小吃饭店") && place.type != null && place.type.contains("v")) {
                    matches = true;
                } else if (type.equals("有公众号") && !TextUtils.isEmpty(place.officalId)) {
                    matches = true;
                } else if (type.equals("有小程序") && !TextUtils.isEmpty(place.appId)) {
                    matches = true;
                } else if (type.equals("收费")) {
                    float price = getPrice(place);
                    if (price > 0) {
                        matches = true;
                    }
                } else if (type.equals("免费")) {
                    float price = getPrice(place);
                    if (price == 0) {
                        matches = true;
                    }
                }
            } catch (Exception e) {
                Log.e("Filter", "Error filtering place: " + e.getMessage());
            }

            if (matches) {
                filteredList.add(place);
            }
        }

        // If there's an active sort, apply it to the filtered list
        if (!currentSortOption.isEmpty()) {
            if (currentSortOption.equals("按价格排序")) {
                filteredList.sort((a, b) -> {
                    float priceA = getPrice(a);
                    float priceB = getPrice(b);
                    return isPriceAscending ? Float.compare(priceA, priceB) : Float.compare(priceB, priceA);
                });
            } else if (currentSortOption.equals("按评分排序")) {
                filteredList.sort((a, b) -> {
                    float ratingA = Float.parseFloat(a.rate);
                    float ratingB = Float.parseFloat(b.rate);
                    return isRatingAscending ? Float.compare(ratingA, ratingB) : Float.compare(ratingB, ratingA);
                });
            }
        }

        adapter.updateData(filteredList);
    }

    private void resetFilter() {
        currentFilter = "";  // Clear current filter
        filteredList.clear();
        adapter.updateData(placeList);
    }
}
