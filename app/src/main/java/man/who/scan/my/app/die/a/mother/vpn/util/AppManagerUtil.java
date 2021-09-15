package man.who.scan.my.app.die.a.mother.vpn.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;

import java.util.ArrayList;
import java.util.List;

import man.who.scan.my.app.die.a.mother.model.AppInfo;

public class AppManagerUtil {

    public static List<AppInfo> loadNetworkAppList(Context context) {
        List<AppInfo> apps = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> list = packageManager.getInstalledPackages(0);
//        List<PackageInfo> list = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (PackageInfo pkg : list) {
            String pkgName = pkg.packageName;
            if (!pkgName.equals("android")){
//            if (hasInternetPermission(pkg) && !pkgName.equals("android")){
                String pkgLabel = pkg.applicationInfo.loadLabel(packageManager).toString();
                boolean isSysApp = (pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
                AppInfo appInfo = new AppInfo(pkgLabel, pkgName, isSysApp);
//                System.out.println(appInfo);
                apps.add(appInfo);
            }
        }
        return apps;
    }

    private static boolean hasInternetPermission(PackageInfo pkg) {
        if(pkg.permissions == null)
            return true;
        for (PermissionInfo per : pkg.permissions) {
            if (per.name == Manifest.permission.INTERNET)
                return true;
        }
        return false;

    }


}
