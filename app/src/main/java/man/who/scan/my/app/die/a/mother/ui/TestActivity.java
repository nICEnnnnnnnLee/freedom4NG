package man.who.scan.my.app.die.a.mother.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import man.who.scan.my.app.die.a.mother.R;

@Deprecated
public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.base_0_test);
        showListView();
    }

    private void showListView() {
        ListView listView = (ListView) findViewById(R.id.lv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getData());
        listView.setAdapter(adapter);
        this.registerForContextMenu(listView);//注册上下文菜单
    }

    //创建菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //设置mune显示的内容
        menu.setHeaderTitle("文件操作");
        menu.setHeaderIcon(R.drawable.ic_launcher);
//         public MenuItem add(int groupId, int itemId, int order, CharSequence title);
        menu.add(1, 1, 1, "copy");
        menu.add(1, 2, 1, "cut");
        menu.add(1, 3, 1, "past");
        menu.add(1, 4, 1, "cancel");
    }

    //响应菜单
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Toast.makeText(this, "clicked copy", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, "clicked cut", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(this, "clicked past", Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(this, "clicked cancel", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private ArrayList<String> getData() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("file" + (i + 1));
        }
        return list;
    }
}