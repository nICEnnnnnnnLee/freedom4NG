package man.who.scan.my.app.die.a.mother;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

    private final static Pattern keyValuePattern = Pattern.compile("^([^:]+):(.*)");
    private final static Pattern hostPattern = Pattern.compile("^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}) +(.*)");

    public static Map<String, String> fromFile(String path) {
        File file = new File(path);
        return fromFile(file);
    }

    public static HashMap<String, String> fromHostFile(HashMap<String, String> configs, File file) {
        return fromHostFile(configs, file, true);
    }

    public static HashMap<String, String> fromHostFile(HashMap<String, String> configs, File file, boolean clearMapFirst) {
        if (clearMapFirst) {
            configs.clear();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                Matcher matcher = hostPattern.matcher(line);
                if (matcher.find()) {
                    configs.put(matcher.group(2).trim(), matcher.group(1));
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
        }
        return configs;
    }

    public static Map<String, String> fromFile(File file) {
        HashMap<String, String> configs = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                Matcher matcher = keyValuePattern.matcher(line);
                if (matcher.find()) {
                    configs.put(matcher.group(1), matcher.group(2).trim());
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
        }
        return configs;
    }

    public static boolean toFile(Map<String, String> map, File file) {
        if (!file.exists())
            file.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String line = String.format("%s:%s\n", entry.getKey(), entry.getValue());
//                System.out.println(line);
                writer.write(line);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean toFile(Map<String, String> map, String path) {
        File file = new File(path);
        return toFile(map, file);
    }

    public static boolean global2File(String path) {
        File file = new File(Global.ROOT_DIR, path);
        Map<String, String> map = Global.vpnConfig.toMap();
        Global.dnsConfig.updateMap(map);
        return toFile(map, file);
    }

    public static boolean file2Global(String path) {
        File file = new File(Global.ROOT_DIR, path);
        Map<String, String> configs = fromFile(file);
        if (configs != null) {
            Global.vpnConfig.fromMap(configs);
            Global.dnsConfig.fromMap(configs);
            Global.initCookies();
            return true;
        } else {
            return false;
        }
    }
}