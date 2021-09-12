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

import java.util.Locale;

import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.ui.FragmetActivity;
import man.who.scan.my.app.die.a.mother.ui.base.BaseFragment;
import me.jingbin.web.ByWebView;

public class BrowserFragment extends BaseFragment {

    FragmetActivity activity;
    View view;

    private WebView webView;
    private ByWebView byWebView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (FragmetActivity) getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.base_empty, container, false);
        Configuration config = resources.getConfiguration();
        String lang = config.getLocales().toLanguageTags();
        if(!lang.startsWith("zh")){
            config.setLocale(Locale.ENGLISH);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        String htmlPath = "file:///android_asset/about/about.html";
        TextView title = activity.findViewById(R.id.tv_title);
        title.setText(R.string.about);
        webView = (WebView) view.findViewById(R.id.wb_about);
        return view;
    }

    //由于安全原因 需要加 @JavascriptInterface
    @JavascriptInterface
    public String getVersionName() {
        Context context = this.getContext();
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}
