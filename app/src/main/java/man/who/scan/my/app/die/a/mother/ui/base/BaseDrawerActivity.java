package man.who.scan.my.app.die.a.mother.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.BaseConfig;
import man.who.scan.my.app.die.a.mother.ui.FragmetActivity;

public class BaseDrawerActivity extends BaseActivity implements View.OnClickListener {

    protected View fab_start, fab_stop;
    //    protected ImageView iv_add, iv_more, iv_delete, iv_save, iv_reload;
    protected TextView tv_dns, tv_host, tv_dex;
    protected DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.disableDefaultLoad();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_drawer);
        super.onLayoutLoded();
        fab_start = findViewById(R.id.fab_start);
        fab_stop = findViewById(R.id.fab_stop);

        tv_dns = findViewById(R.id.tv_dns);
        tv_host = findViewById(R.id.tv_host);
        tv_dex = findViewById(R.id.tv_dex);
        tv_dns.setOnClickListener(this);
        tv_host.setOnClickListener(this);
        tv_dex.setOnClickListener(this);

        mDrawerLayout = findViewById(R.id.drawerLayout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_dns:
                mDrawerLayout.closeDrawers();
                Intent intent = new Intent(BaseDrawerActivity.this, FragmetActivity.class);
                intent.putExtra("configPath", Global.DNS_FILE.getAbsolutePath());
                intent.putExtra("configType", BaseConfig.DNS);
                startActivity(intent);
                break;
            case R.id.tv_host:
                mDrawerLayout.closeDrawers();
                intent = new Intent(BaseDrawerActivity.this, FragmetActivity.class);
                intent.putExtra("configPath", Global.HOST_FILE.getAbsolutePath());
                intent.putExtra("configType", BaseConfig.HOST);
                startActivity(intent);
                break;
            case R.id.tv_dex:
                mDrawerLayout.closeDrawers();
                intent = new Intent(BaseDrawerActivity.this, FragmetActivity.class);
                intent.putExtra("configPath", Global.HOST_FILE.getAbsolutePath());
                intent.putExtra("configType", BaseConfig.DEX);
                startActivity(intent);
                break;
        }
    }

}
