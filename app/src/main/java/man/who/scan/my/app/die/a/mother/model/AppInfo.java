package man.who.scan.my.app.die.a.mother.model;

public class AppInfo {

    private String appName;
    private String packageName;
    private boolean isSysApp;
    private boolean isInWhiteList;
    private boolean isInBlackList;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"appName\":\"").append(appName).append("\",");
        sb.append("\"packageName\":\"").append(packageName).append("\",");
        sb.append("\"isSysApp\":").append(isSysApp).append(",");
        sb.append("\"isInWhiteList\":").append(isInWhiteList).append(",");
        sb.append("\"isInBlackList\":").append(isInBlackList);
        sb.append("}");
        return sb.toString();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isSysApp() {
        return isSysApp;
    }

    public void setSysApp(boolean sysApp) {
        isSysApp = sysApp;
    }

    public boolean isInWhiteList() {
        return isInWhiteList;
    }

    public void setInWhiteList(boolean inWhiteList) {
        isInWhiteList = inWhiteList;
    }

    public boolean isInBlackList() {
        return isInBlackList;
    }

    public void setInBlackList(boolean inBlackList) {
        isInBlackList = inBlackList;
    }

    public AppInfo(String appName, String packageName, boolean isSysApp){
        this.appName = appName;
        this.packageName = packageName;
        this.isSysApp = isSysApp;
        isInWhiteList = false;
        isInBlackList = false;
    }
}
