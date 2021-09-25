package man.who.scan.my.app.die.a.mother.model;

import java.lang.reflect.Method;

public class DexConfig extends BaseConfig{

    public String dexPath;
    public String dexMainClassName;

//    public static Class mainClass;
    public static Method startService;
    public static Method stopService;
//    public volatile static Boolean isRunning;
}
