package x.shei.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import x.shei.R;
import x.shei.adapter.Ed2kAdapter;
import x.shei.db.DataObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Ed2kFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view =  inflater.inflate(R.layout.recyclerview, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        if (single) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            GridLayoutManager layoutManager =
                    new GridLayoutManager(getActivity(),4);
            recyclerView.setLayoutManager(layoutManager);
        }
        loadImages1();
        return view;
    }

    private RecyclerView recyclerView;
    private Ed2kAdapter imageAdapter;
    private List<DataObject> imageUris = new ArrayList<>();
    boolean single = false;


    private void loadImages1() {
        new Thread(() -> {
            List<DataObject> tempList = readDataFromAsset(getActivity(), "ed2k.txt");
            getActivity().runOnUiThread(() -> {
                imageUris.clear();
                imageUris.addAll(tempList);
                if (imageAdapter == null) {
                    imageAdapter = new Ed2kAdapter(getActivity(), imageUris);
                    recyclerView.setAdapter(imageAdapter);
                } else {
                    imageAdapter.notifyDataSetChanged();
                }
            });
        }).start();
    }

    public static List<DataObject> readDataFromAsset(Context context, String fileName) {
        List<DataObject> dataList = new ArrayList<>();
        BufferedReader reader = null;
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 1) {
                    DataObject dataObject = new DataObject(line);
                    dataList.add(dataObject);
                } else if (parts.length > 1) {
                    String x = parts[2].replace("[www.mp4us.com]","")
                            .replace("【www.xbw20.com】","")
                            .replace("【www.xbw20.net】","")
                            .replace("【www.xb84w.com】","")
                            .replace("【www.xb84w.net】","");
                    DataObject dataObject = new DataObject(line, x);
                    dataList.add(dataObject);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataList;
    }
}
