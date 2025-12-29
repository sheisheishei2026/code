package x.shei.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import x.shei.R;
import x.shei.adapter.PanAdapter;
import x.shei.db.DataObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PanFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recyclerview, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        if (single) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
//            StaggeredGridLayoutManager layoutManager =
//                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            GridLayoutManager layoutManager =
                    new GridLayoutManager(getActivity(),4);
            recyclerView.setLayoutManager(layoutManager);
        }
        loadImages1();
        return view;
    }

    private static final int REQUEST_PERMISSION = 100;
    private static final int REQUEST_DELETE_PERMISSION = 101;
    private RecyclerView recyclerView;
    private PanAdapter imageAdapter;
    private List<DataObject> imageUris = new ArrayList<>();
    boolean single = false;


    private void loadImages1() {
        new Thread(() -> {
            List<DataObject> tempList = readDataFromAsset(getActivity(), "pan.txt");
            getActivity().runOnUiThread(() -> {
                imageUris.clear();
                imageUris.addAll(tempList);
                if (imageAdapter == null) {
                    imageAdapter = new PanAdapter(getActivity(), imageUris);
                    recyclerView.setAdapter(imageAdapter);
                } else {
                    imageAdapter.notifyDataSetChanged();
                }
            });
        }).start();
    }

    private void showToast(String message) {
        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
    }

    public static List<DataObject> readDataFromAsset(Context context, String fileName) {
        List<DataObject> dataList = new ArrayList<>();
        BufferedReader reader = null;
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    DataObject dataObject = new DataObject(parts[0], parts[1].replace(" ",""));
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
