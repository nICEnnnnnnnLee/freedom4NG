package man.who.scan.my.app.die.a.mother.dex;

import dalvik.system.DexClassLoader;
import man.who.scan.my.app.die.a.mother.Global;

public class MDexClassLoder {


    public static Class load(String dexPath, String className) throws ClassNotFoundException {
        DexClassLoader classLoader = new DexClassLoader(dexPath, Global.DEX_DIR.getAbsolutePath(), null, MDexClassLoder.class.getClassLoader());
        return classLoader.loadClass(className);
    }
}
