package x.shei.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import x.shei.R;
import x.shei.util.ImmersedUtil;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;

public class WebQQ extends Activity {

    private WebView mWebView;
    private WebSettings webSettings;
    private EditText urlEditText;
    private ImageButton refreshButton;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.ui_web_tbs);

        // 初始化视图
        mWebView = findViewById(R.id.forum_context);
        urlEditText = findViewById(R.id.urlEditText);
        refreshButton = findViewById(R.id.refreshButton);

        // 设置WebView
        setupWebView();

        // 设置刷新按钮点击事件
        refreshButton.setOnClickListener(v -> mWebView.reload());

        findViewById(R.id.close).setOnClickListener(v -> finish());

        // 设置输入框点击监听，实现全选
        urlEditText.setOnClickListener(v -> urlEditText.selectAll());

        // 设置输入框动作监听
        urlEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                handleUrlInput(urlEditText.getText().toString());
                return true;
            }
            return false;
        });

        boolean full = getIntent().getBooleanExtra("full",true);
        if (full) {
            findViewById(R.id.pat).setVisibility(View.GONE);
            ImmersedUtil.setImmersedMode(this,false);
        }

        String url = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(url)) {
            urlEditText.setText(url);
            loadUrl(url);
        } else {
//            loadUrl("https://www.jyquan.xyz/j-34396-28-25");
//            loadUrl("https://peizhifei.github.io/you/");
            mWebView.loadUrl("file:///android_asset/learn.html");
        }
    }

    public void select(View v) {
        urlEditText.selectAll();
    }

    private void setupWebView() {
        webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setUserAgentString("Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");

        mWebView.setWebViewClient(new com.tencent.smtt.sdk.WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                urlEditText.setText(url);
            }
        });
    }

    // 处理URL输入
    private void handleUrlInput(String input) {
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, "请输入网址或搜索内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 移除首尾空格
        input = input.trim();

        // 检查是否是有效的网址格式
        if (isValidUrl(input)) {
            loadUrl(input);
        } else {
            // 不是网址，使用百度搜索
            String searchUrl = "https://www.google.com/search?q=" + Uri.encode(input);
            loadUrl(searchUrl);
        }
    }

    // 判断是否是有效的网址
    private boolean isValidUrl(String input) {
        // 如果已经是完整的URL，直接返回true
        if (URLUtil.isValidUrl(input)) {
            return true;
        }

        String domainRegex = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$";
        return input.matches(domainRegex);
    }

    private void loadUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        // 如果不是以http或https开头，自动添加https://
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        mWebView.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }
}
