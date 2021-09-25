package man.who.scan.my.app.die.a.mother.ui.utils.model;

public class Cookie {
    private String host_key;
    private String name;
    private String value;
    private String path;

    public Cookie() {
    }

    public Cookie(String host_key, String name, String path){
        this.host_key = host_key;
        this.name = name;
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getHost_key() {
        return host_key;
    }

    public void setHost_key(String host_key) {
        this.host_key = host_key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
