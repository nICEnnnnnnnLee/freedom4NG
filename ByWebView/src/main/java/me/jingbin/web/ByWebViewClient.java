package me.jingbin.web;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

import androidx.annotation.RequiresApi;

/**
 * Created by jingbin on 2020/06/30
 * 监听网页链接:
 * - 根据标识:打电话、发短信、发邮件
 * - 进度条的显示
 * - 添加javascript监听
 * - 唤起京东，支付宝，微信原生App
 */
public class ByWebViewClient extends WebViewClient {

    private WeakReference<Activity> mActivityWeakReference = null;
    private ByWebView mByWebView;
    private OnByWebClientCallback onByWebClientCallback;

    ByWebViewClient(Activity activity, ByWebView byWebView) {
        mActivityWeakReference = new WeakReference<Activity>(activity);
        this.mByWebView = byWebView;
    }

    void setOnByWebClientCallback(OnByWebClientCallback onByWebClientCallback) {
        this.onByWebClientCallback = onByWebClientCallback;
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        Log.e("webview", "onReceivedHttpAuthRequest");
        final WebView mView = view;
        final HttpAuthHandler mHandler = handler;
        final EditText usernameInput = new EditText(view.getContext());
        usernameInput.setHint("用户名");
        final EditText passwordInput = new EditText(view.getContext());
        passwordInput.setHint("密码");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout ll = new LinearLayout(view.getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(usernameInput);
        ll.addView(passwordInput);
        AlertDialog.Builder authDialog = new AlertDialog
                .Builder(view.getContext())
                .setTitle("身份验证")
                .setView(ll)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mHandler.proceed(usernameInput.getText().toString(), passwordInput.getText().toString());
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        mView.stopLoading();
//                                onLoadListener.onAuthCancel((WebviewActivity)mView, mTitleTextView);
                    }
                });
        if (view != null)
            authDialog.show();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        boolean isOpenThirdApp = true;
        if (onByWebClientCallback != null) {
            isOpenThirdApp =  onByWebClientCallback.isOpenThirdApp(url);
        }
        if(isOpenThirdApp){
            Activity mActivity = this.mActivityWeakReference.get();
            isOpenThirdApp = ByWebTools.handleThirdApp(mActivity, url);
        }
        if(!isOpenThirdApp){
            mByWebView.loadUrl(url);
        }
        return true;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        boolean isOpenThirdApp = true;
        if (onByWebClientCallback != null) {
            isOpenThirdApp =  onByWebClientCallback.isOpenThirdApp(url);
        }
        if(isOpenThirdApp){
            Activity mActivity = this.mActivityWeakReference.get();
            isOpenThirdApp = ByWebTools.handleThirdApp(mActivity, url);
        }
        if(!isOpenThirdApp){
            mByWebView.loadUrl(url);
        }
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (onByWebClientCallback != null) {
            onByWebClientCallback.onPageStarted(view, url, favicon);
        }
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // html加载完成之后，添加监听图片的点击js函数
        Activity mActivity = this.mActivityWeakReference.get();
        if (mActivity != null && !mActivity.isFinishing()
                && !ByWebTools.isNetworkConnected(mActivity) && mByWebView.getProgressBar() != null) {
            mByWebView.getProgressBar().hide();
        }
        if (onByWebClientCallback != null) {
            onByWebClientCallback.onPageFinished(view, url);
        }
        super.onPageFinished(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        // 6.0以下执行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return;
        }
        mByWebView.showErrorView();
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        // 这个方法在 android 6.0才出现。加了正常的页面可能会出现错误页面
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            int statusCode = errorResponse.getStatusCode();
//            if (404 == statusCode || 500 == statusCode) {
//                mByWebView.showErrorView();
//            }
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (request.isForMainFrame()) {
            // 是否是为 main frame创建
            mByWebView.showErrorView();
        }
    }

    /**
     * 解决google play上线 WebViewClient.onReceivedSslError问题
     */
    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        if (onByWebClientCallback == null || !onByWebClientCallback.onReceivedSslError(view, handler, error)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage("SSL认证失败，是否继续访问？");
            builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            onByWebClientCallback.onReceivedSslError(view, handler, error);
        }
    }

    /**
     * 视频全屏播放按返回页面被放大的问题
     */
    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        if (newScale - oldScale > 7) {
            //异常放大，缩回去。
            view.setInitialScale((int) (oldScale / newScale * 100));
        }
    }

}
