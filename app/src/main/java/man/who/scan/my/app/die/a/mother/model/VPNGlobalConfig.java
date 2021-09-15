package man.who.scan.my.app.die.a.mother.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import man.who.scan.my.app.die.a.mother.Config;
import man.who.scan.my.app.die.a.mother.Global;

public class VPNGlobalConfig extends BaseConfig {

    public int mode = 0;
    public Set<String> whitelist;
    public Set<String> blacklist;

    public Set<String> getEmptyWhiteList(){
        if(whitelist == null){
            whitelist = new HashSet<>();
        }else{
            whitelist.clear();
        }
        return whitelist;
    }

    public Set<String> getEmptyBlackList(){
        if(blacklist == null){
            blacklist = new HashSet<>();
        }else{
            blacklist.clear();
        }
        return blacklist;
    }
    public void load() {
        File globalConfig = new File(Global.ROOT_DIR, "globalConfig.ini");
        Map<String, String> map = Config.fromFile(globalConfig);
        String mode_str = map.get("mode");
        if(mode_str != null)
            mode = Integer.parseInt(mode_str);

        if (mode == MODE_WHITE_LIST) {
            whitelist = getEmptyWhiteList();
            File whitlist = new File(Global.ROOT_DIR, "whitelist.txt");
            try (BufferedReader reader = new BufferedReader(new FileReader(whitlist))) {
                String line = reader.readLine();
                while (line != null) {
                    whitelist.add(line);
                    line = reader.readLine();
                }
            } catch (Exception e) {
            }
        }else if (mode == MODE_BLACK_LIST) {
            blacklist = getEmptyBlackList();
            File blacklistf = new File(Global.ROOT_DIR, "blacklist.txt");
            try (BufferedReader reader = new BufferedReader(new FileReader(blacklistf))) {
                String line = reader.readLine();
                while (line != null) {
                    blacklist.add(line);
                    line = reader.readLine();
                }
            } catch (Exception e) {
            }
        }
    }


    public void save(){
        File globalConfig = new File(Global.ROOT_DIR, "globalConfig.ini");
        Map<String, String> map = toMap();
        Config.toFile(map, globalConfig);
        if(mode == MODE_WHITE_LIST){
            File whitlist = new File(Global.ROOT_DIR, "whitelist.txt");
            whitlist.delete();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(whitlist))) {
                for(String line: whitelist){
                    writer.write(line);
                    writer.newLine();
                }
            } catch (Exception e) {
            }
        }else if(mode == MODE_BLACK_LIST){
            File blacklistf = new File(Global.ROOT_DIR, "blacklist.txt");
            blacklistf.delete();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(blacklistf))) {
                for(String line: blacklist){
                    writer.write(line);
                    writer.newLine();
                }
            } catch (Exception e) {
            }
        }
    }
}
