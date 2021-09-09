package man.who.scan.my.app.die.a.mother.model;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import man.who.scan.my.app.die.a.mother.R;


public class BaseConfig {
    final static String options[] = {"true", "false"};
    final static public int DNS = 1;
    final static public int VPN = 2;
    final static public int HOST = 3;
    final static public int DEX = 4;
    final static public int ABOUT = 5;

    private static int getId(String id) {
        try {
            Field field = R.id.class.getField(id);
            return (int) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("R.id 并没有该值：" + id);
        }
    }

    public void getFromView(View view0) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                String key = field.getName();
                View view = view0.findViewById(getId(key));
                if (field.getType().equals(String.class)) {
                    String str = ((EditText) view).getText().toString();
                    if (!str.isEmpty() && !str.equalsIgnoreCase("null"))
                        field.set(this, str);
                } else if (field.getType().equals(int.class)) {
                    field.setInt(this, Integer.parseInt(((EditText) view).getText().toString()));
                } else if (field.getType().equals(boolean.class)) {
                    String itemStr = ((Spinner) view).getSelectedItem().toString();
                    field.setBoolean(this, "true".equalsIgnoreCase(itemStr));
                } else {
                    System.err.println("BaseConfig.getFromView(): 未曾见过的类型");
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }

    public void initView(View view0) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                String key = field.getName();
                Object value = field.get(this);
                View view = view0.findViewById(getId(key));
                if (view instanceof Spinner) {
                    ArrayAdapter adapter = new ArrayAdapter<String>(view0.getContext(), android.R.layout.simple_spinner_item, options);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    ((Spinner) view).setAdapter(adapter);
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
//        updateView(activity);
    }

    public void updateView(View view0) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                String key = field.getName();
                Object value = field.get(this);
                int id = getId(key);
                View view = view0.findViewById(id);
                if (view instanceof EditText) {
                    ((EditText) view).setText(value.toString());
                } else if (view instanceof Spinner) {
                    int position = (Boolean) value ? 0 : 1;
                    ((Spinner) view).setSelection(position);
                } else {
                    System.err.println("暂时未实现该类型的自动设置");
                }
            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println(e.toString());
            }
        }
    }

    @Override
    public String toString() {
        Field[] fields = this.getClass().getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        for (Field filed : fields) {
            try {
                sb.append(String.format("%s -> %s\n", filed.getName(), filed.get(this)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    public void fromMap(Map<String, String> map) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                String value = map.get(field.getName());
                if (value != null) {
                    if (field.getType().equals(String.class)) {
                        String str = (String) value;
                        if (!str.isEmpty() && !str.equalsIgnoreCase("null"))
                            field.set(this, value);
                    } else if (field.getType().equals(int.class)) {
//                        System.out.printf("%s 将被设置为 %s\n", field.getName(), value);
                        field.setInt(this, Integer.parseInt(value));
                    } else if (field.getType().equals(boolean.class)) {
                        field.setBoolean(this, "true".equalsIgnoreCase(value));
                    } else {
                        System.err.println("BaseConfig.fromMap(): 未曾见过的类型");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.toString());
            }
        }
    }

    public Map<String, String> updateMap(Map<String, String> map) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field filed : fields) {
            try {
                Type type = filed.getType();
                if (type == int.class || type == String.class || type == boolean.class) {
                    String value = filed.get(this).toString();
                    map.put(filed.getName(), value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        return updateMap(map);
    }
}
