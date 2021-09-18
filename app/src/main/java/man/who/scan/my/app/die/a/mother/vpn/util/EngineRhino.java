package man.who.scan.my.app.die.a.mother.vpn.util;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import man.who.scan.my.app.die.a.mother.Global;

public class EngineRhino {

    static boolean useDynamicScope;

    static class MyFactory extends ContextFactory {
        @Override
        protected boolean hasFeature(Context cx, int featureIndex) {
            if (featureIndex == Context.FEATURE_DYNAMIC_SCOPE) {
                return useDynamicScope;
            }
            return super.hasFeature(cx, featureIndex);
        }
    }

    static {
        useDynamicScope = false;
        ContextFactory.initGlobal(new MyFactory());
    }

    ScriptableObject sharedScope;

    public static boolean isPlainHostName(String host) {
        return !host.contains(".");
    }

    public static String dnsResolve(String host) {
//        System.out.println("dnsResolve: " + host);
        if(Global.hostTableRuntime != null)
            return Global.hostTableRuntime.getOrDefault(host, "");
        else
            return "";
    }

    /*
    try {
                InputStreamReader isr = new InputStreamReader(resources.openRawResource(R.raw.gfw_pac));
                EngineRhino pac = new EngineRhino(isr);
                boolean result = pac.isDirect("/", "google.com");
                System.out.printf("google.com: %s\n", result);
                result = pac.isDirect("/", "baidu.com");
                System.out.printf("baidu.com: %s\n", result);
                result = pac.isDirect("/", "1.1.1.1");
                System.out.printf("1.1.1.1: %s\n", result);
                result = pac.isDirect("/", "api.twitter.com");
                System.out.printf("api.twitter.com: %s\n", result);
                result = pac.isDirect("/", "104.244.42.2");
                System.out.printf("104.244.42.2: %s\n", result);
    //            boolean result = pac.isDirect("/", "1.1.1.1");
                result = pac.isDirect("/", "api.twitter.com");
                System.out.printf("api.twitter.com: %s\n", result);
            } catch (Exception e) {
                e.printStackTrace();
            }
     */
    public EngineRhino(String script) {
        useDynamicScope = true;
        Context cx = Context.enter();
        cx.setOptimizationLevel(-1);
        sharedScope = cx.initStandardObjects(null, true);

//        Scriptable scope = cx.newObject(sharedScope);
//        scope.setPrototype(sharedScope);
//        scope.setParentScope(null);
        injectPACInterface(cx, sharedScope);
        cx.evaluateString(sharedScope, script, "script", 1, null);
        Context.exit();
        useDynamicScope = false;
    }

    public EngineRhino(Reader script) throws IOException {
        useDynamicScope = true;
        Context cx = Context.enter();
        cx.setOptimizationLevel(-1);
        sharedScope = cx.initStandardObjects(null, true);

//        Scriptable scope = cx.newObject(sharedScope);
//        scope.setPrototype(sharedScope);
//        scope.setParentScope(null);
        injectPACInterface(cx, sharedScope);
        cx.evaluateReader(sharedScope, script, "script", 1, null);
        Context.exit();
        useDynamicScope = false;
    }

    private void injectPACInterface(Context cx, Scriptable scope) {
//        String pattern = "%s=function(){{var args=Array.prototype.slice.call(arguments);return 'true' == java_%s.invoke(null, args)}};";
        String pattern = "var %s = Packages.%s.%s;";
        Method[] methods = EngineRhino.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC)) {
                String name = method.getName();
                String cmd = String.format(pattern, name, EngineRhino.class.getName(), name);
                cx.evaluateString(scope, cmd, "<cmd>", 1, null);
            }
        }
    }

    public Boolean isDirect(String url, String host) {
        try {
            Context cx = Context.enter();
            Object functionArgs[] = {url, host};
            Object fObj = sharedScope.get("FindProxyForURL", sharedScope);
            Object result = ((Function) fObj).call(cx, sharedScope, sharedScope, functionArgs);
            String scriptResult = Context.toString(result);
            return scriptResult.startsWith("DIRECT");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            Context.exit();
        }
    }
}
