package man.who.scan.my.app.die.a.mother.ui.items;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.AppInfo;
import man.who.scan.my.app.die.a.mother.model.BaseConfig;
import man.who.scan.my.app.die.a.mother.ui.FragmetActivity;
import man.who.scan.my.app.die.a.mother.ui.base.BaseFragment;
import man.who.scan.my.app.die.a.mother.vpn.util.AppManagerUtil;

public class AppListFragment extends BaseFragment {

    FragmetActivity activity;
    View view;
    WebView wb_app_list;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (FragmetActivity) getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.app_list, container, false);
        String htmlPath = resources.getString(R.string.app_list_index_path);
        Configuration config = resources.getConfiguration();
        String lang = config.getLocales().toLanguageTags();
        if(!lang.startsWith("zh")){
            config.setLocale(Locale.ENGLISH);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        TextView title = activity.findViewById(R.id.tv_title);
        title.setText(R.string.app_list_title);
        wb_app_list = (WebView) view.findViewById(R.id.wb_about);
        // 启用javascript
        wb_app_list.getSettings().setJavaScriptEnabled(true);
        // 从assets目录下面的加载html
        wb_app_list.loadUrl(htmlPath);
        wb_app_list.addJavascriptInterface(AppListFragment.this, "android");
        return view;
    }

    @JavascriptInterface
    public String getAppMode() {
//        System.out.println("=========getAppMode");
        return "" + Global.vpnGlobalConfig.mode;
    }

    @JavascriptInterface
    public void setApplist(String list) {
//        System.out.println("=========");
        String[] lists = list.split(",");
        int mode = Integer.parseInt(lists[0]);
        Global.vpnGlobalConfig.mode = mode;
        switch (mode){
            case BaseConfig.MODE_DEFAULT:
                break;
            case BaseConfig.MODE_WHITE_LIST:
                Global.vpnGlobalConfig.getEmptyWhiteList();
                for(int i=1; i<lists.length; i++){
                    Global.vpnGlobalConfig.whitelist.add(lists[i]);
                }
                break;
            case BaseConfig.MODE_BLACK_LIST:
                Global.vpnGlobalConfig.getEmptyBlackList();
                for(int i=1; i<lists.length; i++){
                    Global.vpnGlobalConfig.blacklist.add(lists[i]);
                }
                break;
        }
        Global.vpnGlobalConfig.save();
    }


    @JavascriptInterface
    public String getApplist() {
//        System.out.println("=========getApplist");
        Context context = this.getContext();
        List<AppInfo> apps = AppManagerUtil.loadNetworkAppList(context);
        for(AppInfo app: apps){
            String pkgName = app.getPackageName();
            if(Global.vpnGlobalConfig.mode == BaseConfig.MODE_BLACK_LIST && Global.vpnGlobalConfig.blacklist.contains(pkgName)){
                app.setInBlackList(true);
            }else if(Global.vpnGlobalConfig.mode == BaseConfig.MODE_WHITE_LIST && Global.vpnGlobalConfig.whitelist.contains(pkgName)){
                app.setInWhiteList(true);
            }
        }
//        System.out.println(apps);
        return apps.toString();
    }
}
