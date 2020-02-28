package com.tuya.panelsdkdemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.widget.Toast;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.panel.TuyaPanelSDK;
import com.tuya.smart.wrapper.api.TuyaWrapper;

public class TuyaSmartApp extends Application {

    private static TuyaSmartApp mInstance;

    public static TuyaSmartApp getAppContext() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (TextUtils.isEmpty(BuildConfig.TUYA_SMART_APPKEY) || TextUtils.isEmpty(BuildConfig.TUYA_SMART_SECRET)) {
            Toast.makeText(this, "appkey or appsecret is empty. \nPlease check your configuration", Toast.LENGTH_LONG).show();
        }
        mInstance = this;
        TuyaWrapper.init(this);
        TuyaPanelSDK.init(this, BuildConfig.TUYA_SMART_APPKEY, BuildConfig.TUYA_SMART_SECRET);
        L.setSendLogOn(BuildConfig.DEBUG);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
