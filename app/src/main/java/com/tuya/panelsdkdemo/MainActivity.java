package com.tuya.panelsdkdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.tuya.sdk.panel.base.presenter.TuyaPanel;
import com.tuya.smart.android.panel.TuyaPanelSDK;
import com.tuya.smart.android.panel.api.ITuyaPanelLoadCallback;
import com.tuya.smart.android.panel.api.ITuyaPressedRightMenuListener;
import com.tuya.smart.android.user.api.ILogoutCallback;
import com.tuya.smart.api.service.MicroServiceManager;
import com.tuya.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService;
import com.tuya.smart.demo_login.base.utils.LoginHelper;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHomeChangeListener;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.utils.ProgressUtil;
import com.tuya.smart.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
                            //clear the panel cache
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
        mAdapter = new HomeAdapter();
        homeRecycler.setAdapter(mAdapter);
        setListener();
        TuyaHomeSdk.getHomeManagerInstance().registerTuyaHomeChangeListener(mHomeChangeListener);
        getHomeList();
    }

    private void setListener() {
        //open the panel listener
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
                    //  group panel
                    TuyaPanelSDK.getPanelInstance().gotoPanelViewControllerWithGroup(TuyaPanelSDK.getCurrentActivity(), getService().getCurrentHomeId(), bean.getGroupId(), mLoadCallback);
                } else {
                    //  device panel
                    TuyaPanelSDK.getPanelInstance().gotoPanelViewControllerWithDevice(TuyaPanelSDK.getCurrentActivity(), getService().getCurrentHomeId(), bean.getDevId(), mLoadCallback);
                }
            }
        });

        //get the panel toolbar right menu listener
        TuyaPanelSDK.getPanelInstance().setPressedRightMenuListener(new ITuyaPressedRightMenuListener() {
            @Override
            public void onPressedRightMenu(String s, long groupId) {
                Toast.makeText(TuyaPanelSDK.getCurrentActivity(), s + " " + groupId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * you must implementation AbsBizBundleFamilyService
     *
     * @return AbsBizBundleFamilyService
     */
    private AbsBizBundleFamilyService getService() {
        return MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TuyaPanel.getInstance().onDestroy();
        TuyaHomeSdk.getHomeManagerInstance().unRegisterTuyaHomeChangeListener(mHomeChangeListener);
        TuyaHomeSdk.getHomeManagerInstance().onDestroy();
    }

    private void getHomeList() {
        ProgressUtil.showLoading(this, "Loading...");
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                if (!list.isEmpty()) {
                    HomeBean homeBean = list.get(0);
                    setCurrentHomeId(homeBean);
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

    /**
     * you must invoke this method
     *
     * @param homeBean current family homeBean
     */
    private void setCurrentHomeId(HomeBean homeBean) {
        getService().setCurrentHomeId(homeBean.getHomeId());
    }

    private void getCurrentHomeDetail() {
        TuyaHomeSdk.newHomeInstance(getService().getCurrentHomeId()).getHomeDetail(new ITuyaHomeResultCallback() {
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

    private ItemBean getItemBeanFromGroup(GroupBean groupBean) {
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

    private ItemBean getItemBeanFromDevice(DeviceBean deviceBean) {
        ItemBean itemBean = new ItemBean();
        itemBean.setDevId(deviceBean.getDevId());
        itemBean.setIconUrl(deviceBean.getIconUrl());
        itemBean.setTitle(deviceBean.getName());
        return itemBean;
    }

    public static class ItemBean {
        private String devId;
        private long groupId;
        private String iconUrl;
        private String title;

        public String getDevId() {
            return devId;
        }

        public void setDevId(String devId) {
            this.devId = devId;
        }

        public long getGroupId() {
            return groupId;
        }

        public void setGroupId(long groupId) {
            this.groupId = groupId;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
