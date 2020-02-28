package com.tuya.panelsdkdemo.base.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.tuya.panelsdkdemo.BuildConfig;
import com.tuya.panelsdkdemo.base.utils.ActivityUtils;
import com.tuya.panelsdkdemo.base.utils.DialogUtil;
import com.tuya.panelsdkdemo.base.utils.LoginHelper;
import com.tuya.panelsdkdemo.login.activity.LoginActivity;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.common.utils.TuyaUtil;
import com.tuya.smart.home.sdk.TuyaHomeSdk;


/**
 * Created by letian on 16/7/19.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.d("splash", "tuyaTime: " + TuyaUtil.formatDate(System.currentTimeMillis(), "yyyy-mm-dd hh:mm:ss"));

        if (isInitAppkey()) {
            gotoLogin();
        } else {
            showTipDialog();
        }
    }

    public void gotoLogin() {
        if (TuyaHomeSdk.getUserInstance().isLogin()) {//已登录，跳主页
            LoginHelper.afterLogin();
            ActivityUtils.gotoMainActivity(this);
        } else {
            ActivityUtils.gotoActivity(this, LoginActivity.class, ActivityUtils.ANIMATE_FORWARD, true);
        }
    }


    private void showTipDialog() {
        DialogUtil.simpleConfirmDialog(this, "appkey or appsecret is empty. \nPlease check your configuration", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }

    private boolean isInitAppkey() {
        return !TextUtils.isEmpty(BuildConfig.TUYA_SMART_APPKEY) && !TextUtils.isEmpty(BuildConfig.TUYA_SMART_SECRET);
    }

    public static String getInfo(String infoName, Context context) {
        ApplicationInfo e;
        try {
            e = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            return e.metaData.getString(infoName);
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        return "";
    }

}
