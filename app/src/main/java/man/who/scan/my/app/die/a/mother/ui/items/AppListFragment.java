package man.who.scan.my.app.die.a.mother.ui.items;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.AppInfo;
import man.who.scan.my.app.die.a.mother.model.BaseConfig;
import man.who.scan.my.app.die.a.mother.ui.FragmentActivity;
import man.who.scan.my.app.die.a.mother.ui.base.BaseFragment;
import man.who.scan.my.app.die.a.mother.vpn.util.AppManagerUtil;

public class AppListFragment extends BaseFragment {

    FragmentActivity activity;
    View view;
    WebView wb_app_list;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (FragmentActivity) getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.app_list, container, false);
        String htmlPath = resources.getString(R.string.app_list_index_path);
        Configuration config = resources.getConfiguration();
        String lang = config.getLocales().toLanguageTags();
        if (!lang.startsWith("zh")) {
            config.setLocale(Locale.ENGLISH);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        TextView title = activity.findViewById(R.id.tv_title);
        title.setText(R.string.app_list_title);
        wb_app_list = (WebView) view.findViewById(R.id.wb_about);
        // 启用javascript
        wb_app_list.getSettings().setJavaScriptEnabled(true);
        WebSettings ws = wb_app_list.getSettings();
//        String appCachePath = this.getContext().getCacheDir().getAbsolutePath();
//        ws.setAppCachePath(appCachePath);
        ws.setAllowFileAccess(true);
        ws.setAppCacheEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setDomStorageEnabled(true);
        // 从assets目录下面的加载html
        wb_app_list.loadUrl(htmlPath);
        wb_app_list.addJavascriptInterface(AppListFragment.this, "android");
        return view;
    }

    @JavascriptInterface
    public String getClipboard() {
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = cm.getPrimaryClip();
        if (clipData.getItemCount() > 0) {
            try {
                String content = clipData.getItemAt(0).getText().toString();
                boolean isValid = content.charAt(0) >= '0' && content.charAt(0) <= '2';
                if (isValid && content.contains(",")) {
                    toast(resources.getString(R.string.tips_import_clipborad_ok));
                    return content;
                }

            } catch (Exception e) {
            }
        }
        toast(resources.getString(R.string.tips_clipborad_err_data));
        return "";
    }

    @JavascriptInterface
    public void setClipboard(String data) {
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", data);
        cm.setPrimaryClip(mClipData);
        toast(resources.getString(R.string.tips_settings_copied));
    }

    @JavascriptInterface
    public String getAppMode() {
        return "" + Global.vpnGlobalConfig.mode;
    }

    @JavascriptInterface
    public void setApplist(String list) {
        String[] lists = list.split(",");
        int mode = Integer.parseInt(lists[0]);
        Global.vpnGlobalConfig.mode = mode;
        switch (mode) {
            case BaseConfig.MODE_DEFAULT:
                break;
            case BaseConfig.MODE_WHITE_LIST:
                Global.vpnGlobalConfig.getEmptyWhiteList();
                for (int i = 1; i < lists.length; i++) {
                    Global.vpnGlobalConfig.whitelist.add(lists[i]);
                }
                break;
            case BaseConfig.MODE_BLACK_LIST:
                Global.vpnGlobalConfig.getEmptyBlackList();
                for (int i = 1; i < lists.length; i++) {
                    Global.vpnGlobalConfig.blacklist.add(lists[i]);
                }
                break;
        }
        Global.vpnGlobalConfig.save();
    }


    @JavascriptInterface
    public String getApplist(String isNotFirstRun) {
        boolean isNotFirstTime = "true".equals(isNotFirstRun);
        Context context = this.getContext();
        List<AppInfo> apps = AppManagerUtil.loadNetworkAppList(context, !isNotFirstTime);
        for (AppInfo app : apps) {
            String pkgName = app.getPackageName();
            if (Global.vpnGlobalConfig.mode == BaseConfig.MODE_BLACK_LIST && Global.vpnGlobalConfig.blacklist.contains(pkgName)) {
                app.setInBlackList(true);
            } else if (Global.vpnGlobalConfig.mode == BaseConfig.MODE_WHITE_LIST && Global.vpnGlobalConfig.whitelist.contains(pkgName)) {
                app.setInWhiteList(true);
            }
        }
        return apps.toString();
    }
    @JavascriptInterface
    public String getAppName(String pkgName) {
        return  AppManagerUtil.getAppName(this.getContext(), pkgName);
    }
}
