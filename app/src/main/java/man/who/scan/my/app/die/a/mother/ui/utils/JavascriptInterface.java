package man.who.scan.my.app.die.a.mother.ui.utils;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.Map;

import man.who.scan.my.app.die.a.mother.ui.ByWebViewActivity;

/**
 * Created by jingbin on 2016/11/17.
 * js通信接口
 */
public class JavascriptInterface {

    private Context context;
    private ByWebViewActivity byWebViewActivity;

    public JavascriptInterface(Context context) {
        this.context = context;
        this.byWebViewActivity = ((ByWebViewActivity)context);
    }

    @android.webkit.JavascriptInterface
    public void goToPage(String url) {
        //ByWebViewActivity.loadUrl(context, url, "Tomato", 0, "index");
        final String gotoUrl = url;
        byWebViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                byWebViewActivity.getByWebView().loadUrl(gotoUrl);
            }
        });
    }

    @android.webkit.JavascriptInterface
    public void allThirdApp(String boolStr) {
        byWebViewActivity.allowThirdApp = "true".equalsIgnoreCase(boolStr);
    }

    @android.webkit.JavascriptInterface
    public void copy(String content) {
        //ByWebViewActivity.loadUrl(context, url, "Tomato", 0, "index");
        final String content2Copy = content;
        byWebViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebTools.copy(context, content2Copy);
                Toast.makeText(context, "复制成功", Toast.LENGTH_LONG).show();
            }
        });
    }

    @android.webkit.JavascriptInterface
    public void setHeaders(String headerSettings) {
        String[] lines = headerSettings.split("\n");
        Map<String, String> headers = ByWebViewActivity.getHeaders();
        headers.clear();
        headers.put("x-requested-with", "tomato." + ByWebViewActivity.versionName);
        for(String line: lines){
            if(!line.startsWith("#")){
                int index = line.indexOf(":");
                if(index > 0){
                    final String key = line.substring(0, index).trim();
                    final String value = line.substring(index + 1).trim();
                    if("user-agent".equalsIgnoreCase(key)){
                        byWebViewActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                byWebViewActivity.getByWebView().getWebView().getSettings().setUserAgentString(value);;
                            }
                        });
                        headers.put("user-agent", key);
                    }else{
                        headers.put(key, value);
                    }
                }
            }
        }
    }
}
