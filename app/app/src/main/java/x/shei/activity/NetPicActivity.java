//package x.shei.activity;
//
//import static x.shei.activity.APPActivity.DataList;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.recyclerview.widget.StaggeredGridLayoutManager;
//
//import x.shei.R;
//import x.shei.adapter.Image2Adapter;
//import x.shei.util.ImmersedUtil;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class NetPicActivity extends BaseActivity {
//
//    private static final int REQUEST_PERMISSION = 100;
//    private static final int REQUEST_DELETE_PERMISSION = 101;
//    private RecyclerView recyclerView;
//    private Image2Adapter imageAdapter;
//    private List<String> imageUris = new ArrayList<>();
//    boolean single = true;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.recyclerview);
//        ImmersedUtil.setImmersedMode(this, false);
//
//        recyclerView = findViewById(R.id.recyclerView);
//        if (single) {
//            recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        } else {
//            StaggeredGridLayoutManager layoutManager =
//                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//            recyclerView.setLayoutManager(layoutManager);
//        }
//        loadImages1();
//    }
//
//    private void loadImages1() {
//        imageUris.clear();
//        imageUris.addAll(DataList);
//        if (imageAdapter == null) {
//            imageAdapter = new Image2Adapter(this, imageUris);
//            recyclerView.setAdapter(imageAdapter);
//        } else {
//            imageAdapter.notifyDataSetChanged();
//        }
//    }
//
//    private void showToast(String message) {
//        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                loadImages1();
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_DELETE_PERMISSION) {
//            if (resultCode == Activity.RESULT_OK) {
//                showToast("删除成功");
//                // 重新加载图片列表
//                imageUris.clear();
//                loadImages1();
//            } else {
//                showToast("删除被取消");
//            }
//        }
//    }
//
//    public void change(View v) {
//        single = !single;
//        if (single) {
//            recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        } else {
//            StaggeredGridLayoutManager layoutManager =
//                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//            recyclerView.setLayoutManager(layoutManager);
//        }
//    }
//}
