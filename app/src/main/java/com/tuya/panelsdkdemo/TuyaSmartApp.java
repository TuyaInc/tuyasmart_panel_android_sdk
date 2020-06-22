package com.tuya.panelsdkdemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.widget.Toast;

import com.tuya.smart.android.panel.TuyaPanelSDK;
import com.tuya.smart.api.router.UrlBuilder;
import com.tuya.smart.api.service.RouteEventListener;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.utils.ToastUtil;
import com.tuya.smart.wrapper.api.TuyaWrapper;

public class TuyaSmartApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (TextUtils.isEmpty(BuildConfig.TUYA_SMART_APPKEY) || TextUtils.isEmpty(BuildConfig.TUYA_SMART_SECRET)) {
            Toast.makeText(this, "appkey or appsecret is empty. \nPlease check your configuration", Toast.LENGTH_LONG).show();
        }
        // fail router listener
        TuyaWrapper.init(this, new RouteEventListener() {
            @Override
            public void onFaild(int errorCode, UrlBuilder urlBuilder) {
                ToastUtil.shortToast(TuyaPanelSDK.getCurrentActivity(), urlBuilder.originUrl);
            }
        });
        TuyaHomeSdk.setDebugMode(BuildConfig.DEBUG);
        // init
        TuyaPanelSDK.init(this, BuildConfig.TUYA_SMART_APPKEY, BuildConfig.TUYA_SMART_SECRET);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
