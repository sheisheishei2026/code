package x.shei.fragment;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import x.shei.activity.FullscreenImageActivity;
import x.shei.R;
import x.shei.db.PlaceItem;
import x.shei.db.PlacesData;
import x.shei.model.PlaceMediaItem;
import x.shei.adapter.MediaPagerAdapter;
import com.google.android.exoplayer2.ExoPlayer;

public class MapFragment extends Fragment {
    private static final String TAG = "MapFragment";
    private MapView mapView;
    private AMap aMap;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private ClipboardManager clipboard;

    // 初始仰角常量
    private static final float INITIAL_CAMERA_TILT = 180f;

    private List<Marker> markers = new ArrayList<>();
    private boolean isFinishing = false;
    private boolean isDestroyed = false;
    private List<String> currentDisplayedIds = new ArrayList<>(); // 添加当前显示的ID列表

    // 修改marker尺寸和样式常量
    private static final int MARKER_WIDTH = 200;
    private static final int MARKER_HEIGHT = 120;
    private static final float MARKER_CORNER_RADIUS = 15f; // 圆角半径
    private static final int TEXT_PADDING = 80; // 增加文字和图片的间距

    private PopupWindow currentPopupWindow;
    private ExoPlayer currentPlayer;  // Add this field to track the current player

//    private AMap.OnMyLocationChangeListener myLocationChangeListener;
//    private LatLng myLocation;

    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;

    private static final String ARG_PLACE_ITEM = "place_item";
    private PlaceItem placeItem;

