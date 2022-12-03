package man.who.scan.my.app.die.a.mother.ui.items;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import man.who.scan.my.app.die.a.mother.Config;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.dex.MDexClassLoader;
import man.who.scan.my.app.die.a.mother.dex.MDexService;
import man.who.scan.my.app.die.a.mother.model.DexConfig;
import man.who.scan.my.app.die.a.mother.ui.FileChooserActivity;
import man.who.scan.my.app.die.a.mother.ui.base.BaseFragment;
import man.who.scan.my.app.die.a.mother.vpn.util.ResourcesUtil;

public class DexServiceFragment extends BaseFragment implements View.OnClickListener {

    EditText et_dex_path, et_dex_main_class;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv_title.setText(R.string.dex_settings);
        Global.dexConfig.fromMap(Config.fromFile(Global.DEX_CONFIG_FILE));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dex_service, container, false);
        Global.dexConfig.initView(view);
        Global.dexConfig.updateView(view);
        et_dex_path = view.findViewById(R.id.dexPath);
        et_dex_main_class = view.findViewById(R.id.dexMainClassName);
        View bv_import_dex = view.findViewById(R.id.bv_import_dex);
        bv_import_dex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果Android版本较高，需要申请文件读取权限
//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !Environment.isExternalStorageManager()){
//                    Intent intent = new Intent();
//                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                    activity.startActivityForResult(intent, REQUEST_ALL_FILE_AUTH);
//                }else
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toURI());
                    activity.startActivityForResult(intent, SELECT_FILE_BY_SYSTEM);
                } else {
                    Intent intent = new Intent(activity, FileChooserActivity.class);
                    intent.putExtra("type", "file");
                    startActivityForResult(intent, SELECT_FILE);
                }
            }
        });
        View bv_import_main_class = view.findViewById(R.id.bv_import_main_class);
        bv_import_main_class.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Class main = MDexClassLoader.load(et_dex_path.getText().toString(), et_dex_main_class.getText().toString());
                    DexConfig.startService = main.getDeclaredMethod("start");
                    DexConfig.stopService = main.getDeclaredMethod("stop");

                    System.setProperty("ninja.dex.path", et_dex_path.getText().toString());
                    Global.dexConfig.getFromView(view);
                    Config.toFile(Global.dexConfig.toMap(), Global.DEX_CONFIG_FILE);
                    toast(resources.getString(R.string.tips_successfully_loaded));
                } catch (Exception e) {
                    toast(e.toString());
                }
            }
        });
        View bv_start_dex = view.findViewById(R.id.bv_start_dex);
        bv_start_dex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startService(new Intent(activity, MDexService.class));
            }
        });
        View bv_stop_dex = view.findViewById(R.id.bv_stop_dex);
        bv_stop_dex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.stopService(new Intent(activity, MDexService.class));
            }
        });
        return view;
    }


    @Override
    public void onClick(View v) {
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    String file = data.getStringExtra("path");
                    EditText et_DexPath = view.findViewById(R.id.dexPath);
                    et_DexPath.setText(file);
                }
                break;
            case REQUEST_ALL_FILE_AUTH:
                Intent intent = new Intent(activity, FileChooserActivity.class);
                intent.putExtra("type", "file");
                startActivityForResult(intent, SELECT_FILE);
                break;
            case SELECT_FILE_BY_SYSTEM:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    String file = getRealPath(uri);
                    EditText et_DexPath = view.findViewById(R.id.dexPath);
                    et_DexPath.setText(file);
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    this.getContext().getContentResolver().takePersistableUriPermission(uri,
                            takeFlags);
                }
                break;
        }
    }


    // http://t.zoukankan.com/androidxiaoyang-p-4968663.html
    private String getRealPath(Uri fileUrl) {
        String fileName = null;
        if (fileUrl != null) {
            if (fileUrl.getScheme().toString().compareTo("content") == 0) // content://开头的uri
            {
                //把文件复制到沙盒目录
                ContentResolver contentResolver = this.getContext().getContentResolver();
                String displayName = "temp.dex";
                try {
                    InputStream is = contentResolver.openInputStream(fileUrl);
                    File cache = new File(this.getContext().getCacheDir().getAbsolutePath(), displayName);
                    FileOutputStream fos = new FileOutputStream(cache);
                    ResourcesUtil.copy(is, fos);
                    fileName = cache.getCanonicalPath();
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (fileUrl.getScheme().compareTo("file") == 0) // file:///开头的uri
            {
                fileName = fileUrl.getPath();
            }
        }
        return fileName;
    }


}
