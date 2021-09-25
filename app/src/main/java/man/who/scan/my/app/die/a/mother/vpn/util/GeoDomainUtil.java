package man.who.scan.my.app.die.a.mother.vpn.util;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;

public class GeoDomainUtil {

    HashSet<String> gfw_set;
    HashSet<String> direct_set;

    public GeoDomainUtil(InputStream gfw_f, InputStream direct_f) throws IOException {
        initGFW(gfw_f);
        initDirectSet(direct_f);
    }

    private void initDirectSet(InputStream direct_f) throws IOException{
        byte[] raw = readAll(direct_f);
        String result = new String(raw);
        direct_set = new HashSet<>();
        String[] lines = result.split("\n");
//        for (String line : lines) {
//            direct_set.add(line);
//        }
        Collections.addAll(direct_set, lines);
    }

    private void initGFW(InputStream gfw_f) throws IOException{
        byte[] raw = readAll(gfw_f);
        String result = new String(Base64.decode(raw, Base64.NO_WRAP));
        String[] lines = result.split("\n");
        gfw_set = new HashSet<>();
        for (String line : lines) {
            if ("!##############General List End#################".equals(line))
                break;
            if (line.contains(".*"))
                continue;
            else if (line.contains("*"))
                line = line.replace("*", "/");

            if (line.startsWith("||"))
                line = line.substring(2);
            else if (line.startsWith("|"))
                line = line.substring(1);
            else if (line.startsWith("."))
                line = line.substring(1);

            if (line.startsWith("!"))
                continue;
            else if (line.startsWith("["))
                continue;
            else if (line.startsWith("@"))
                continue;

            int start = line.indexOf("://");
            if (start > -1)
                line = line.substring(start + 3);
            int end = line.indexOf("/");
            if (end > -1)
                line = line.substring(0, end);
            gfw_set.add(line);
        }
    }

    private byte[] readAll(InputStream f) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = f.read(buffer);
        while (len > 0) {
            out.write(buffer, 0, len);
            len = f.read(buffer);
        }
        byte[] raw = out.toByteArray();
        f.close();
        return raw;
    }

    public Boolean isDirect(String host) {
        if (host.endsWith("cn"))
            return true;
        int idx1 = host.lastIndexOf(".");
        int idx2 = host.lastIndexOf(".", idx1 - 1);
        while (true) {
            String suffix = host.substring(idx2 + 1);
            if(direct_set.contains(suffix)){
                return true;
            }else if (gfw_set.contains(suffix))
                return false;
            else if (idx2 == -1)
                return null;
            idx2 = host.lastIndexOf('.', idx2 - 1);
        }
    }

//    private boolean match(String host, HashSet<String> set){
//        int idx1 = host.lastIndexOf(".");
//        int idx2 = host.lastIndexOf(".", idx1 - 1);
//        while (true) {
//            String suffix = host.substring(idx2 + 1);
//            if (set.contains(suffix))
//                return true;
//            if (idx2 == -1)
//                return false;
//            idx2 = host.lastIndexOf('.', idx2 - 1);
//        }
//    }
}
