package man.who.scan.my.app.die.a.mother.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.HashMap;

import androidx.core.content.ContextCompat;
import man.who.scan.my.app.die.a.mother.MainActivity;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.ui.utils.JavascriptInterface;
import man.who.scan.my.app.die.a.mother.ui.utils.WebTools;
import me.jingbin.web.ByWebTools;
import me.jingbin.web.ByWebView;
import me.jingbin.web.OnByWebClientCallback;
import me.jingbin.web.OnTitleProgressCallback;

/**
 * 网页可以处理:
 * 点击相应控件：
 * - 进度条显示
 * - 上传图片(版本兼容)
 * - 全屏播放网络视频
 * - 唤起微信支付宝
 * - 拨打电话、发送短信、发送邮件
 * - 返回网页上一层、显示网页标题
 * JS交互部分：
 * - 前端代码嵌入js(缺乏灵活性)
 * - 网页自带js跳转
 * 被作为第三方浏览器打开
 *
 * @author jingbin
 * link to https://github.com/youlookwhat/ByWebView
 */
public class ByWebViewActivity extends Activity {

    // 网页链接
    private int mState;
    private String mUrl;
    private String mFromUrl;
    private String mTitle;
    private WebView webView;
    private ByWebView byWebView;
    private TextView tvGunTitle;
    public static String versionName;
    private static HashMap<String, String> headers;

    public static  HashMap<String, String> getHeaders(){
        return headers;
    }
    public ByWebView getByWebView(){
        return byWebView;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_by_webview);
        getIntentData();
        initTitle();
        getDataFromBrowser(getIntent());
    }

    private void getIntentData() {
        mUrl = getIntent().getStringExtra("url");
        mFromUrl = getIntent().getStringExtra("fromUrl");
        mTitle = getIntent().getStringExtra("title");
        mState = getIntent().getIntExtra("state", 0);
    }

    private void initTitle() {
        //StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.colorPrimary), 0);
        initToolBar();
        LinearLayout container = findViewById(R.id.ll_container);
        if(headers == null){
            headers = new HashMap<>();
            versionName = getVersionName(this);
            headers.put("x-requested-with", "tomato." + versionName);
            headers.put("user-agent", "ninja");  // user-agent 小写生效
        }
        byWebView = ByWebView
                .with(this)
                .setWebParent(container, new LinearLayout.LayoutParams(-1, -1))
                .useWebProgress(ContextCompat.getColor(this, R.color.coloRed))
                .setOnTitleProgressCallback(onTitleProgressCallback)
                .setOnByWebClientCallback(onByWebClientCallback)
                .addJavascriptInterface("tomato_bridge", new JavascriptInterface(this))
                .setHeaders(headers)
                .loadUrl(mUrl);
        webView = byWebView.getWebView();
    }

    private String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private void initToolBar() {
        // 可滚动的title 使用简单 没有渐变效果，文字两旁有阴影
        Toolbar mTitleToolBar = findViewById(R.id.title_tool_bar);
        tvGunTitle = findViewById(R.id.tv_gun_title);
        setActionBar(mTitleToolBar);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            //去除默认Title显示
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTitleToolBar.setNavigationIcon(R.drawable.icon_back);
        mTitleToolBar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.actionbar_more));
        tvGunTitle.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvGunTitle.setSelected(true);
            }
        }, 1900);
        tvGunTitle.setText(mTitle);
    }

    private OnTitleProgressCallback onTitleProgressCallback = new OnTitleProgressCallback() {
        @Override
        public void onReceivedTitle(String title) {
            Log.e("---title", title);
            tvGunTitle.setText(title);
        }
    };

    private OnByWebClientCallback onByWebClientCallback = new OnByWebClientCallback() {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.e("---onPageStarted", url);
        }

        @Override
        public boolean onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // 如果自己处理，需要返回true
            return super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // 网页加载完成后的回调
//            if (mState == 1) {
//                loadImageClickJs();
//                loadTextClickJs();
//                loadWebsiteSourceCodeJs();
//            } else if (mState == 2) {
//                loadCallJs();
//            }
        }

        @Override
        public boolean isOpenThirdApp(String url) {
            // 处理三方链接
            //Log.e("---url", url);
            //return ByWebTools.handleThirdApp(ByWebViewActivity.this, url);
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// 返回键
                handleFinish();
                break;
            case R.id.actionbar_share:// 分享到
                String shareText = webView.getTitle() + webView.getUrl();
                WebTools.share(ByWebViewActivity.this, shareText);
                break;
            case R.id.actionbar_cope:// 复制链接
                WebTools.copy(ByWebViewActivity.this, webView.getUrl());
                Toast.makeText(this, "复制成功", Toast.LENGTH_LONG).show();
                break;
            case R.id.actionbar_open:// 打开链接
                WebTools.openLink(ByWebViewActivity.this, webView.getUrl());
                break;
            case R.id.actionbar_webview_refresh:// 刷新页面
                byWebView.reload();
                break;
            case R.id.actionbar_webview_copy_cookies:// 复制当前页面cookie
                //byWebView.reload();
                byWebView.getLoadJsHolder().loadJs("javascript:window.tomato_bridge.copy(document.cookie)");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 上传图片之后的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        byWebView.handleFileChooser(requestCode, resultCode, intent);
    }

    /**
     * 使用singleTask启动模式的Activity在系统中只会存在一个实例。
     * 如果这个实例已经存在，intent就会通过onNewIntent传递到这个Activity。
     * 否则新的Activity实例被创建。
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getDataFromBrowser(intent);
    }

    /**
     * 作为三方浏览器打开传过来的值
     * Scheme: https
     * host: www.jianshu.com
     * path: /p/1cbaf784c29c
     * url = scheme + "://" + host + path;
     */
    private void getDataFromBrowser(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            try {
                String scheme = data.getScheme();
                String host = data.getHost();
                String path = data.getPath();
                String text = "Scheme: " + scheme + "\n" + "host: " + host + "\n" + "path: " + path;
                Log.e("data", text);
                String url = scheme + "://" + host + path;
                byWebView.loadUrl(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 直接通过三方浏览器打开时，回退到首页
     */
    public void handleFinish() {
        //TODO
//        supportFinishAfterTransition();
//        if (!MainActivity.isLaunch) {
//            MainActivity.start(this);
//        }
        finishAfterTransition();
        if(mFromUrl == null || mFromUrl.startsWith("file")){
            this.startActivity(new Intent(this, MainActivity.class));
        }else{
            loadUrl(this, "file:///android_asset/browser/index.html", "Tomato", 0, mUrl);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (byWebView.handleKeyEvent(keyCode, event)) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                handleFinish();
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        byWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        byWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        byWebView.onDestroy();
        super.onDestroy();
    }

    /**
     * 打开网页:
     *
     * @param mContext 上下文
     * @param url      要加载的网页url
     * @param title    标题
     * @param state    类型
     */
    public static void loadUrl(Context mContext, String url, String title, int state, String fromUrl) {
        Intent intent = new Intent(mContext, ByWebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("state", state);
        intent.putExtra("mFromUrl", fromUrl);
        intent.putExtra("title", title == null ? "加载中..." : title);
        mContext.startActivity(intent);
    }
}
