package man.who.scan.my.app.die.a.mother.ui.items;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import man.who.scan.my.app.die.a.mother.Config;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.dex.MDexClassLoder;
import man.who.scan.my.app.die.a.mother.dex.MDexService;
import man.who.scan.my.app.die.a.mother.model.DexConfig;
import man.who.scan.my.app.die.a.mother.ui.FileChooserActivity;
import man.who.scan.my.app.die.a.mother.ui.base.BaseFragment;

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
                Intent intent = new Intent(activity, FileChooserActivity.class);
                intent.putExtra("type", "file");
                startActivityForResult(intent, SELECT_FILE);
            }
        });
        View bv_import_main_class = view.findViewById(R.id.bv_import_main_class);
        bv_import_main_class.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Class main = MDexClassLoder.load(et_dex_path.getText().toString(), et_dex_main_class.getText().toString());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BaseFragment.SELECT_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                String file = data.getStringExtra("path");
                EditText et_DexPath = view.findViewById(R.id.dexPath);
                et_DexPath.setText(file);
            }
        }
    }
}
