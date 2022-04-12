package com.afar.osaio.smart.scan.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.smart.bluetooth.activity.BaseBluetoothActivity;
import com.afar.osaio.smart.electrician.activity.AddDeviceActivity;
import com.afar.osaio.smart.electrician.activity.InputWiFiPsdActivity;
import com.afar.osaio.smart.electrician.adapter.DeviceSortAdapter;
import com.afar.osaio.smart.electrician.adapter.DeviceTitleAdapter;
import com.afar.osaio.smart.electrician.bean.DeviceGroupingBean;
import com.afar.osaio.smart.electrician.bean.DeviceTypeBean;
import com.afar.osaio.smart.home.tuyable.AddTuYaBlePopupWindows;
import com.afar.osaio.R;
import com.afar.osaio.application.activity.WebViewActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.tuyable.DeviceBleInfoBean;
import com.afar.osaio.smart.lpipc.activity.AddLpSuitWithRouterActivity;
import com.afar.osaio.smart.router.RouterConnectionWifiActivity;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.bean.SelectDeviceBean;
import com.afar.osaio.bean.SelectProduct;
import com.afar.osaio.smart.lpipc.activity.AddLpSuitActivity;
import com.afar.osaio.smart.lpipc.contract.AddCameraSelectContract;
import com.afar.osaio.smart.lpipc.presenter.AddCameraSelectPresenter;
import com.afar.osaio.smart.scan.adapter.ProductSelectListAdapter;
import com.afar.osaio.smart.scan.adapter.listener.ProductSelectListListener;
import com.afar.osaio.smart.smartlook.activity.AddBluetoothDeviceActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.tool.SystemUtil;
import com.tuya.smart.android.ble.api.ScanDeviceBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AddCameraSelectActivity extends BaseBluetoothActivity implements AddCameraSelectContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rcvDeviceTitle)
    RecyclerView rcvDeviceTitle;
    @BindView(R.id.rvProductSelect)
    RecyclerView rvProductSelect;
    @BindView(R.id.mbDividerLine)
    View mbDividerLine;

    @BindView(R.id.ivCloseIcon)
    ImageView ivCloseIcon;
    @BindView(R.id.topBarView)
    View topBarView;
    @BindView(R.id.tvNetworkWeakTip)
    TextView tvNetworkWeakTip;

    private boolean mIsNormalDenied = false;
    private AddCameraSelectContract.Presenter mPresenter;
    private DeviceTitleAdapter mDeviceTitleAdapter;
    private ProductSelectListAdapter mProductSelectAdapter;
    private String mSelectDeviceType;
    private Dialog mPrivacyPolicyDialog = null;
    List<SelectProduct> mCameraSelectProducts = new ArrayList<>();
    List<SelectProduct> mRouterSelectProducts = new ArrayList<>();
    private DeviceSortAdapter mDeviceSortAdapter;
    private List<Object> deviceGroupings;
    private AddTuYaBlePopupWindows addTuYaBlePopupWindows;
    private static final int TOPBAR_TYPE_BLUE = 0;
    private static final int TOPBAR_TYPE_LOCATION = 1;
    private static final int REQUEST_CODE_FOR_LOCATION = 1111;
    private int type = TOPBAR_TYPE_BLUE;
    /**
     * 用户选择的设备类型
     */
    private String addDeviceType;

    private static final String[] PERMS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH,
    };


    public static void toAddCameraSelectActivity(Context from) {
        Intent intent = new Intent(from, AddCameraSelectActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera_select);
        ButterKnife.bind(this);
        initData();
        setupView();
        initBle();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkBeforeScanningBluetooth(); //蓝牙+WiFi配网
    }

    private void initData() {
        deviceGroupings = new ArrayList<>();
        new AddCameraSelectPresenter(this);
        if (mPresenter != null) {
            showLoading();
            mPresenter.loadProductInfo();
        }
    }

    public void setupView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_title);
        mbDividerLine.setVisibility(View.VISIBLE);
        setupProductAndModelView();
        setupTuyaDeviceView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        releaseRes();
    }

    @Override
    public void setPresenter(@NonNull AddCameraSelectContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        if (mProductSelectAdapter != null) {
            mProductSelectAdapter.release();
            mProductSelectAdapter = null;
        }

        if (rvProductSelect != null) {
            rvProductSelect.setAdapter(null);
            rvProductSelect = null;
        }
    }

    /**
     * 打开涂鸦配网
     */
    private void showTuYaBlePopupWindows() {
        if (addTuYaBlePopupWindows != null) {
            addTuYaBlePopupWindows.dismiss();
        }
        addTuYaBlePopupWindows = new AddTuYaBlePopupWindows(this, false, false);

        addTuYaBlePopupWindows.setListener(new AddTuYaBlePopupWindows.AddTuYaBleListener() {

            @Override
            public void onSelectSmartClick(DeviceBleInfoBean deviceBleInfoBean) {
                if (deviceBleInfoBean != null && deviceBleInfoBean.getScanDeviceBean() != null) {
                    ScanDeviceBean scanDeviceBean = deviceBleInfoBean.getScanDeviceBean();
                    InputWiFiPsdActivity.toInputWiFiPsdActivity(AddCameraSelectActivity.this, ConstantValue.BLUE_MODE, ConstantValue.ADD_DEFAULT,
                            scanDeviceBean.getDeviceType(), scanDeviceBean.getUuid(), scanDeviceBean.getAddress(), scanDeviceBean.getMac());
                }
            }


            @Override
            public void startLeScanSuccess(ScanDeviceBean bean) { //搜索到了设备
                View activity = findViewById(R.id.lay_add_camera_select);
                activity.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addTuYaBlePopupWindows.showAtLocation(activity, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    }
                }, 200);
            }

            @Override
            public void closePopView() {

            }

            @Override
            public void startLeScanEmpty() {

            }
        });


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick({R.id.ivLeft, R.id.tvAction})
    public void onViewClicked(View view) {
        NooieLog.d("onViewClicked---view.getId()=" + view.getAccessibilityClassName());
        switch (view.getId()) {

            case R.id.ivLeft:
                finish();
                break;

            case R.id.tvAction:
                if (type == TOPBAR_TYPE_BLUE) {
                    BluetoothHelper.startBluetooth(this, ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE);
                } else if (type == TOPBAR_TYPE_LOCATION) {
                    if (!BluetoothHelper.isLocationEnabled(NooieApplication.mCtx)) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        // showCheckLocationEnableDialog(getResources().getString(R.string.bluetooth_scan_operation_tip_disconnect_location));
                    }
                }
                break;

        }
    }

    /**
     * update ui by current device type
     */
    private void gotoAddDevice(SelectDeviceBean deviceBean) {

        if (deviceBean == null) {
            return;
        }
        int productType = deviceBean.getProductType();
        if (productType == ConstantValue.PRODUCT_TYPE_LOCK) {
            if (!checkLogin("", "")) {
                return;
            }
            if (isBluetoothReady()) {
                AddBluetoothDeviceActivity.toAddBluetoothDeviceActivity(this);
            } else {
                checkBluetooth(getString(R.string.add_bluetooth_device_check_location_enable), true);
            }
        } else if (productType == ConstantValue.PRODUCT_TYPE_ROUTER) {
            showPopMenu();
        } else if (productType == ConstantValue.PRODUCT_TYPE_LOW_POWER) {
            if (NooieDeviceHelper.mergeIpcType(deviceBean.getType()) == IpcType.HC320) {
                gotoConnectionModeActivity(deviceBean);
                return;
            }
            if (!checkLogin("", "")) {
                return;
            }
            Bundle param = new Bundle();
            param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, deviceBean.getType());
            if (NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(deviceBean.getType())) != IpcType.EC810_CAM) {
                AddLpSuitWithRouterActivity.toAddLpSuitWithRouterActivity(this, param);
            } else {
                AddLpSuitActivity.toAddLpSuitActivity(this, param);
            }
        } else if (productType == ConstantValue.PRODUCT_TYPE_CARD || productType == ConstantValue.PRODUCT_TYPE_GUN || productType == ConstantValue.PRODUCT_TYPE_HEAD || productType == ConstantValue.PRODUCT_TYPE_MINI) {
            /*
            mSelectDeviceType = IpcType.getIpcType(deviceBean.getType()) != IpcType.IPC_UNKNOWN ? deviceBean.getType() : IpcType.PC420.getType();
            ConnectionModeActivity.toConnectionModeActivity(this, mSelectDeviceType);
             */
            gotoConnectionModeActivity(deviceBean);
        } else {
        }

        NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_SELECT_DEVICE_TYPE, NooieDeviceHelper.createSelectDeviceTypeDNExternal(deviceBean.getType()));
    }

    private void showPopMenu() {
        boolean isGoToLogin = !checkLogin("", "");
        if (isGoToLogin) {
            return;
        }
        RouterConnectionWifiActivity.toRouterConnectionWifiActivity(AddCameraSelectActivity.this);
    }

    private void gotoConnectionModeActivity(SelectDeviceBean deviceBean) {
        mSelectDeviceType = deviceBean != null && IpcType.getIpcType(deviceBean.getType()) != IpcType.IPC_UNKNOWN ? deviceBean.getType() : IpcType.PC420.getType();
        ConnectionModeActivity.toConnectionModeActivity(this, mSelectDeviceType);
    }

    private void gotoAmazon(String model) {
        if (model.equalsIgnoreCase("LOCK")) {
        } else if (model.equalsIgnoreCase("GATEWAY")) {
            SystemUtil.gotoBrower(NooieApplication.mCtx, ConstantValue.AMAZON_VICTURE_GATEWAY);
        } else {
            SystemUtil.gotoBrower(NooieApplication.mCtx, ConstantValue.AMAZON_VICTURE_IPC);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms) && !mIsNormalDenied) {
            mIsNormalDenied = false;
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onLoadProductInfoResult(String result, List<SelectProduct> selectProducts) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equals(result)) {
            initRouterProductList(createRouterProducts());
            initCameraProductList(selectProducts);
            updateProductView();
        }
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }

    private void setupProductAndModelView() {
        mDeviceTitleAdapter = new DeviceTitleAdapter();
        mDeviceTitleAdapter.setListener(new DeviceTitleAdapter.DeviceTitleListener() {
            @Override
            public void onItemClick(int position) {
                showProductView(position);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvDeviceTitle.setLayoutManager(layoutManager);
        rcvDeviceTitle.setAdapter(mDeviceTitleAdapter);

        mProductSelectAdapter = new ProductSelectListAdapter();
        mProductSelectAdapter.setListener(new ProductSelectListListener() {
            @Override
            public void onItemClick(SelectProduct data) {
            }

            @Override
            public void onItemLongClick(SelectProduct data) {
            }

            @Override
            public void onModelItemClick(SelectDeviceBean selectDevice) {
                dealOnSelectDevice(selectDevice);
            }
        });

        setCameraDeviceAdapter();
    }

    private void setCameraDeviceAdapter() {
        LinearLayoutManager productLayoutManager = new LinearLayoutManager(this);
        rvProductSelect.setLayoutManager(productLayoutManager);
        rvProductSelect.setAdapter(mProductSelectAdapter);
    }

    private void dealOnSelectDevice(SelectDeviceBean deviceBean) {
        boolean isShowPrivacyPolicy = !GlobalPrefs.getIgnorePrivacy();
        if (isShowPrivacyPolicy) {
            showPrivacyPolicyDialog(deviceBean);
        } else {
            gotoAddDevice(deviceBean);
        }
    }

    private void initCameraProductList(List<SelectProduct> selectProducts) {
        if (mCameraSelectProducts == null) {
            mCameraSelectProducts = new ArrayList<>();
        }
        mCameraSelectProducts.clear();
        mCameraSelectProducts.addAll(CollectionUtil.safeFor(selectProducts));
    }

    private void initRouterProductList(List<SelectProduct> selectProducts) {
        if (mRouterSelectProducts == null) {
            mRouterSelectProducts = new ArrayList<>();
        }
        mRouterSelectProducts.clear();
        mRouterSelectProducts.addAll(CollectionUtil.safeFor(selectProducts));
    }

    private List<SelectProduct> createRouterProducts() {
        List<SelectProduct> selectProducts = new ArrayList<>();
        SelectProduct selectProduct = new SelectProduct();
        selectProduct.setName("Router");
        selectProduct.setType(0);
        selectProduct.setEnable(true);
        List<SelectDeviceBean> children = new ArrayList<>();
        SelectDeviceBean selectDeviceBean = new SelectDeviceBean();
        selectDeviceBean.setName("R2");
        selectDeviceBean.setType("R2");
        selectDeviceBean.setProductType(0);
        selectDeviceBean.setEnable(true);
        children.add(selectDeviceBean);
        selectProduct.setChildren(children);
        selectProducts.add(selectProduct);
        return selectProducts;
    }

    private void updateProductView() {
        if (isDestroyed() || checkNull(mDeviceTitleAdapter)) {
            return;
        }

        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.camera));
        titles.add(getResources().getString(R.string.electrical));
        titles.add(getString(R.string.small_appliances));
        titles.add(getResources().getString(R.string.lighting));
        titles.add(getString(R.string.add_camera_product_category_router));
        mDeviceTitleAdapter.setData(titles);
        mDeviceTitleAdapter.changeSelected(0);
        showProductView(0);
    }

    private void showProductView(int type) {
        if (isDestroyed() || checkNull(mProductSelectAdapter)) {
            return;
        }
        if (type == 0) {
            setCameraDeviceAdapter();
            mProductSelectAdapter.setData(mCameraSelectProducts);
        } else if (type == 1) {
            setupElectricalList();
            setTuyaDeviceAdapter();
            mDeviceSortAdapter.setData(deviceGroupings);
        } else if (type == 2) {
            setupSmallApplicancesList();
            setTuyaDeviceAdapter();
            mDeviceSortAdapter.setData(deviceGroupings);
        } else if (type == 3) {
            setupLightingList();
            setTuyaDeviceAdapter();
            mDeviceSortAdapter.setData(deviceGroupings);
        } else if (type == 4) {
            setCameraDeviceAdapter();
            mProductSelectAdapter.setData(mRouterSelectProducts);
        }
        /*if (type == 1) {
            mProductSelectAdapter.setData(mRouterSelectProducts);
        } else {
            mProductSelectAdapter.setData(mCameraSelectProducts);
        }*/
    }

    private void showPrivacyPolicyDialog(SelectDeviceBean deviceBean) {
        hidePrivacyPolicyDialog();
        mPrivacyPolicyDialog = DialogUtils.showPrivacyPolicyDialog(this, getString(R.string.privacy_policy), getString(R.string.add_camera_privary_policy), getString(R.string.add_camera_privary_policy_ignore), getString(R.string.add_camera_privary_policy_confirm), getString(R.string.terms_of_service), getString(R.string.privacy_policy), new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                gotoAddDevice(deviceBean);
            }
        }, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GlobalPrefs.setIsIgnorePrivacy(isChecked);
            }
        }, new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                WebViewActivity.toWebViewActivity(AddCameraSelectActivity.this, WebViewActivity.getUrl(CommonUtil.getTerms(NooieApplication.mCtx)), getString(R.string.terms_of_service));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        }, new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                WebViewActivity.toWebViewActivity(AddCameraSelectActivity.this, WebViewActivity.getUrl(CommonUtil.getPrivacyPolicyByCountry(NooieApplication.mCtx, CountryUtil.getCurrentCountry(NooieApplication.mCtx))), getString(R.string.privacy_policy));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        });
    }

    private void hidePrivacyPolicyDialog() {
        if (mPrivacyPolicyDialog != null) {
            mPrivacyPolicyDialog.dismiss();
            mPrivacyPolicyDialog = null;
        }
    }

    private void setupTuyaDeviceView() {
        mDeviceSortAdapter = new DeviceSortAdapter();
        setupElectricalList();
        mDeviceSortAdapter.setOnItemClickListener(new DeviceSortAdapter.OnItemClickListener() {
            @Override
            public void onClick(View itemView, int position, String name) {
                NooieLog.e("----------postion " + position + "  name " + name);
                addDeviceType = name;
                openAddDeviceActivity(name);
            }
        });
        setTuyaDeviceAdapter();
        mDeviceSortAdapter.setData(deviceGroupings);
    }

    private void openAddDeviceActivity(String name) {
        if (name.equals(getString(R.string.plug_lower) + "\n(Wi-Fi)") || name.equals((getString(R.string.plug_lower) + "\n" + getString(R.string.ble_wifi)))) {
            AddDeviceActivity.toAddDeviceActivity(AddCameraSelectActivity.this, ConstantValue.ADD_DEVICE, false, name);
        } else if (name.equals(getString(R.string.switch_teckin) + "\n(Wi-Fi)")) {
            AddDeviceActivity.toAddDeviceActivity(AddCameraSelectActivity.this, ConstantValue.ADD_SWITCH, false, name);
        } else if (name.equals(getString(R.string.power_strip) + "\n(Wi-Fi)")) {
            AddDeviceActivity.toAddDeviceActivity(AddCameraSelectActivity.this, ConstantValue.ADD_POWERSTRIP, false, name);
        } else if (name.equals(getString(R.string.bulb) + "\n(Wi-Fi)") || name.equals(getString(R.string.bulb) + "\n" + getString(R.string.ble_wifi)) || name.equals(getString(R.string.floor_lamp) + "\n(Wi-Fi)")) {
            AddDeviceActivity.toAddDeviceActivity(AddCameraSelectActivity.this, ConstantValue.ADD_LAMP, false, name);
        } else if ((name.equals(getString(R.string.strip_light) + "\n" + getString(R.string.ble_wifi)))) {
            AddDeviceActivity.toAddDeviceActivity(AddCameraSelectActivity.this, ConstantValue.ADD_LIGHT_STRIP, false, name);
        } else if (name.equals(getString(R.string.dimmer_switch) + "\n(Wi-Fi)")) {
            AddDeviceActivity.toAddDeviceActivity(AddCameraSelectActivity.this, ConstantValue.ADD_LIGHT_MODULATOR, false, name);
        } else if (name.equals(getString(R.string.pet_feeder) + "\n" + getString(R.string.ble_wifi))) {
            AddDeviceActivity.toAddDeviceActivity(AddCameraSelectActivity.this, ConstantValue.ADD_PET_FEEDER, false, name);
        } else if (name.equals(getString(R.string.air_purifier) + "\n" + getString(R.string.ble_wifi))) {
            AddDeviceActivity.toAddDeviceActivity(AddCameraSelectActivity.this, ConstantValue.ADD_AIR_PURIFIER, false, name);
        }
    }

    private void setTuyaDeviceAdapter() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (deviceGroupings == null ){
                    NooieLog.e("AddCameraSelectActivity----getSpanSize() error,deviceGroupings =null");
                    return 0;
                }
                if ( position >deviceGroupings.size() || position ==deviceGroupings.size()){
                    NooieLog.e("AddCameraSelectActivity----getSpanSize() error,position >deviceGroupings.size,position="+position+",deviceGroupings.size()="+deviceGroupings.size());
                    return 0;
                }
                if (deviceGroupings.get(position) instanceof DeviceGroupingBean) {
                    return 3;
                } else {
                    return 1;
                }

            }
        });
        rvProductSelect.setLayoutManager(layoutManager);
        rvProductSelect.setAdapter(mDeviceSortAdapter);
    }

    private void setupElectricalList() {
        DeviceTypeBean space = new DeviceTypeBean();
        DeviceTypeBean plug = new DeviceTypeBean();
        plug.setDeviceName(getString(R.string.plug_lower) + "\n(Wi-Fi)");
        plug.setDevicePic(R.drawable.ic_device_socket_single);
        DeviceTypeBean plugBle = new DeviceTypeBean();
        plugBle.setDeviceName(getString(R.string.plug_lower) + "\n" + getString(R.string.ble_wifi));
        plugBle.setDevicePic(R.drawable.ic_device_socket_single);
        DeviceTypeBean powerstrip = new DeviceTypeBean();
        powerstrip.setDeviceName(getString(R.string.power_strip) + "\n(Wi-Fi)");
        powerstrip.setDevicePic(R.drawable.ic_device_socket_multiple);
        DeviceTypeBean switchBean = new DeviceTypeBean();
        switchBean.setDeviceName(getString(R.string.switch_teckin) + "\n(Wi-Fi)");
        switchBean.setDevicePic(R.drawable.ic_device_switch);
        DeviceTypeBean dimmer = new DeviceTypeBean();
        dimmer.setDeviceName(getString(R.string.dimmer_switch) + "\n(Wi-Fi)");
        dimmer.setDevicePic(R.drawable.ic_device_switch_light);
        deviceGroupings.clear();
        deviceGroupings.add(plug);
        deviceGroupings.add(plugBle);
        deviceGroupings.add(powerstrip);
        //deviceGroupings.add(space);
        deviceGroupings.add(switchBean);
        deviceGroupings.add(dimmer);
    }

    private void setupLightingList() {
        DeviceTypeBean lamp = new DeviceTypeBean();
        lamp.setDeviceName(getString(R.string.bulb) + "\n(Wi-Fi)");
        lamp.setDevicePic(R.drawable.ic_device_light);
        DeviceTypeBean lampBluetooth = new DeviceTypeBean();
        lampBluetooth.setDeviceName(getString(R.string.bulb) + "\n" + getString(R.string.ble_wifi));
        lampBluetooth.setDevicePic(R.drawable.ic_device_light);
        DeviceTypeBean strip = new DeviceTypeBean();
        strip.setDeviceName(getString(R.string.strip_light) + "\n" + getString(R.string.ble_wifi));
        strip.setDevicePic(R.drawable.ic_device_light_strip);
        DeviceTypeBean floorLamp = new DeviceTypeBean();
        floorLamp.setDeviceName(getString(R.string.floor_lamp) + "\n(Wi-Fi)");
        floorLamp.setDevicePic(R.drawable.ic_device_light_fill);
        deviceGroupings.clear();
        deviceGroupings.add(lamp);
        deviceGroupings.add(lampBluetooth);
        deviceGroupings.add(strip);
        deviceGroupings.add(floorLamp);
    }

    private void setupSmallApplicancesList() {
        DeviceTypeBean feeder = new DeviceTypeBean();
        feeder.setDeviceName(getString(R.string.pet_feeder) + "\n" + getString(R.string.ble_wifi));
        feeder.setDevicePic(R.drawable.ic_device_light_feeder);
        DeviceTypeBean purifier = new DeviceTypeBean();
        purifier.setDeviceName(getString(R.string.air_purifier) + "\n" + getString(R.string.ble_wifi));
        purifier.setDevicePic(R.drawable.ic_device_purifier);
        deviceGroupings.clear();
        deviceGroupings.add(feeder);
        deviceGroupings.add(purifier);
    }
