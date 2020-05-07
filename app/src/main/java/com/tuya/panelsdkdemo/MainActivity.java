package com.tuya.panelsdkdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.tuya.panelsdkdemo.bean.ItemBean;
import com.tuya.sdk.panel.base.presenter.TuyaPanel;
import com.tuya.smart.android.panel.TuyaPanelSDK;
import com.tuya.smart.android.panel.api.ITuyaOpenUrlListener;
import com.tuya.smart.android.panel.api.ITuyaPanelLoadCallback;
import com.tuya.smart.android.panel.api.ITuyaPressedRightMenuListener;
import com.tuya.smart.android.panel.utils.ProgressUtil;
import com.tuya.smart.android.user.api.ILogoutCallback;
import com.tuya.smart.demo_login.base.utils.LoginHelper;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHomeChangeListener;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private long mCurrentHomeId;
    private HomeAdapter mAdapter;
    private ITuyaHomeChangeListener mHomeChangeListener = new ITuyaHomeChangeListener() {
        @Override
        public void onHomeAdded(long l) {

        }

        @Override
        public void onHomeInvite(long homeId, String homeName) {

        }

        @Override
        public void onHomeRemoved(long l) {

        }

        @Override
        public void onHomeInfoChanged(long l) {

        }

        @Override
        public void onSharedDeviceList(List<DeviceBean> list) {

        }

        @Override
        public void onSharedGroupList(List<GroupBean> list) {

        }

        @Override
        public void onServerConnectSuccess() {
            getHomeList();
        }
    };

    public static void open(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle("Panel-Test");
        toolbar.inflateMenu(R.menu.toolbar_main_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_logout) {
                    TuyaHomeSdk.getUserInstance().logout(new ILogoutCallback() {
                        @Override
                        public void onSuccess() {
                            //清除所有面板的缓存
                            TuyaPanelSDK.getPanelInstance().clearPanelCache();

                            LoginHelper.reLogin(MainActivity.this, false);
                        }

                        @Override
                        public void onError(String errorCode, String errorMsg) {

                        }
                    });

                }
                return false;
            }
        });
        RecyclerView homeRecycler = findViewById(R.id.home_recycler);
        homeRecycler.setLayoutManager(new LinearLayoutManager(this));
        homeRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new HomeAdapter(null);
        homeRecycler.setAdapter(mAdapter);
        setListener();
        TuyaHomeSdk.getHomeManagerInstance().registerTuyaHomeChangeListener(mHomeChangeListener);
        getHomeList();
    }

    private void getHomeList() {
        ProgressUtil.showLoading(this, "Loading...");
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                if (!list.isEmpty()) {
                    HomeBean homeBean = list.get(0);
                    mCurrentHomeId = homeBean.getHomeId();
                    getCurrentHomeDetail();
                } else {
                    ToastUtil.showToast(MainActivity.this, "家庭列表为空,请创建家庭");
                    ProgressUtil.hideLoading();
                }
            }

            @Override
            public void onError(String s, String s1) {
                ProgressUtil.hideLoading();
                Toast.makeText(MainActivity.this, s + "\n" + s1, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getCurrentHomeDetail() {
        TuyaHomeSdk.newHomeInstance(mCurrentHomeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                List<ItemBean> beans = new ArrayList<>(8);
                for (GroupBean groupBean : homeBean.getGroupList()) {
                    beans.add(getItemBeanFromGroup(groupBean));
                }
                for (DeviceBean deviceBean : homeBean.getDeviceList()) {
                    beans.add(getItemBeanFromDevice(deviceBean));
                }
                mAdapter.setData(beans);
                ProgressUtil.hideLoading();
            }

            @Override
            public void onError(String s, String s1) {
                ProgressUtil.hideLoading();
                Toast.makeText(MainActivity.this, s + "\n" + s1, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setListener() {
        //panel加载状态回调 回调里应使用 TuyaPanelSDK.getCurrentActivity()
        final ITuyaPanelLoadCallback mLoadCallback = new ITuyaPanelLoadCallback() {
            @Override
            public void onStart(String deviceId) {
                ProgressUtil.showLoading(TuyaPanelSDK.getCurrentActivity(), "Loading...");
            }

            @Override
            public void onError(String deviceId, int code, String error) {
                ProgressUtil.hideLoading();
                Toast.makeText(TuyaPanelSDK.getCurrentActivity(), "errorCode:" + code + ",errorString:" + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(String deviceId) {
                ProgressUtil.hideLoading();
            }

            @Override
            public void onProgress(String deviceId, int progress) {
            }
        };

        mAdapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final ItemBean bean, int position) {
                if (bean.getGroupId() > 0) {
                    TuyaPanelSDK.getPanelInstance().gotoPanelViewControllerWithGroup(TuyaPanelSDK.getCurrentActivity(), mCurrentHomeId, bean.getGroupId(), mLoadCallback);
                } else {
                    TuyaPanelSDK.getPanelInstance().gotoPanelViewControllerWithDevice(TuyaPanelSDK.getCurrentActivity(), mCurrentHomeId, bean.getDevId(), mLoadCallback);
                }
            }
        });

        //面板右上角点击回调
        TuyaPanelSDK.getPanelInstance().setPressedRightMenuListener(new ITuyaPressedRightMenuListener() {
            @Override
            public void onPressedRightMenu(String deviceId) {
                //通过 deviceId 可以实现面板内跳转其他面板的逻辑
                Toast.makeText(TuyaPanelSDK.getCurrentActivity(), "panelMore", Toast.LENGTH_SHORT).show();
            }
        });

        //面板内打开路由页面回调
        TuyaPanelSDK.getPanelInstance().setOpenUrlListener(new ITuyaOpenUrlListener() {
            @Override
            public void handleOpenURLString(String urlString) {
                Toast.makeText(TuyaPanelSDK.getCurrentActivity(), urlString, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TuyaPanel.getInstance().onDestroy();
        TuyaHomeSdk.getHomeManagerInstance().unRegisterTuyaHomeChangeListener(mHomeChangeListener);
        TuyaHomeSdk.getHomeManagerInstance().onDestroy();
    }

    /**
     * 从GroupBean中获取HomeItemBean
     *
     * @param groupBean
     * @return
     */
    public ItemBean getItemBeanFromGroup(GroupBean groupBean) {
        ItemBean itemBean = new ItemBean();
        itemBean.setGroupId(groupBean.getId());
        itemBean.setTitle(groupBean.getName());
        itemBean.setIconUrl(groupBean.getIconUrl());

        List<DeviceBean> deviceBeans = groupBean.getDeviceBeans();
        if (deviceBeans == null || deviceBeans.isEmpty()) {
            return null;
        } else {
            DeviceBean onlineDev = null;
            for (DeviceBean dev : deviceBeans) {
                if (dev != null) {
                    if (dev.getIsOnline()) {
                        onlineDev = dev;
                        break;
                    } else {
                        onlineDev = dev;
                    }
                }
            }
            itemBean.setDevId(onlineDev.getDevId());
            return itemBean;
        }
    }

    /**
     * 从DeviceBean中获取HomeItemBean
     *
     * @param deviceBean
     * @return
     */
    public ItemBean getItemBeanFromDevice(DeviceBean deviceBean) {
        ItemBean itemBean = new ItemBean();
        itemBean.setDevId(deviceBean.getDevId());
        itemBean.setIconUrl(deviceBean.getIconUrl());
        itemBean.setTitle(deviceBean.getName());
        return itemBean;
    }

}
