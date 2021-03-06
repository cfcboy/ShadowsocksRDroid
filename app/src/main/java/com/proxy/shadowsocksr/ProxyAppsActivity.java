package com.proxy.shadowsocksr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.orhanobut.hawk.Hawk;
import com.proxy.shadowsocksr.adapter.AppsAdapter;
import com.proxy.shadowsocksr.adapter.items.AppItem;

import java.util.ArrayList;
import java.util.List;

public class ProxyAppsActivity extends Activity implements AppsAdapter.OnItemClickListener
{
    private RecyclerView rvApps;
    private AppsAdapter appsAdapter;
    private List<AppItem> appLst;
    private ArrayList<String> proxyApps;

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_app_to_proxy);
        rvApps = (RecyclerView) findViewById(R.id.rv_proxy_apps);
        rvApps.setLayoutManager(new LinearLayoutManager(this));
        rvApps.setHasFixedSize(true);
        appLst = new ArrayList<>();
        appsAdapter = new AppsAdapter(appLst);
        appsAdapter.setOnItemClickListener(this);
        rvApps.setAdapter(appsAdapter);
    }

    @Override public void onItemClick(View v, int pos)
    {
        AppItem ai = appLst.get(pos);
        if (ai.checked)
        {
            proxyApps.add(ai.pkgname);
        }
        else
        {
            proxyApps.remove(ai.pkgname);
        }
        Hawk.put("PerAppProxy", proxyApps);
    }

    @Override protected void onResume()
    {
        super.onResume();
        final AlertDialog ad = new AlertDialog.Builder(this)
                .setCancelable(false).setMessage("Please wait load list...")
                .show();
        //
        new Thread(new Runnable()
        {
            @Override public void run()
            {
                proxyApps = Hawk.get("PerAppProxy");
                //
                PackageManager pm = getPackageManager();
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ApplicationInfo> lst = pm.getInstalledApplications(0);
                for (ApplicationInfo appI : lst)
                {
                    if (appI.uid < 10000)
                    {
                        continue;
                    }
                    AppItem ai = new AppItem(appI.loadIcon(pm),
                                             appI.loadLabel(pm).toString(),
                                             appI.packageName,
                                             proxyApps.contains(appI.packageName));
                    appLst.add(ai);
                }
                ProxyAppsActivity.this.runOnUiThread(new Runnable()
                {
                    @Override public void run()
                    {
                        appsAdapter.notifyDataSetChanged();
                        ad.dismiss();
                    }
                });
            }
        }).start();
    }

    @Override protected void onPause()
    {
        super.onPause();
    }
}
