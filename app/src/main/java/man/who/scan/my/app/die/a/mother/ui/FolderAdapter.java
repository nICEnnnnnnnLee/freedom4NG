package man.who.scan.my.app.die.a.mother.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

@Deprecated
public class FolderAdapter extends ArrayAdapter<File> {

    final Context context;
    final int resourceId;
    public FolderAdapter(Context context, int textViewResourceId, List<File> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        File file = getItem(position); //获取当前项的Fruit实例
        String path = file.getName();
        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        if (convertView == null) {
            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            TextView view0 = new TextView(context);
            view0.setText(path);
            view0.setPadding(15, 5, 15, 5);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(15, 15, 15, 15);
            view0.setLayoutParams(params);
            view0.setBackgroundResource(android.R.color.white);
            return view0;
        } else {
            return convertView;
        }
    }
}