    public static MapFragment newInstance(PlaceItem placeItem) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLACE_ITEM, placeItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (getArguments() != null) {
            placeItem = (PlaceItem) getArguments().getSerializable(ARG_PLACE_ITEM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // 设置隐私政策
        AMapLocationClient.updatePrivacyShow(requireContext(), true, true);
        AMapLocationClient.updatePrivacyAgree(requireContext(), true);
        MapsInitializer.updatePrivacyShow(requireContext(), true, true);
        MapsInitializer.updatePrivacyAgree(requireContext(), true);

        // 初始化地图
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        // 设置定位按钮点击事件
        ImageButton btnLocation = view.findViewById(R.id.btn_location);
        btnLocation.setVisibility(View.GONE); // 隐藏定位按钮
//        btnLocation.setOnClickListener(v -> startLocation());

        // 检查权限
        if (checkPermissions()) {
            initMap();
        } else {
            requestPermissions();
        }

        return view;
    }

    public void moveTo(PlaceItem placeItem){
        if (placeItem != null) {
            // 如果当前有浮窗显示，先关闭它
            if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
                currentPopupWindow.dismiss();
            }

            // 移动相机到marker位置，保持当前仰角和缩放级别
            CameraPosition currentPosition = aMap.getCameraPosition();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(new LatLng(Double.parseDouble(placeItem.getLatitude()),Double.parseDouble(placeItem.getLongitude())),
                            currentPosition.zoom,  // 保持当前缩放级别
                            currentPosition.tilt,  // 保持当前仰角
                            currentPosition.bearing)  // 保持当前方向
            );
            aMap.animateCamera(cameraUpdate, 500, null);

            // 显示浮窗
            showPlaceDetails(placeItem);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 如果有传入的placeItem，自动显示其marker并移动地图
        moveTo(placeItem);
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();

            // 设置地图UI设置
            aMap.getUiSettings().setZoomControlsEnabled(false); // 隐藏缩放按钮
            aMap.getUiSettings().setMyLocationButtonEnabled(false); // 隐藏定位按钮
            aMap.getUiSettings().setScaleControlsEnabled(false); // 隐藏比例尺
            aMap.getUiSettings().setRotateGesturesEnabled(false); // 禁用旋转手势
            aMap.showBuildings(true); // 显示3D建筑物
            aMap.showMapText(true); // 显示地图文字
            aMap.setMapLanguage("zh"); // 设置地图语言为中文
            aMap.setMapType(AMap.MAP_TYPE_BUS); // 使用普通地图类型以显示3D效果

            // 设置定位蓝点
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
            myLocationStyle.interval(2000);
            myLocationStyle.strokeColor(Color.argb(180, 3, 145, 255));
            myLocationStyle.radiusFillColor(Color.argb(10, 0, 0, 180));
            myLocationStyle.strokeWidth(5);

            // 设置定位蓝点的Style
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setMyLocationEnabled(true);

            // 设置定位监听
//            myLocationChangeListener = location -> {
//                if (location != null) {
//                    myLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                }
//            };
//            aMap.setOnMyLocationChangeListener(myLocationChangeListener);

            // 设置标记点击监听
            aMap.setOnMarkerClickListener(marker -> {
                handleMarkerClick(marker);
                return true;
            });

            // 加载并显示标记点
            loadMarkers();

            // 移动相机到正阳门箭楼位置并设置初始仰角
            LatLng zhengyangmen = new LatLng(39.903179, 116.397755);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(zhengyangmen, 16, INITIAL_CAMERA_TILT, 0)
            );
            aMap.moveCamera(cameraUpdate);
            aMap.showBuildings(true);
            aMap.showMapText(true);
            aMap.setMapType(AMap.MAP_TYPE_BUS);
        }
    }

    private void startLocation() {
        if (locationClient == null) {
            try {
                locationClient = new AMapLocationClient(requireContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            locationOption = new AMapLocationClientOption();

            // 设置定位模式为高精度模式
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

            // 设置定位间隔,单位毫秒,默认为2000ms
//            locationOption.setInterval(2000);

            // 设置定位参数
            locationClient.setLocationOption(locationOption);

            // 设置定位监听
            locationClient.setLocationListener(locationListener);
        }

        // 启动定位
//        locationClient.startLocation();
    }

    private AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
//            if (location != null) {
//                if (location.getErrorCode() == 0) {
//                    // 定位成功回调信息，设置相关消息
//                    myLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                    // 移动到当前位置，保持当前仰角和缩放级别
//                    CameraPosition currentPosition = aMap.getCameraPosition();
//                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(
//                            new CameraPosition(myLocation,
//                                    currentPosition.zoom,  // 保持当前缩放级别
//                                    currentPosition.tilt,  // 保持当前仰角
//                                    currentPosition.bearing)  // 保持当前方向
//                    );
//                    aMap.animateCamera(cameraUpdate, 1000, null);
//                } else {
//                    // 显示错误信息
//                    Toast.makeText(requireContext(),
//                            "定位失败：" + location.getErrorInfo(),
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//            // 停止定位
//            locationClient.stopLocation();
        }
    };

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
            Log.e(TAG, "Error reading JSON file: " + ex.getMessage());
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
                for (PlaceItem place : placesData.items) {
                    // 如果currentDisplayedIds为空，显示所有marker
                    // 否则只显示匹配的marker
                    if (currentDisplayedIds.isEmpty() || currentDisplayedIds.contains(place.id)) {
                        addMarkerToMap(place);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            Toast.makeText(requireContext(), "加载标记点失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addMarkerToMap(PlaceItem place) {
        LatLng position = new LatLng(
                Double.parseDouble(place.latitude),
                Double.parseDouble(place.longitude)
        );

        // 创建marker
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(place.title)
                .snippet(place.price + "\n" + place.time)
                .anchor(0.5f, 0.8f) // 修改锚点位置到底部
                .draggable(false)
                .visible(false); // 初始设置为不可见，等图片加载完成后再显示

        Marker marker = aMap.addMarker(markerOptions);
        marker.setObject(place);
        markers.add(marker);


        // 加载并处理图片
        if (place.img != null && !place.img.isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.mipmap.ic_launcher)
//            .error(R.drawable.error)
                    .override(200, 120);
            Glide.with(requireContext())
                    .asBitmap()
                    .load(place.img)
                    .centerCrop()
                    .apply(requestOptions)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            if (!isFinishing2()) { // 检查Fragment是否正在结束
                                Bitmap processedBitmap = processMarkerBitmap(resource, Color.WHITE);
                                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(processedBitmap);
                                marker.setIcon(icon);
                                marker.setVisible(true); // 图片加载完成后显示marker
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }

        // 添加文本标注
        int textBackgroundColor;
        if (place.type != null && place.type.contains("v")) {
            textBackgroundColor = Color.parseColor("#f7f799");
        } else {
            try {
                int rate = Integer.parseInt(place.rate);
                switch (rate) {
                    case 5:
                        textBackgroundColor = Color.parseColor("#FBE0AE");
                        break;
                    case 4:
                        textBackgroundColor = Color.parseColor("#bdf4c6");
                        break;
                    case 3:
                        textBackgroundColor = Color.parseColor("#daeffc");
                        break;
                    case 2:
                        textBackgroundColor = Color.parseColor("#d8d8d8");
                        break;
                    case 1:
                    case 0:
                        textBackgroundColor = Color.parseColor("#f2f2f2");
                        break;
                    default:
                        textBackgroundColor = Color.WHITE;
                        break;
                }
            } catch (NumberFormatException e) {
                textBackgroundColor = Color.WHITE;
            }
        }

        // 创建气泡背景
        Bitmap bubbleBitmap = createBubbleBackground(place.title, textBackgroundColor);
        BitmapDescriptor bubbleIcon = BitmapDescriptorFactory.fromBitmap(bubbleBitmap);

        // 创建气泡marker
        MarkerOptions bubbleOptions = new MarkerOptions()
                .position(position)
                .icon(bubbleIcon)
                .anchor(0.5f, 0.5f)
                .zIndex(2);

        Marker bubbleMarker = aMap.addMarker(bubbleOptions);
        bubbleMarker.setObject(place); // 设置相同的PlaceItem对象
        markers.add(bubbleMarker); // 添加到markers列表中以便管理
    }

    private Bitmap processMarkerBitmap(Bitmap original, int backgroundColor) {
        // 创建目标Bitmap，增加底部padding为文字预留空间
        Bitmap output = Bitmap.createBitmap(MARKER_WIDTH, MARKER_HEIGHT + TEXT_PADDING, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(backgroundColor);

        Rect rect = new Rect(0, 0, MARKER_WIDTH, MARKER_HEIGHT);
        RectF rectF = new RectF(rect);

        canvas.drawRoundRect(rectF, MARKER_CORNER_RADIUS, MARKER_CORNER_RADIUS, paint);

        // 设置图片混合模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // 计算缩放比例，保持原始图片的宽高比
        float scale;
        float dx = 0, dy = 0;

        if (original.getWidth() * MARKER_HEIGHT > MARKER_WIDTH * original.getHeight()) {
            scale = (float) MARKER_HEIGHT / original.getHeight();
            dx = (MARKER_WIDTH - original.getWidth() * scale) / 2;
        } else {
            scale = (float) MARKER_WIDTH / original.getWidth();
            dy = (MARKER_HEIGHT - original.getHeight() * scale) / 2;
        }

        // 创建缩放矩阵
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dy);

        // 绘制缩放后的图片
        canvas.drawBitmap(original, matrix, paint);
        return output;
    }

    private Bitmap createBubbleBackground(String text, int backgroundColor) {
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(40);
        textPaint.setColor(Color.parseColor("#555555"));

        // 测量文本宽度
        float textWidth = textPaint.measureText(text);
        int padding = 20; // 左右内边距
        int height = 80; // 减小气泡高度，减少底部padding

        // 创建位图
        Bitmap bitmap = Bitmap.createBitmap((int) (textWidth + padding * 2), height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 绘制背景
        Paint bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(backgroundColor);

        // 绘制圆角矩形
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        float radius = 200f; // 圆角半径
        canvas.drawRoundRect(rect, radius, radius, bgPaint);

        // 绘制文本，调整Y坐标使文字更靠下
        float textX = padding;
        float textY = height / 2.5f + textPaint.getTextSize() / 2f; // 调整文字垂直位置，使其更靠下
        canvas.drawText(text, textX, textY, textPaint);

        return bitmap;
    }

    private void handleMarkerClick(Marker marker) {
        PlaceItem place = (PlaceItem) marker.getObject();
        if (place != null) {
            // 如果当前有浮窗显示，先关闭它
            if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
                currentPopupWindow.dismiss();
            }

            // 移动相机到marker位置，保持当前仰角和缩放级别
            CameraPosition currentPosition = aMap.getCameraPosition();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(marker.getPosition(),
                            currentPosition.zoom,  // 保持当前缩放级别
                            currentPosition.tilt,  // 保持当前仰角
                            currentPosition.bearing)  // 保持当前方向
            );
            aMap.animateCamera(cameraUpdate, 500, null);

            // 显示浮窗
            showPlaceDetails(place);
        }
    }

    private void showPlaceDetails(PlaceItem place) {
        // 如果有正在显示的弹窗，先关闭它
        if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
            currentPopupWindow.dismiss();
            // Release the current player if it exists
            if (currentPlayer != null) {
                currentPlayer.release();
                currentPlayer = null;
            }
        }

        View infoWindow = getLayoutInflater().inflate(R.layout.marker_info_window, null);

//        ImageView placeImage = infoWindow.findViewById(R.id.place_image);
        TextView placeTitle = infoWindow.findViewById(R.id.place_title);
        FlexboxLayout labelContainer = infoWindow.findViewById(R.id.label_container);
        TextView placeRate = infoWindow.findViewById(R.id.place_rate);

        // 获取星级评分控件
        ImageView[] stars = new ImageView[5];
        stars[0] = infoWindow.findViewById(R.id.star1);
        stars[1] = infoWindow.findViewById(R.id.star2);
        stars[2] = infoWindow.findViewById(R.id.star3);
        stars[3] = infoWindow.findViewById(R.id.star4);
        stars[4] = infoWindow.findViewById(R.id.star5);

        View priceContainer = infoWindow.findViewById(R.id.price_container);
        View cameraContainer = infoWindow.findViewById(R.id.camera_container);
        View timeContainer = infoWindow.findViewById(R.id.time_container);
        View bookingContainer = infoWindow.findViewById(R.id.booking_container);
        View guideTimeContainer = infoWindow.findViewById(R.id.guide_time_container);

        TextView placePrice = infoWindow.findViewById(R.id.place_price);
        TextView placeCamera = infoWindow.findViewById(R.id.place_camera);
        TextView placeTime = infoWindow.findViewById(R.id.place_time);
        TextView placeBooking = infoWindow.findViewById(R.id.place_booking);
        TextView placeGuideTime = infoWindow.findViewById(R.id.place_guide_time);

        Button btnView = infoWindow.findViewById(R.id.btn_view);
        Button btnView2 = infoWindow.findViewById(R.id.btn_view2);
        Button btnView3 = infoWindow.findViewById(R.id.btn_view3);
        Button btnView4 = infoWindow.findViewById(R.id.btn_view4);

        ViewPager2 viewPager = infoWindow.findViewById(R.id.viewPager);
        TabLayout tabLayout = infoWindow.findViewById(R.id.tabLayout);

        // 准备媒体数据
        List<PlaceMediaItem> mediaItems = new ArrayList<>();

        // 如果有视频，先添加视频
        if (place.video != null && !place.video.isEmpty()) {
            mediaItems.add(new PlaceMediaItem(place.video, place.img, true));
        }

        // 添加主图片
        if (place.img != null && !place.img.isEmpty()) {
            mediaItems.add(new PlaceMediaItem(place.img, false));
        }

        // 设置适配器
        MediaPagerAdapter adapter = new MediaPagerAdapter(mediaItems, item -> {
            if (item.isVideo()) {
                // 视频点击事件现在在播放器内部处理
                return;
            } else {
                // 处理图片点击
                Intent intent = new Intent(requireContext(), FullscreenImageActivity.class);
                intent.putExtra("image_url", item.getUrl());
                startActivity(intent);
            }
        });

        viewPager.setAdapter(adapter);

        // 设置TabLayout
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText("")
        ).attach();

        placeTitle.setText(place.title);

        // 处理推荐标签
        if (!TextUtils.isEmpty(place.recomend)) {
            labelContainer.setVisibility(View.VISIBLE);
            String[] labels = place.recomend.split("，");
            for (String label : labels) {
                TextView tagView = new TextView(requireContext());
                tagView.setText(label.trim());
                tagView.setTextSize(12);
                tagView.setTextColor(Color.parseColor("#666666"));
                tagView.setBackgroundResource(R.drawable.tag_background);
                tagView.setPadding(16, 6, 16, 6);

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(8, 4, 8, 4);
                tagView.setLayoutParams(params);

                labelContainer.addView(tagView);
            }
        } else {
            labelContainer.setVisibility(View.GONE);
        }

        // 设置评分和星级显示
        float rating = Float.parseFloat(place.rate);
        placeRate.setText(place.rate + "分");

        // 设置星星
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_empty);
            }
        }

        // 设置其他信息
        if (place.price != null && !place.price.isEmpty()) {
            priceContainer.setVisibility(View.VISIBLE);
            try {
                float price = Float.parseFloat(place.price);
                if (price == 0) {
                    placePrice.setText("免费");
                } else {
                    placePrice.setText(place.price + "元");
                }
            } catch (NumberFormatException e) {
                placePrice.setText(place.price);
            }
        }

        if (place.camera != null && !place.camera.isEmpty()) {
            cameraContainer.setVisibility(View.VISIBLE);
            placeCamera.setText(place.camera);
        }

        if (place.time != null && !place.time.isEmpty()) {
            timeContainer.setVisibility(View.VISIBLE);
            placeTime.setText(place.time);
            timeContainer.setOnClickListener(v -> {
                Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            });
        }

        if (place.go != null && !place.go.isEmpty()) {
            bookingContainer.setVisibility(View.VISIBLE);
            placeBooking.setText(place.go);
            bookingContainer.setOnClickListener(v -> {
                Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            });
        }

        if (place.voice != null && !place.voice.isEmpty()) {
            guideTimeContainer.setVisibility(View.VISIBLE);
            placeGuideTime.setText(place.voice);
            guideTimeContainer.setOnClickListener(v -> {
                Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            });
        }

        // 设置查看位置按钮点击事件
        btnView.setOnClickListener(v -> {
            try {
                if (isPackageInstalled("com.autonavi.minimap")) {
                    Intent intent = new Intent("android.intent.action.VIEW",
                            Uri.parse("androidamap://viewMap?sourceApplication=appname&lat=" +
                                    place.latitude + "&lon=" + place.longitude + "&poiname=" + place.title));
                    intent.setPackage("com.autonavi.minimap");
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "请安装高德地图或腾讯地图", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "打开失败，请检查应用是否正确安装", Toast.LENGTH_SHORT).show();
            }
        });

        btnView2.setOnClickListener(v -> {
            try {
                String uri = "qqmap://map/search?keyword=" + place.title;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.tencent.map");
                intent.setData(Uri.parse(uri));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "打开失败，请检查应用是否正确安装", Toast.LENGTH_SHORT).show();
            }
        });
        btnView3.setOnClickListener(v -> {
            try {
                ClipData clip = ClipData.newPlainText("text",  place.title);
                clipboard.setPrimaryClip(clip);

                String packageName = "com.sankuai.meituan";
                String className = "com.sankuai.meituan.search.home.SearchActivity";
                Intent intent = new Intent();
                intent.setClassName(packageName, className);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "打开失败，请检查应用是否正确安装", Toast.LENGTH_SHORT).show();
            }
        });

        btnView4.setOnClickListener(v -> {
            try {
                ClipData clip = ClipData.newPlainText("text",  place.title);
                clipboard.setPrimaryClip(clip);

                String packageName = "com.dianping.v1";
                String className = "com.dianping.v1.NovaMainActivity";
//                String className = "com.dianping.nova.picasso.DPPicassoBoxActivity";
                Intent intent = new Intent();
                intent.setClassName(packageName, className);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "打开失败，请检查应用是否正确安装", Toast.LENGTH_SHORT).show();
            }
        });

        // 创建并显示PopupWindow
        PopupWindow popupWindow = new PopupWindow(infoWindow,
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(false);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);

        // Add dismiss listener to properly release the player
        popupWindow.setOnDismissListener(() -> {
            if (adapter.getCurrentPlayer() != null) {
                adapter.getCurrentPlayer().release();
                currentPlayer = null;
            }
        });

        // 计算底部间距（5%屏幕高度）
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int bottomMargin = (int) (screenHeight * 0.1);

        // 显示在底部中心位置
        popupWindow.showAtLocation(mapView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0, bottomMargin);

        // 保存当前显示的弹窗引用
        currentPopupWindow = popupWindow;
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            requireContext().getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        // 关闭可能存在的弹窗
        if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
            currentPopupWindow.dismiss();
        }
        // Release the player when fragment is paused
        if (currentPlayer != null) {
            currentPlayer.release();
            currentPlayer = null;
        }
        isFinishing = true;
    }

    @Override
    public void onDestroy() {
        try {
            isDestroyed = true;

            // 停止定位
            if (locationClient != null) {
                locationClient.stopLocation();
                locationClient.onDestroy();
                locationClient = null;
            }

            // 移除定位监听
            if (aMap != null) {
                aMap.setOnMyLocationChangeListener(null);
            }

            // 清除所有标记
            if (markers != null) {
                for (Marker marker : markers) {
                    if (marker != null) {
                        marker.remove();
                        marker.destroy();
                    }
                }
                markers.clear();
            }

            // 清除地图缓存
            if (aMap != null) {
                aMap.clear();
                aMap = null;
            }

            // 销毁地图视图
            if (mapView != null) {
                mapView.onDestroy();
                mapView = null;
            }

            // 清除图片加载缓存
//            if (!isFinishing()) {
//                Glide.get(requireContext()).clearMemory();
//            }

            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
            super.onDestroy();
        }
    }

    private boolean isFinishing2() {
        return isFinishing || isDestroyed;
    }

    private boolean checkPermissions() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                initMap();
            } else {
                Toast.makeText(requireContext(), "需要相关权限才能使用地图功能", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 添加新方法：根据ID列表显示对应的marker
    public void showMarkersByIds(List<String> ids) {
        if (aMap == null || markers == null) return;

        // 保存当前显示的ID列表
        currentDisplayedIds = new ArrayList<>(ids);

        // 隐藏所有marker
        for (Marker marker : markers) {
            marker.setVisible(false);
        }

        // 显示匹配的marker
        for (Marker marker : markers) {
            PlaceItem place = (PlaceItem) marker.getObject();
            if (place != null && ids.contains(place.id)) {
                marker.setVisible(true);
            }
        }

        // 如果有显示的marker，调整地图视野以显示所有marker
        if (!ids.isEmpty()) {
            adjustMapBounds(ids);
        }
    }

    // 添加新方法：调整地图视野以显示所有指定的marker
    private void adjustMapBounds(List<String> ids) {
        List<LatLng> points = new ArrayList<>();
        for (Marker marker : markers) {
            PlaceItem place = (PlaceItem) marker.getObject();
            if (place != null && ids.contains(place.id)) {
                points.add(marker.getPosition());
            }
        }

        if (!points.isEmpty()) {
            // 计算边界
            double minLat = points.get(0).latitude;
            double maxLat = points.get(0).latitude;
            double minLng = points.get(0).longitude;
            double maxLng = points.get(0).longitude;

            for (LatLng point : points) {
                minLat = Math.min(minLat, point.latitude);
                maxLat = Math.max(maxLat, point.latitude);
                minLng = Math.min(minLng, point.longitude);
                maxLng = Math.max(maxLng, point.longitude);
            }

            // 添加一些边距
            double latPadding = (maxLat - minLat) * 0.1;
            double lngPadding = (maxLng - minLng) * 0.1;

            // 创建边界
            LatLngBounds bounds = new LatLngBounds(
                    new LatLng(minLat - latPadding, minLng - lngPadding),
                    new LatLng(maxLat + latPadding, maxLng + lngPadding)
            );

            // 移动相机到边界
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
    }
}
