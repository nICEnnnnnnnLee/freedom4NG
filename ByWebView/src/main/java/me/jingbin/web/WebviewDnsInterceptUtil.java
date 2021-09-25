package me.jingbin.web;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;

public class WebviewDnsInterceptUtil {

    private static ConcurrentHashMap<String, String> hosts;

    public static void setDnsRecord(String domain, String ip) {
        if (hosts == null)
            hosts = new ConcurrentHashMap<String, String>();
        hosts.put(domain, ip);
    }

    public static void removeDnsRecord(String domain) {
        if (hosts != null)
            hosts.remove(domain);
    }

    public static void removeAllDnsRecord() {
        if (hosts != null)
            hosts.clear();
    }


    @SuppressLint("NewApi")
    public static WebResourceResponse getDnsInterceptRequest(WebView view, WebResourceRequest request) {
        if (request != null && request.getUrl() != null && request.getMethod().equalsIgnoreCase("get")) {
            return getWebResourceFromUrl(request.getUrl().toString());
        }
        return null;
    }

    public static WebResourceResponse getDnsInterceptUrl(WebView view, String url) {
        if (!TextUtils.isEmpty(url) && Uri.parse(url).getScheme() != null) {
            return getWebResourceFromUrl(url);
        }
        return null;
    }

    //核心拦截方法
    private static WebResourceResponse getWebResourceFromUrl(String url) {
        String scheme = Uri.parse(url).getScheme().trim();
        String ipAddr = hosts.get(Uri.parse(url).getHost());
        if (ipAddr == null) {
            Log.d("debug", "web log 不拦截：" + url);
            return null;
        }
        // HttpDns解析css文件的网络请求及图片请求
        if ((scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            try {
                URL oldUrl = new URL(url);
                URLConnection connection = oldUrl.openConnection();
                String newUrl = url.replaceFirst(oldUrl.getHost(), ipAddr);
                connection = new URL(newUrl).openConnection(); // 设置HTTP请求头Host域
                connection.setRequestProperty("Host", oldUrl.getHost());
                String[] strings = connection.getContentType().split(";");
                return new WebResourceResponse(strings[0], "UTF-8", connection.getInputStream());
            } catch (MalformedURLException e) {
//                e.printStackTrace();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        return null;
    }
}