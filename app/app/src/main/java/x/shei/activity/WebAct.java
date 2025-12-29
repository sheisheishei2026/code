package x.shei.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import x.shei.R;
import x.shei.util.ImmersedUtil;
import x.shei.util.MyWebView;

public class WebAct extends Activity {

    private MyWebView mWebView;
    private View actionbar;
    WebSettings webSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersedUtil.setImmersedMode(this, false);

//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        mWebView.setLayoutParams(params);
        setContentView(R.layout.ui_web);
        mWebView = findViewById(R.id.webview);
        actionbar = findViewById(R.id.actionbar);
        webSettings = mWebView.getSettings();
//
//        mWebView.getSettings().setJavaScriptEnabled(true);
//        //支持javascript
//        mWebView.requestFocus();
//        //触摸焦点起作用mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);//取消滚动条
//        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        //设置允许js弹出alert对话框
//
//        //支持获取手势焦点，输入用户名、密码或其他
//        mWebView.requestFocusFromTouch();

        webSettings.setBlockNetworkImage(false); // 解决图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setJavaScriptEnabled(true);  //支持js
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);  //提高渲染的优先级
//
//        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true);  //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setUserAgentString("Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
//
        webSettings.setDisplayZoomControls(false);  //支持缩放，默认为true。是下面那个的前提。
        webSettings.setSupportZoom(true);  //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置可以缩放
//        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
//
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); //支持内容重新布局
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);  //关闭webview中缓存
        webSettings.setAllowFileAccess(true);  //设置可以访问文件
//        webSettings.setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true);  //支持自动加载图片
//        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
//
        webSettings.setStandardFontFamily("");//设置 WebView 的字体，默认字体为 "sans-serif"
        webSettings.setDefaultFontSize(20);//设置 WebView 字体的大小，默认大小为 16
        webSettings.setMinimumFontSize(12);//设置 WebView 支持的最小字体大小，默认为 8
        webSettings.setDomStorageEnabled(true);

        String url = getIntent().getStringExtra("url");
        Log.e("asd", " url: " + url);
        if (!TextUtils.isEmpty(url)) {
            mWebView.loadUrl(url);
        }else{
            mWebView.loadUrl("https://m.xb84w.net");
        }
//        mWebView.loadUrl("file:///android_asset/index.html");

//load在线
//mWebView.loadUrl("http://www.google.com");
//js访问android，定义接口
//        mWebView.addJavascriptInterface(new JsInteration(), "control");
//设置了Alert才会弹出，重新onJsAlert（）方法return true可以自定义处理信息
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//return super.onJsAlert(view, url, message, result);
                Toast.makeText(WebAct.this, message, Toast.LENGTH_LONG).show();
                return true;
            }

        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                if (url.startsWith(IData.HOST_URL) || url.startsWith("https://pan")) {
                if (url.startsWith("https://pan")) {
                    view.loadUrl(url);
                    return true;
                } else {
                    return false;
                }

            }
        });

//        mWebView.setOnScrollChangedCallback(new MyWebView.OnScrollChangedCallback() {
//            @Override
//            public void onScroll(int dx, int dy) {
//                Log.e("asd", "dy:" + dy);
//                if (dy > 10) {
//                    ValueAnimator valueAnimator = ValueAnimator.ofInt(0, actionbar.getHeight());
//                    valueAnimator.setEvaluator(new FloatEvaluator());
//                    valueAnimator.setDuration(250);
//                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                        @Override
//                        public void onAnimationUpdate(ValueAnimator animation) {
//                            ViewGroup.LayoutParams layoutParams = actionbar.getLayoutParams();
//                            layoutParams.height = (int) animation.getAnimatedValue();
//                            actionbar.setLayoutParams(layoutParams);
//                        }
//                    });
//                    valueAnimator.start();
//                } else {
//                    ValueAnimator valueAnimator = ValueAnimator.ofInt( actionbar.getHeight(),0);
//                    valueAnimator.setEvaluator(new FloatEvaluator());
//                    valueAnimator.setDuration(250);
//                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                        @Override
//                        public void onAnimationUpdate(ValueAnimator animation) {
//                            ViewGroup.LayoutParams layoutParams = actionbar.getLayoutParams();
//                            layoutParams.height = (int) animation.getAnimatedValue();
//                            actionbar.setLayoutParams(layoutParams);
//                        }
//                    });
//                    valueAnimator.start();
//                }
//            }
//        });

    }

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

    public void back(View view) {
        mWebView.goBack();
    }

    public void jump(View view) {
//        Router.goToDetail(this, mWebView.getUrl());
    }
}