//------------------------------涂鸦蓝牙配网-------------------------------------

    private void checkBeforeScanningBluetooth() {
        NooieLog.d("isCheckLocationPermission = " + GlobalPrefs.getIsDenyLocationPermission());

        if (!EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION)) {
            if (GlobalPrefs.getIsDenyLocationPermission()) { //拒绝过位置权限后，不再请求权限
                return;
            }
            showCheckLocationPermDialog();
        } else if (!BluetoothHelper.isLocationEnabled(NooieApplication.mCtx)) {
            setTopBarType(TOPBAR_TYPE_LOCATION);
            showTopBar(true);
        } else if (!BluetoothHelper.isBluetoothOn()) {
            setTopBarType(TOPBAR_TYPE_BLUE);
            showTopBar(true);
        } else {
            showTopBar(false);
            showTuYaBlePopupWindows();
        }
    }


    @Override
    public void permissionsGranted(int requestCode) {
        super.permissionsGranted(requestCode);
        if (requestCode == REQUEST_CODE_FOR_LOCATION) {
            if (!BluetoothHelper.isLocationEnabled(NooieApplication.mCtx)) {
                setTopBarType(TOPBAR_TYPE_LOCATION);
            }
        }
    }

    @Override
    public void showCheckLocationPermDialog() {
        showCheckLocalPermForBluetoothDialog(new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION) && EasyPermissions.somePermissionDenied(AddCameraSelectActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                requestPermission(ConstantValue.PERM_GROUP_LOCATION, REQUEST_CODE_FOR_LOCATION);
            }

            @Override
            public void onClickLeft() {
                GlobalPrefs.setIsDenyLocationPermission(true);
                NooieLog.d("isCheckLocationPermission =setIsDenyLocationPermission(true) =" + GlobalPrefs.getIsDenyLocationPermission());

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE) {
            if (resultCode == RESULT_OK) {
                showTopBar(false);
                checkBeforeScanningBluetooth();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void bluetoothStateOffChange() {
        if (addTuYaBlePopupWindows != null) {
            addTuYaBlePopupWindows.dismiss();
        }
        //蓝牙关闭
        NooieLog.d("-->> debug AddCameraSelectActivity bluetoothStateOffChange() ");
        showTopBar(true);
    }

    @Override
    public void bluetoothStateOnChange() {
        super.bluetoothConnected();
        NooieLog.d("-->> debug AddCameraSelectActivity bluetoothStateOnChange() ");
        showTopBar(false);
    }

    public void setTopBarType(int typeNow) {
        if (typeNow == TOPBAR_TYPE_LOCATION) {
            type = TOPBAR_TYPE_LOCATION;
            tvNetworkWeakTip.setText(R.string.bluetooth_scan_operation_tip_disconnect_location);
        } else if (typeNow == TOPBAR_TYPE_BLUE) {
            type = TOPBAR_TYPE_BLUE;
            tvNetworkWeakTip.setText(R.string.bluetooth_scan_operation_tip_disconnect);
        }

    }

    public void showTopBar(boolean isShow) {
        if (isShow) {
            topBarView.animate()
                    .translationY(0).alpha(1.0f).scaleY(1.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            topBarView.setVisibility(View.VISIBLE);

                        }
                    });
        } else {
            topBarView.animate()
                    .translationY(-topBarView.getHeight() / 3).alpha(0.0f).setDuration(600).scaleY(0.33f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            topBarView.setVisibility(View.GONE);
                        }
                    });
        }
        topBarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTopBar(false);
            }
        });
    }
}
