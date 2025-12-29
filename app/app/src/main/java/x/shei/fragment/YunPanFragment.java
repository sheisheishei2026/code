package x.shei.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alicloud.databox.opensdk.AliyunpanAction;
import com.alicloud.databox.opensdk.AliyunpanBroadcastHelper;
import com.alicloud.databox.opensdk.AliyunpanClient;
import com.alicloud.databox.opensdk.scope.AliyunpanFileScope;
import com.alicloud.databox.opensdk.scope.AliyunpanUserScope;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import x.shei.App;
import x.shei.AuthLoginActivity;
import x.shei.adapter.FileListAdapter;
import x.shei.activity.FullscreenImageActivity;
import x.shei.R;
import x.shei.util.ViewHelper;

public class YunPanFragment extends Fragment {
    View  view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main3, container, false);
        this.view = view;
        AliyunpanBroadcastHelper.registerReceiver(getActivity(), receive);
        TextView tvResult = view.findViewById(R.id.tvResult);

        view.findViewById(R.id.btnOAuth).setOnClickListener(v -> startOAuth(tvResult));
        view.findViewById(R.id.clearUserInfo).setOnClickListener(v -> clearOAuth());
        view. findViewById(R.id.btnUserInfo).setOnClickListener(v -> getDriveInfo(tvResult));

        recyclerView = view.findViewById(R.id.recyclerView);
//        StaggeredGridLayoutManager layoutManager =
//                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new FileListAdapter(getActivity(), fileInfoList, new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Map<String, Object> fileInfo) {
                String fileType = (String) fileInfo.get("type");
                String url = (String) fileInfo.get("url");
                String thumbnail = (String) fileInfo.get("thumbnail");
                Log.e("asd", "fileType:" + fileType);
                Log.e("asd", "url:" + url);
                Log.e("asd", "thumbnail:" + thumbnail);

                if ("folder".equals(fileType)) {
                    String fileId = (String) fileInfo.get("file_id");
                    getFileList("1", fileId);
                    Log.e("asd", "fileId:" + fileId);
                } else {
                    // 如果是图片类型，跳转到大图浏览
                    Intent intent = new Intent(getActivity(), FullscreenImageActivity.class);
                    intent.putExtra("image_url", url);
                    startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        getFileList("1", fileId);
//        getFileList("1", "root");
        view.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                change(view);
            }
        });
        return view;
    }

    private RecyclerView recyclerView;
    private FileListAdapter adapter;
    private List<Map<String, Object>> fileInfoList = new ArrayList<>();

    private final BroadcastReceiver receive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            String message = intent.getStringExtra("message");

            switch (AliyunpanAction.valueOf(action)) {
                case NOTIFY_LOGIN_SUCCESS:
                     view.findViewById(R.id.test).setVisibility(View.GONE);
                    getFileList("1", fileId);
                    break;
                case NOTIFY_LOGOUT:
                    Toast.makeText(context, "登录状态失效", Toast.LENGTH_SHORT).show();
                {
                    Intent authIntent = new Intent(context, AuthLoginActivity.class);
                    authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(authIntent);
                }
                break;
                case NOTIFY_LOGIN_CANCEL:
                    Toast.makeText(context, "授权取消 message=" + message, Toast.LENGTH_SHORT).show();
                    break;
                case NOTIFY_LOGIN_FAILED:
                    Toast.makeText(context, "授权失败 message=" + message, Toast.LENGTH_SHORT).show();
                    break;
                case NOTIFY_RESET_STATUS:
                    Toast.makeText(context, "授权状态重置", Toast.LENGTH_SHORT).show();
                    Intent authIntent = new Intent(context, AuthLoginActivity.class);
                    authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(authIntent);
                    break;
            }
        }
    };

    public static String defaultDriveId;
    private List<String> fileIdList;
    private String fileId = "67b604a4859c217eeef54ed9affa08002daabf7f";


    private void getFileList(String driveId, String parentFileId) {
        AliyunpanClient aliyunpanClient = App.aliyunpanClient;
        if (aliyunpanClient == null) {
            return;
        }
        aliyunpanClient.send(new AliyunpanFileScope.GetFileList(
//                        driveId == null? "" : driveId,
                                     "618529513",
                                     100,
                                     "",
                                     "name",
                                     "DESC",
                                     parentFileId,
                                     "video,doc,audio,image",
                                     "all",
                                     12000l,
                                     480l,
                                     480l,
                                     "*"
                             ) {
                                 @Override
                                 public String getUrl() {
                                     return null;
                                 }

                                 @Override
                                 public JSONObject getBody() {
                                     return null;
                                 }
                             },
                result -> {
                    JSONArray items = result.getData().asJSONObject().optJSONArray("items");
//                        android.util.Log("asd",items.toString());
                    List<Map<String, Object>> newFileInfoList = new ArrayList<>();

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject itemJsonObject = items.optJSONObject(i);
                        String fileId = itemJsonObject.optString("file_id");
                        String fileName = itemJsonObject.optString("name");
                        long fileSize = itemJsonObject.optLong("size", 0);
                        String fileType = itemJsonObject.optString("type", "unknown");
                        String fileUrl = itemJsonObject.optString("url", "unknown");

                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("file_id", fileId);
                        fileInfo.put("name", fileName);
                        fileInfo.put("size", fileSize);
                        fileInfo.put("type", fileType);
                        fileInfo.put("url", fileUrl);
                        newFileInfoList.add(fileInfo);
                    }

                    fileInfoList.addAll(newFileInfoList);
                    adapter.notifyDataSetChanged();
                }, error -> {
                });
    }

    private void clearOAuth() {
        AliyunpanClient aliyunpanClient = App.aliyunpanClient;
        if (aliyunpanClient == null) {
            return;
        }
        aliyunpanClient.clearOauth();
    }

    private void startOAuth(TextView tvResult) {
        AliyunpanClient aliyunpanClient = App.aliyunpanClient;
        if (aliyunpanClient == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            aliyunpanClient.oauth(
                    unused -> {
                        ViewHelper.appendWithTime(tvResult, "oauth start");
                    },
                    // 处理失败的情况
                    error -> {
                        ViewHelper.appendWithTime(tvResult, "oauth failed: " + error);
                    });
        }
    }

    private void getDriveInfo(TextView tvResult) {
        AliyunpanClient aliyunpanClient = App.aliyunpanClient;
        if (aliyunpanClient == null) {
            return;
        }
        aliyunpanClient.send(new AliyunpanUserScope.GetDriveInfo() {
                                 @Override
                                 public String getUrl() {
                                     return null;
                                 }

                                 @Override
                                 public JSONObject getBody() {
                                     return null;
                                 }
                             },
                result -> {
                    ViewHelper.appendWithTime(tvResult, "GetDriveInfo success: " + result);
                    defaultDriveId = result.getData().asJSONObject().optString("default_drive_id");
                },
                error -> ViewHelper.appendWithTime(tvResult, "GetDriveInfo failed: " + error));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AliyunpanBroadcastHelper.unregisterReceiver(getActivity(), receive);
    }

    boolean single= true;

    public void change(View v) {
//        View view = findViewById(R.id.test2);
//        if (view.getVisibility() == View.VISIBLE) {
//            view.setVisibility(View.GONE);
//        } else {
//            view.setVisibility(View.VISIBLE);
//        }

        single = !single;
        if (single) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            StaggeredGridLayoutManager layoutManager =
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
        }

    }
}
