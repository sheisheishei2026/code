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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import x.shei.db.DataObject;
import x.shei.R;
import x.shei.adapter.YouAdapter;
import x.shei.db.PlaceItem;
import x.shei.db.PlacesData;

public class ScenicFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.activity_you, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.entranceRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
//        recyclerView.setPadding(recyclerView.getPaddingLeft(),
//                getStatusBarHeight() + recyclerView.getPaddingTop(),
//                recyclerView.getPaddingRight(),
//                recyclerView.getPaddingBottom());

        YouAdapter adapter = new YouAdapter(getActivity(), dataObjectList);
        recyclerView.setAdapter(adapter);
        loadMarkers();
        return view;
    }
    static List<DataObject> dataObjectList = new ArrayList<>();

    static {
        dataObjectList.add(new DataObject("206",
                "https://www.chncpa.org/ncpa_virtualtour/"));
        dataObjectList.add(new DataObject("4",
                "http://3d.jb.mil.cn/90zn/index.html"));
        dataObjectList.add(new DataObject("199",
                "https://www.720yun.com/vr/e28jz0tyuO5"));
        dataObjectList.add(new DataObject("169",
                "https://www.720yun.com/vr/028j55hazw9"));
        dataObjectList.add(new DataObject("179",
                "https://pano.pgm.org.cn/#/panorama?panorama_id=81&scene_id=3769&scene_name=scene_3769_autumn"));
        dataObjectList.add(new DataObject("182",
                "https://www.canalmuseum.org.cn/yzl.html"));
        dataObjectList.add(new DataObject("66",
                "https://www.cfm119.com/digitalmuseum/index.html"));
        dataObjectList.add(new DataObject("131",
                "http://4dmodel.com/SuperTwoCustom/SongQingLinVtour/tour.html"));
        dataObjectList.add(new DataObject("57",
                "https://pano.taagoo.com/panos/bdlccvrdjzbpt0817/badaling/welcome.html?from=singlemessage"));
        dataObjectList.add(new DataObject("63",
                "http://360.70.tibetol.cn/?scene_id=1&g_id=23919&c_id=23920"));
        dataObjectList.add(new DataObject("10",
                "https://fhac.com.cn/online_exhibition.html"));
        dataObjectList.add(new DataObject("6",
                "https://www.720yun.com/vr/8ecjOgwvem1"));
        dataObjectList.add(new DataObject("42",
                "https://my.matterportvr.cn/show/?m=HxyLfv6Witq&%3Blang=cn"));
        dataObjectList.add(new DataObject("13",
                "https://m.eqxiu.com/s/kbESm9Xn?adpop=1"));
        dataObjectList.add(new DataObject("201",
                "https://panorama.gmfyg.org.cn/"));
        dataObjectList.add(new DataObject("43",
                "http://www.cnm.com.cn/zgqbbwg/qj/html/index.html?scene_id=67740143"));
        dataObjectList.add(new DataObject("44",
                "http://yungarden.gardensmuseum.cn/navigation.jspx"));
        dataObjectList.add(new DataObject("14",
                "http://yungarden.gardensmuseum.cn/navigation.jspx"));
        dataObjectList.add(new DataObject("104",
                "https://h5.weizhan.gmw.cn/vr/bjlddwm/#/"));
        dataObjectList.add(new DataObject("23",
                "http://www.caacmuseum.cn/MUSEUM_VR/TwoAirlinesUprising/"));
        dataObjectList.add(new DataObject("16",
                "https://www.bjsc.net.cn/kxzxvr/kxgc.html"));
        dataObjectList.add(new DataObject("127",
                "https://www.kuleiman.com/137110/index.html"));
        dataObjectList.add(new DataObject("29",
                "http://www.1937china.com/views/kzzl/zlzs_xnzt.html"));
        dataObjectList.add(new DataObject("137",
                "https://h5.btime.com/Page/jjgk"));
        dataObjectList.add(new DataObject("2",
                "https://www.chnmuseum.cn/Portals/0/web/vr/"));
        dataObjectList.add(new DataObject("3",
                "https://m.capitalmuseum.org.cn/wx/page/#/panorama"));
        dataObjectList.add(new DataObject("32",
                "http://zhuanti.cpon.cn/vrgb/?scene_id=68074463#scene_id=68074463"));
        dataObjectList.add(new DataObject("5",
                "https://vr1.nnhm.org.cn/sceneFront/index.html?G_TEMP_ID=3d457cb207004b8e8689ceee3bd3e33a&skinType=2"));

        dataObjectList.add(new DataObject("187",
                "https://marketing.markor.com.cn/h5/m99VR?activityNo=8888&project=m99vr1010"));
        dataObjectList.add(new DataObject("40",
                "https://www.720yun.com/t/226jz0yyrm8?from=singlemessage&pano_id=6137923#scene_id=8727031"));
        dataObjectList.add(new DataObject("108",
                "https://dym.com.cn/bwg/index.html"));
        dataObjectList.add(new DataObject("27",
                "https://www.bjp.org.cn/fullview1/fv_b1/app.html"));
        dataObjectList.add(new DataObject("48",
                "https://xnmy.cdstm.cn/vr/65921/"));
        dataObjectList.add(new DataObject("30",
                "https://www.720yun.com/t/92vktmqlsfb#scene_id=75406041"));
//        dataObjectList.add(new DataObject("16",
//                "https://cc8m144elzi.720yun.com/t/74vkO7dy7qb#scene_id=55381501"));
        dataObjectList.add(new DataObject("85",
                "https://cc8m144elzi.720yun.com/vr/43ejOgknsf2"));
        dataObjectList.add(new DataObject("293",
                "https://exhibition.xsg.tsinghua.edu.cn/3d/"));

//        dataObjectList.add(new DataObject("军事博物馆基本陈列飞机大炮",
//                "http://3d.jb.mil.cn/bqcl/plane/index.html"));
//        dataObjectList.add(new DataObject("中国共产党领导的革命战争陈列",
//                "http://3d.jb.mil.cn/gming/index.html"));
//        dataObjectList.add(new DataObject("中国历代军事陈列",
//                "http://3d.jb.mil.cn/lidai/index.html#"));

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

    private void loadMarkers() {
        String jsonData = loadJSONFromAsset();
        if (jsonData == null) {
            Toast.makeText(getActivity(), "无法加载地点数据", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            Gson gson = new Gson();
           PlacesData placesData = gson.fromJson(jsonData, PlacesData.class);

            if (placesData != null && placesData.items != null) {
                for (PlaceItem item : placesData.items) {
                    for (DataObject dataObject : dataObjectList) {
                        if (item.id.equals(dataObject.getPart1())) {
                            dataObject.img = item.img;
                            dataObject.title = item.title;
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e("asd", "Error parsing JSON: " + e.getMessage());
            Toast.makeText(getActivity(), "加载标记点失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
