package man.who.scan.my.app.die.a.mother.ui;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.ui.base.BaseActivity;
import man.who.scan.my.app.die.a.mother.ui.base.EmptyFragment;
import man.who.scan.my.app.die.a.mother.ui.items.FileFragment;

public class FileChooserActivity extends BaseActivity implements View.OnClickListener {


    public String currentPath;
    public String typeStr; // dir file

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeStr = getIntent().getStringExtra("type");
        tv_title.setText("请选择路径");
        showFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), false);
//        showFolder(getFilesDir(), false);
        if ("dir".equals(typeStr)) {
            iv_save.setVisibility(View.VISIBLE);
            iv_save.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.putExtra("path", currentPath);
        if (v == iv_save) {
            intent.putExtra("type", "dir");
        } else {
            intent.putExtra("type", "file");
        }
        this.setResult(RESULT_OK, intent);
        this.finish();
    }

    public void showFolder(File folder, boolean addToBackStack) {
        currentPath = folder.getAbsolutePath();
        FragmentManager manager = this.getFragmentManager();
//        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.base, new EmptyFragment());
        // 当前目录
        FileFragment frag = new FileFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", 0);
        bundle.putString("display", currentPath);
        frag.setArguments(bundle);
        transaction.add(R.id.base, frag);
        // 返回上一级
        frag = new FileFragment();
        bundle = new Bundle();
        bundle.putInt("type", 1);
        bundle.putString("display", "返回上一级目录");
        bundle.putString("file", folder.getParent());
        frag.setArguments(bundle);
        transaction.add(R.id.base, frag);
        // 文件列表
//        File[] files = folder.listFiles(new FileFilter() {
//            @Override
//            public boolean accept(File file) {
//                return file.isDirectory();
//            }
//        });
        File[] files = folder.listFiles();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        if (files != null) {
            List<File> fList = Arrays.asList(files);
            Collections.sort(fList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile())
                        return -1;
                    if (o1.isFile() && o2.isDirectory())
                        return 1;
                    return o1.getName().compareTo(o2.getName());
                }
            });
//            System.out.println("当前文件夹： " + folder.getAbsolutePath());

            for (File file : fList) {
                frag = new FileFragment();
                bundle = new Bundle();
                int type = file.isDirectory() ? 2 : 3;
                bundle.putInt("type", type);
                bundle.putString("display", file.getName());
                bundle.putString("file", file.getAbsolutePath());
                frag.setArguments(bundle);
                transaction.add(R.id.base, frag);
            }
        }
//        if (addToBackStack)
//            transaction.addToBackStack(folder.getAbsolutePath());
        transaction.commit();

    }
}
