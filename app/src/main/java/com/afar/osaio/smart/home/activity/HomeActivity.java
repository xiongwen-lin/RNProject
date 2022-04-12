package com.afar.osaio.smart.home.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.text.TextUtils;
import android.view.Gravity;
import android.widget.PopupWindow;

import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.message.bean.MsgUnreadInfo;
import com.afar.osaio.notification.NotificationManager;
import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.smart.cache.UserInfoCache;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.electrician.eventbus.GroupManageEvent;
import com.afar.osaio.smart.electrician.eventbus.HomeChangeEvent;
import com.afar.osaio.smart.electrician.eventbus.HomeEvent;
import com.afar.osaio.smart.electrician.eventbus.HomeFamilyChangeEvent;
import com.afar.osaio.smart.electrician.eventbus.WeatherEvent;
import com.afar.osaio.smart.electrician.manager.DeviceManager;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.presenter.IHomePresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IHomeView;
import com.afar.osaio.smart.event.AccountStateEvent;
import com.afar.osaio.smart.event.DeviceChangeEvent;
import com.afar.osaio.smart.event.MsgCountUpdateEvent;
import com.afar.osaio.smart.event.NetworkChangeEvent;
import com.afar.osaio.smart.event.TabSelectedEvent;
import com.afar.osaio.smart.event.TabSwitchEvent;
import com.afar.osaio.smart.push.bean.PushActiveMessageExtras;
import com.afar.osaio.smart.push.firebase.analytics.FirebaseAnalyticsManager;
import com.afar.osaio.smart.push.firebase.analytics.FirebaseConstant;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.smart.scan.activity.ConnectionModeActivity;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.ToastUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nooie.common.base.GlobalData;
import com.nooie.common.base.SDKGlobalData;
import com.nooie.common.detector.TrackerRouterInfo;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.configure.LanguageUtil;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.graphics.BitmapUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.common.utils.tool.SystemUtil;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.sdk.api.network.base.bean.PushCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BannerResult;
import com.nooie.sdk.api.network.base.bean.entity.MsgActiveInfo;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.processor.cloud.CloudManager;
import com.nooie.sdk.device.DeviceCmdService;
import com.afar.osaio.BuildConfig;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.NooieBaseSupportActivity;
import com.nooie.sdk.db.dao.UserRegionService;
import com.nooie.sdk.db.entity.CountryCodeEntity;
import com.afar.osaio.smart.event.HomeActionEvent;
import com.afar.osaio.smart.event.SelectPortraitEvent;
import com.afar.osaio.smart.home.contract.HomeContract;
import com.afar.osaio.smart.home.fragment.HomeFragment;
import com.afar.osaio.smart.home.presenter.HomePresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.PhotoPopupWindows;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.WeatherBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.sdk.TuyaBaseSdk;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observer;

public class HomeActivity extends NooieBaseSupportActivity implements HomeContract.View, IHomeView, DeviceManager.IHomeChangeListenerCallBack,
        DeviceManager.IHomeStatusListenerCallBack {

    public static final int RESULT_CODE_KITKAT_PHOTO = 321;
    protected static final int RESULT_CODE_PHOTO = 322;
    protected static final int RESULT_CODE_CAMERA = 323;
    protected static final int RESULT_CODE_CLIP = 324;

    private int REQUEST_CODE_FOR_PHONE = 1;
    private int REQUEST_CODE_FOR_CAMERA = 2;
    private int REQUEST_CODE_FOR_BLUETOOTH = 3;
    private int REQUEST_CODE_FOR_STORAGE = 4;

    private String locationProvider;

    public static void toHomeActivity(Context from) {
        Intent intent = new Intent(from, HomeActivity.class);
        from.startActivity(intent);
    }

    public static void toHomeActivity(Context from, String refreshType) {
        Intent intent = new Intent(from, HomeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_REFRESH_TYPE, refreshType);
        from.startActivity(intent);
    }

    public static void toHomeActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, HomeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    private HomeContract.Presenter mPresenter;
    private PhotoPopupWindows mPopMenus;
    private boolean mIsNormalDenied = false;
    private int mCurrentPosition = HomeFragment.FIRST;
    private IHomePresenter mHomePresenter;
    private HomeBean mHomeBean;
    private boolean isFirstClickSmartHomeFragment = true;

    public static final String TYPE_ADD_DEVICE = "TYPE_ADD_DEVICE";
    public static final String TYPE_REMOVE_DEVICE = "TYPE_REMOVE_DEVICE";
    public static final String TYPE_ADD_GROUP = "TYPE_ADD_GROUP";
    public static final String TYPE_ADD_HOME = "TYPE_ADD_HOME";
    public static final String TYPE_REMOVE_GROUP = "TYPE_REMOVE_GROUP";
    public static final String TYPE_REMOVE_HOME = "TYPE_REMOVE_HOME";
    public static final String TYPE_LOGIN_HOME = "TYPE_LOGIN_HOME";

    private static final String[] PERMS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NooieLog.d("-->> debug HomeActivity onCreate: ");
        setContentView(R.layout.activity_home_page);
        if (findFragment(HomeFragment.class) == null) {
            loadRootFragment(R.id.fl_home_container, HomeFragment.newInstance());
        }

        initData();
        EventBus.getDefault().register(this);
        initDeviceCmdReceiver();
        //registerDeviceCmdReceiver();
        registerAppStateListener();
    }

    private void initData() {
        new HomePresenter(this);
        mHomePresenter = new com.afar.osaio.smart.electrician.presenter.HomePresenter(this);
        DeviceManager.getInstance().registerHomeChangeListener();
        DeviceManager.getInstance().setHomeChangeListenerCallBack(this);
        mHomePresenter.createDefaultHome(0);
        //NooieDeviceHelper.initNativeConnect();
        checkNotificationEnable();
        //checkPushPerm();
//        if (MyAccountHelper.getInstance().isLogin() && mPresenter != null) {
//            mPresenter.getUserInfo(mUid, mUserAccount);
//        }
        checkUserInfo();
        EventTrackingApi.getInstance().checkEventTracking();
        mCurrentPosition = HomeFragment.FIRST;
        //    private String logFileName;
        //    private String mAccount;
        //    private String mPassword;
        //    private String mUid;
        //    private String mToken;
        //    private String mRefreshToken;
        //    private long mExpireTime;
        //    private String mPushToken;
        //    private int mLoginType;
        //    private String mThirdPartyOpenId;
        //    private int mThirdPartyUserType;
        //    private String mWebUrl;
        //    private String mP2pUrl;
        //    private String mS3Url;
        //    private String mRegion;
        //    private String mSsUrl;
        //    private int mGapTime;
        //    private String mPhoneId;
        //    private boolean mGapTimeValid;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mAccount: " + SDKGlobalData.getInstance().getAccount())
                .append(" mPassword: " + SDKGlobalData.getInstance().getPassword())
                .append(" mUid: " + SDKGlobalData.getInstance().getUid())
                .append(" mToken: " + SDKGlobalData.getInstance().getToken())
                .append(" mRefreshToken: " + SDKGlobalData.getInstance().getRefreshToken())
                .append(" mExpireTime: " + SDKGlobalData.getInstance().getExpireTime())
                .append(" mPushToken: " + SDKGlobalData.getInstance().getPushToken())
                .append(" mLoginType: " + SDKGlobalData.getInstance().getLoginType())
                .append(" mThirdPartyOpenId: " + SDKGlobalData.getInstance().getThirdPartyOpenId())
                .append(" mThirdPartyUserType: " + SDKGlobalData.getInstance().getThirdPartyUserType())
                .append(" mWebUrl: " + SDKGlobalData.getInstance().getWebUrl())
                .append(" mP2pUrl: " + SDKGlobalData.getInstance().getP2pUrl())
                .append(" mS3Url: " + SDKGlobalData.getInstance().getS3Url())
                .append(" mRegion: " + SDKGlobalData.getInstance().getRegion())
                .append(" mSsUrl: " + SDKGlobalData.getInstance().getSsUrl())
                .append(" mGapTime: " + SDKGlobalData.getInstance().getGapTime())
                .append(" mPhoneId: " + SDKGlobalData.getInstance().getPhoneId())
                .append(" mGapTimeValid: " + SDKGlobalData.getInstance().getGapTimeValid())
                .append(" appid: " + NetConfigure.getInstance().getAppId())
                .append(" serect: " + NetConfigure.getInstance().getAppSecret())
                .append(" baseurl: " + NetConfigure.getInstance().getBaseUrl())
                .append(" userphoto: " + UserInfoCache.getInstance().getUserInfo().getPhoto());
        NooieLog.d("************************* info: " + stringBuilder.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        NooieLog.d("-->> debug HomeActivity onResume: ");
        initFirebaseAnalyticsInfo();
        checkIsNeedReportUserInfo();
        checkShowAd();
        getWeather();
        checkIsInitCloudManager();
        startNetworkDetect();
        updateApDeviceHardVersion();
        tryDisconnectBluetooth();
        ApHelper.getInstance().setIsEnterApDevicePage(false);
        checkMsgUnread();
    }

    @Override
    public void onPause() {
        super.onPause();
        NooieLog.d("-->> debug HomeActivity onPause: ");
        stopNetworkDetect();
    }

    @Override
    public void onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport();
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        // 设置横向(和安卓4.x动画相同)
        return new DefaultHorizontalAnimator();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NooieLog.d("-->> debug HomeActivity onDestroy: ");
        EventBus.getDefault().unregister(this);
        DeviceManager.getInstance().release();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        if (mHomePresenter != null) {
            mHomePresenter.release();
        }
        DeviceInfoCache.getInstance().clearCache();
        unInitDeviceCmdReceiver();
        //unregisterDeviceCmdReceiver();
        stopUpdateApDeviceHardVersion();
        unRegisterAppStateListener();

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String refreshType = intent.getStringExtra(ConstantValue.INTENT_KEY_REFRESH_TYPE);
        if (refreshType != null) {
            if (refreshType.equals(TYPE_ADD_GROUP)) {
                mHomePresenter.loadHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
            } else if (refreshType.equals(TYPE_LOGIN_HOME)) {
                DeviceManager.getInstance().registerHomeChangeListener();
                mHomePresenter.createDefaultHome(0);
            }
        }
        NooieLog.e("HomeActivity onNewIntent homeId " + FamilyManager.getInstance().getCurrentHomeId());

        NooieLog.d("-->> debug HomeActivity onNewIntent: home page action=" + getHomePageAction());
        if (getHomePageAction() == ConstantValue.HOME_PAGE_ACTION_SWITCH_CONNECTION_MODE) {
            if (!TextUtils.isEmpty(getDeviceModel())) {
                ConnectionModeActivity.toConnectionModeActivity(this, getDeviceModel());
            }
        }
    }

    private void updateUI(HomeBean homeBean) {
        mHomeBean = homeBean;
        DeviceManager.getInstance().syncGetDevList(homeBean);
        DeviceManager.getInstance().notifyLoadHomeDetailSuccess(homeBean);
        DeviceManager.getInstance().setHomeStatusListenerCallBack(this);
        DeviceManager.getInstance().registerHomeStatusListener(mHomeBean.getHomeId());
    }

    @Override
    public int getStatusBarMode() {
        /*if (mCurrentPosition == HomeFragment.SECOND || mCurrentPosition == HomeFragment.FOUR) {
            return ConstantValue.STATUS_BAR_DARK_MODE_2;
        }*/
       /* if (mCurrentPosition == HomeFragment.FIRST || mCurrentPosition == HomeFragment.THIRD) {
            return ConstantValue.STATUS_BAR_DARK_MODE_2;
        }*/
        return ConstantValue.STATUS_BAR_DARK_MODE;
    }

    @Override
    public void setPresenter(@NonNull HomeContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void permissionsGranted(int requestCode) {
        if (requestCode == REQUEST_CODE_FOR_PHONE) {
            // 若使用UPush，这里需要初始化UPush
            NooieApplication.get().initJPush(NooieApplication.DEBUG_MODE, new IUmengRegisterCallback() {
                @Override
                public void onSuccess(String deviceToken) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(PushAgent.getInstance(NooieApplication.mCtx).getRegistrationId())) {
                                reportUserInfo();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(String s, String s1) {
                }
            });
        } else if (requestCode == REQUEST_CODE_FOR_CAMERA) {
            startTakePhotoIntent();
        } else if (requestCode == REQUEST_CODE_FOR_BLUETOOTH) {
        } else if (requestCode == REQUEST_CODE_FOR_STORAGE) {
        } else if (requestCode == ConstantValue.REQUEST_CODE_FOR_LOCATION_PERM) {
            switch (mCurrentPosition) {
                case HomeFragment.FIRST: {
                    EventBusActivityScope.getDefault(this).post(new DeviceChangeEvent(DeviceChangeEvent.DEVICE_CHANGE_ACTION_FIND_GRAND_LOCATION_PERMISSION));
                    break;
                }
                case HomeFragment.SECOND: {
                    //添加第一个页面请求位置权限通过后的逻辑
                    getLocation();
                    break;
                }
            }
        }

    }

    private void getWeather() {
        if (BluetoothHelper.isLocationEnabled(NooieApplication.mCtx) && EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION)) {
            getLocation();
        }
    }

    private void getLocation() {
        //1.获取位置管理器
        LocationManager locationManager = (LocationManager) NooieApplication.mCtx.getSystemService(Context.LOCATION_SERVICE);
        //2.获取位置提供器，GPS或是NetWork
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是网络定位
            NooieLog.e("-----------homeActivity 有权限 网络定位 ");
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS定位
            NooieLog.e("-----------homeActivity 有权限  GPS定位");
            locationProvider = LocationManager.GPS_PROVIDER;
        } else {
            NooieLog.e("-----------homeActivity 有权限  没有可用的位置提供器");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            NooieLog.e("-----------homeActivity 有权限  location " + location.getLatitude() + "  " + location.getLongitude());
            mHomePresenter.setWeather(location.getLongitude(), location.getLatitude());
            TuyaBaseSdk.setLatAndLong(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
        } else {
            // 监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
            locationManager.requestLocationUpdates(locationProvider, 0, 0, mListener);
        }
    }

    LocationListener mListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        // 如果位置发生变化，重新显示
        @Override
        public void onLocationChanged(Location location) {
            mHomePresenter.setWeather(location.getLongitude(), location.getLatitude());
        }
    };

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == ConstantValue.REQUEST_CODE_FOR_LOCATION_PERM) {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_CODE_CAMERA:
                    //clipPicture(cropUri, saveUri);
                    Uri cropCameraPicUri = convertUriByPath(getLocalHeaderTmpPath());
                    Uri saveCameraPicUri = convertUriByPath(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount));
                    if (!clipPicture(cropCameraPicUri, saveCameraPicUri)) {
                        clipWithUcrop(Uri.fromFile(new File(getLocalHeaderTmpPath())), Uri.fromFile(new File(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount))));
                    }
                    break;
                case RESULT_CODE_PHOTO:
                    if (data == null || data.getData() == null) {
                        break;
                    }
                    Uri saveUri = Uri.fromFile(new File(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount)));
                    if (!clipPicture(data.getData(), saveUri)) {
                        clipWithUcrop(Uri.fromFile(new File(getLocalHeaderTmpPath())), Uri.fromFile(new File(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount))));
                    }
                    break;
                case RESULT_CODE_KITKAT_PHOTO:
                    if (data == null || data.getData() == null) {
                        break;
                    }
                    String imagePath = BitmapUtil.getPath(this, data.getData());
                    NooieLog.d("-->> HomeActivity test change portrait 5 image path=" + imagePath + " uri=" + data.getData().getPath());
                    Uri cropPicUri = data.getData();
                    Uri savePicUri = convertUriByPath(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount));
                    if (!clipPicture(cropPicUri, savePicUri)) {
                        clipWithUcrop(cropPicUri, Uri.fromFile(new File(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount))));
                    }

                    /*
                    String imagePath = BitmapUtil.getPath(this, data.getData());
                    NooieLog.d("-->> HomeActivity test change portrait 5 image path=" + imagePath);
                    Uri cropPicUri = convertUriByPath(imagePath);
                    Uri savePicUri = convertUriByPath(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount));
                    if (!clipPicture(cropPicUri, savePicUri)) {
                        clipWithUcrop(Uri.fromFile(new File(imagePath)), Uri.fromFile(new File(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount))));
                    }
                     */
                    break;
                case UCrop.REQUEST_CROP:
                case RESULT_CODE_CLIP:
                    try {
                        NooieLog.d("-->> HomeActivity test change portrait 10 savePicPath=" + FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount));
                        if (requestCode == UCrop.REQUEST_CROP) {
                            Uri resultUri = data != null ? UCrop.getOutput(data) : null;
                            NooieLog.d("-->> HomeActivity test change portrait 12 ucrop result uri=" + (resultUri != null ? resultUri.getPath() : ""));
                        }
                        EventBusActivityScope.getDefault(this).post(new SelectPortraitEvent(FileUtil.getAccountNamePortrait(NooieApplication.mCtx, mUserAccount)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onReturnFromSignInActivity() {
        EventBusActivityScope.getDefault(this).post(new TabSwitchEvent(HomeFragment.FIRST, true));
    }

    @Subscribe
    public void onHomeActionEvent(HomeActionEvent actionEvent) {
        if (actionEvent == null) {
            return;
        }

        if (actionEvent.action == HomeActionEvent.HOME_ACTION_SHOW_PHOTO_PICKER) {
            showPopMenu();
        } else if (actionEvent.action == HomeActionEvent.HOME_ACTION_ALBUM_STORAGE_PERMISSION) {
            if (!EasyPermissions.hasPermissions(NooieApplication.mCtx, CommonUtil.getStoragePermGroup())) {
                requestPermission(CommonUtil.getStoragePermGroup(), REQUEST_CODE_FOR_STORAGE);
            }
        } else if (actionEvent.action == HomeActionEvent.HOME_ACTION_LOCATION_PERMISSION) {
            NooieLog.d("-->> debug HomeActivity onHomeActionEvent: location denied=" + EasyPermissions.somePermissionDenied(this, ConstantValue.PERM_GROUP_LOCATION) + " mCurrentPosition=" + mCurrentPosition);
            if (checkUseLocationEnable()) {
                getWeather();//1.3.3,有权限，点击请求天气
                return;
            }
            switch (mCurrentPosition) {
                case HomeFragment.FIRST: {
                    //添加第一个页面请求位置权限的逻辑
                    requestLocationPerm(getString(R.string.enable_location_tips), true);
                    break;
                }
                case HomeFragment.SECOND: {
                    requestLocationPerm(getString(R.string.add_camera_input_wifi_psd_location_enable), false);
                    break;
                }
            }
        }
    }

    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        NooieLog.d("-->> debug HomeActivity onTabSelectedEvent: position=" + (event != null ? event.position : -1));
        NooieLog.d("-->> debug HomeActivity onTabSelectedEvent: isFirstClickSmartHomeFragment=" + isFirstClickSmartHomeFragment);
        if (event != null) {
            refreshStatusBar(event.position);
            mCurrentPosition = event.position;
            if (event.position == HomeFragment.SECOND || event.position == HomeFragment.THIRD) {
                checkMsgUnread();
            }
            /*if (event.position == HomeFragment.SECOND && isFirstClickSmartHomeFragment) {
                isFirstClickSmartHomeFragment = false;
                onRefresh();
            }*/
        }
    }

    @Subscribe
    public void onAccountStateEvent(AccountStateEvent event) {
        if (event == null) {
            return;
        }
        if (event.state == AccountStateEvent.ACCOUNT_STATE_REFRESH_AFTER_LOGIN) {
            checkIsNeedReportUserInfo();
            checkIsInitCloudManager();
            checkUserInfo();
            EventBusActivityScope.getDefault(this).post(new AccountStateEvent(AccountStateEvent.ACCOUNT_STATE_REFRESH_AFTER_LOGIN));
        }
    }

    private void initNooieConnect() {
        try {
            String[] ipAndPort = !TextUtils.isEmpty(GlobalData.getInstance().getP2pUrl()) ? GlobalData.getInstance().getP2pUrl().split(":") : null;
            if (ipAndPort == null || ipAndPort.length < 2) {
                return;
            }
            String ip = ipAndPort[0];
            String port = ipAndPort[1];
            DeviceCmdService.getInstance(NooieApplication.mCtx).initConn(GlobalData.getInstance().getUid(), ip, Integer.valueOf(port));
        } catch (Exception e) {
        }
    }

    private void checkNotificationEnable() {
        try {
            GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
            NooieLog.d("-->> HomeActivity checkNotificationEnable enable=" + NotificationUtil.isNotificationEnabled(NooieApplication.mCtx));
            if (!NotificationUtil.isNotificationEnabled(NooieApplication.mCtx) && prefs.getAppNotificationRequest() != ConstantValue.APP_NOTIFICATION_REQUEST_YES) {
                prefs.setAppNotificationRequest(ConstantValue.APP_NOTIFICATION_REQUEST_YES);
                DialogUtils.showConfirmWithSubMsgDialog(this, R.string.home_notification_request_title, R.string.home_notification_request_content, R.string.cancel, R.string.continue_upper, new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        NotificationUtil.requestNotify(NooieApplication.mCtx);
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    private void checkPushPerm() {
        if (!NooieApplication.get().getIsUseJPush()) {
            //report user info once
            if (isFirstLaunch()) {
                reportUserInfo();
            }
        } else if (NooieApplication.get().getIsUseJPush() && EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_PHONE)) {
            requestPermission(ConstantValue.PERM_GROUP_PHONE, REQUEST_CODE_FOR_PHONE);
        } else {
            //report user info once
            if (isFirstLaunch()) {
                reportUserInfo();
            }
            requestPermission(ConstantValue.PERM_GROUP_PHONE, REQUEST_CODE_FOR_PHONE);
        }
    }

    private void checkIsNeedReportUserInfo() {
        String token = NooiePushMsgHelper.getPushToken();
        boolean isNeedUpdateToken = MyAccountHelper.getInstance().isLogin() && NooiePushMsgHelper.isPushTokenChange(GlobalData.getInstance().getPushToken(), token);
        boolean isEnableReportUserInfo = MyAccountHelper.getInstance().isLogin() && !NooiePushMsgHelper.isPushTokenValid(GlobalData.getInstance().getPushToken());
        if (isNeedUpdateToken || isEnableReportUserInfo) {
            reportUserInfo();
        }
    }

    private void reportUserInfo() {
        if (NooiePushMsgHelper.isPushTokenValid(NooiePushMsgHelper.getPushToken())) {
            reportUserInfo(NooiePushMsgHelper.getPushType(), NooiePushMsgHelper.getPushToken());
        } else if (NooiePushMsgHelper.getPushType() == ApiConstant.PUSH_TYPE_FCM) {
            try {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (task == null || !task.isSuccessful()) {
                                    return;
                                }
                                String pushToken = task.getResult();
                                NooieLog.d("-->> debug HomeActivity reportUserInfo fcm onComplete: pushToken=" + pushToken);
                                if (NooiePushMsgHelper.isPushTokenValid(pushToken)) {
                                    reportUserInfo(NooiePushMsgHelper.getPushType(), pushToken);
                                }
                            }
                        });
            } catch (Exception e) {
            }
        }
    }

    private void reportUserInfo(int pushType, String token) {
        if (!MyAccountHelper.getInstance().isLogin()) {
            return;
        }
        //nooie smart 确保只上报一次用户和设备信息
        CountryCodeEntity countryCodeEntity = UserRegionService.getInstance().getUserRegionByAccount(mUserAccount);
        String countryCode = countryCodeEntity != null && !TextUtils.isEmpty(countryCodeEntity.getCountryCode()) ? countryCodeEntity.getCountryCode() : CountryUtil.getCurrentCountry(NooieApplication.mCtx);
        String appVersion = BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")";
        String screenSize = DisplayUtil.SCREEN_WIDTH_PX + "*" + DisplayUtil.SCREEN_HIGHT_PX;
        String language = LanguageUtil.getLocal(NooieApplication.mCtx).getLanguage();
        String nickname = "";
        String photoUrl = "";
        if (!TextUtils.isEmpty(appVersion) && BuildConfig.DEBUG) {
            appVersion = appVersion.replace("_debug", "");
        }
        if (mPresenter != null) {
            NooieLog.d("-->> HomeActivity put user info account=" + mUserAccount + " pushType=" + pushType + " pushToken=" + token + "model=" + Build.MODEL + " brand=" + Build.BRAND + " phoneCode=" + GlobalData.getInstance().getPhoneId());
            mPresenter.reportUserInfo(GlobalData.getInstance().getAccount(), GlobalData.getInstance().getPassword(), CountryUtil.getCurrentTimeZone(), countryCode, ApiConstant.PLATFORM_TYPE_ANDROID, nickname, photoUrl,
                    GlobalData.getInstance().getPhoneId(), ApiConstant.DEVICE_TYPE, pushType, token, appVersion, String.valueOf(BuildConfig.VERSION_CODE), Build.MODEL, Build.BRAND, String.valueOf(Build.VERSION.SDK_INT), screenSize, language, BuildConfig.APPLICATION_ID);
        }
    }

    private void checkIsInitCloudManager() {
        if (!CloudManager.getInstance().getIsInitCache()) {
            NooieLog.d("-->> HomeActivity checkIsInitCloudManager detection thumbnail root path=" + FileUtil.getDetectionThumbnailRootPathInPrivate(NooieApplication.mCtx, mUserAccount).getPath() + " uid=" + mUid);
            CloudManager.getInstance().loadDetectionThumbnails(FileUtil.getDetectionThumbnailRootPathInPrivate(NooieApplication.mCtx, mUserAccount).getPath(), mUid);
        }
    }

    private void checkUserInfo() {
        if (MyAccountHelper.getInstance().isLogin() && mPresenter != null) {
            mPresenter.getUserInfo(mUid, mUserAccount);
        } else if (mPresenter != null) {
            mPresenter.clearLogFile();
        }
    }

    private void showPopMenu() {
        if (mPopMenus != null) {
            mPopMenus.dismiss();
        }

        mPopMenus = new PhotoPopupWindows(this, new PhotoPopupWindows.OnClickSelectPhotoListener() {
            @Override
            public void onClick(boolean takePhoto) {
                if (!EasyPermissions.hasPermissions(NooieApplication.mCtx, CommonUtil.getStoragePermGroup())) {
                    requestPermission(CommonUtil.getStoragePermGroup(), REQUEST_CODE_FOR_STORAGE);
                    return;
                }
                if (takePhoto) {
                    requestPermission(ConstantValue.PERM_GROUP_CAMERA, REQUEST_CODE_FOR_CAMERA);
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        // 4.4版本
                        startActivityForResult(intent, RESULT_CODE_KITKAT_PHOTO);
                    } else {
                        startActivityForResult(intent, RESULT_CODE_PHOTO);
                    }
                }
            }
        });
        mPopMenus.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mPopMenus = null;
            }
        });

        mPopMenus.showAtLocation(this.findViewById(R.id.fl_home_container),
                Gravity.TOP | Gravity.BOTTOM, 0, 0);
    }

    private Uri convertUriByPath(String path) {
        Uri uri = null;
        try {
            NooieLog.d("-->> HomeActivity test change portrait 6 path=" + path);
            File tmpFile = new File(path);
            if (tmpFile == null || !tmpFile.exists()) {
                NooieLog.d("-->> HomeActivity test change portrait 7 tmpfile not exist");
                //return null;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(this, "com.afar.osaio.provider", new File(path));
                NooieLog.d("-->> HomeActivity test change portrait 8 uri=" + (uri != null ? uri.getPath() : ""));
            } else {
                uri = Uri.fromFile(new File(path));
                NooieLog.d("-->> HomeActivity test change portrait 9 uri=" + (uri != null ? uri.getPath() : ""));
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return uri;
    }

    private void startTakePhotoIntent() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getLocalHeaderTmpPath())));
        Uri cropCameraPicUri = convertUriByPath(getLocalHeaderTmpPath());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropCameraPicUri);
        startActivityForResult(intent, RESULT_CODE_CAMERA);
    }

    private String getLocalHeaderTmpPath() {
        return FileUtil.getTmpAccountNamePortrait(NooieApplication.mCtx, mUserAccount);
    }

    private boolean clipPicture(Uri inData, Uri outData) {
        try {
            Intent intent = new Intent();
            intent.setAction("com.android.camera.action.CROP");
            intent.setDataAndType(inData, "image/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 加入访问权限
                grantUriPermission(this, intent, inData);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.putExtra("crop", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 300);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outData);
            intent.putExtra("dragAndScale", true);
            intent.putExtra("scaleUpIfNeeded", true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 加入访问权限
                grantUriPermission(this, intent, outData);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            NooieLog.d("-->> HomeActivity test change portrait 11 crop is supported=" + (intent.resolveActivity(getPackageManager()) != null));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, RESULT_CODE_CLIP);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clipWithUcrop(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(300, 300)
                .start(this);
    }

    public static void grantUriPermission(Context context, Intent intent, Uri uri) {
        if (context == null || intent == null || uri == null) {
            return;
        }
        try {
            List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resolveInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyReportUserInfoResult(String result) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
        } else {
        }
    }

    private AlertDialog mShowAdDialog;

    private void showAdDialog(String title, String content, final String url) {
        hideAdDialog();
        mShowAdDialog = DialogUtils.showPushActiveDialog(this, title, content, getString(R.string.home_ad_go_to_brower), true, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                if (!TextUtils.isEmpty(url)) {
                    SystemUtil.gotoBrower(NooieApplication.mCtx, url);
                }
            }
        });
    }

    private void hideAdDialog() {
        if (mShowAdDialog != null) {
            mShowAdDialog.dismiss();
            mShowAdDialog = null;
        }
    }

    public boolean isShowingAdDialog() {
        return mShowAdDialog != null && mShowAdDialog.isShowing();
    }

    private void checkShowAd() {
        NooieLog.d("-->> HomeActivity local time=" + Calendar.getInstance().getTimeInMillis() + " expired time=" + DateTimeUtil.getTimeMillis(2020, 1, 17, 0, 0, 0));
        boolean isActiveAvailable = MyAccountHelper.getInstance().isLogin() && Calendar.getInstance().getTimeInMillis() < DateTimeUtil.getTimeMillis(2030, 1, 17, 0, 0, 0);
        if (isActiveAvailable && !isShowingAdDialog() && mPresenter != null) {
            mPresenter.getLastActiveMsg();
        }
    }

    @Override
    public void onGetLastActiveMsgResult(String result, MsgActiveInfo msgActiveInfo) {
        if (isDestroyed()) {
            return;
        }

        NooieLog.d("-->> HomeActivity onGetLastActiveMsgResult result=" + result);
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result) && msgActiveInfo != null) {
            NooieLog.d("-->> HomeActivity onGetLastActiveMsgResult id=" + msgActiveInfo.getId() + " status=" + msgActiveInfo.getStatus() + " msg=" + msgActiveInfo.getMsg());
            boolean isShowAd = msgActiveInfo.getStatus() == ApiConstant.SYS_MSG_STATUS_UNREAD && !TextUtils.isEmpty(msgActiveInfo.getMsg());
            if (isShowAd) {
                showAdDialog(getString(R.string.home_ad_dialog_title), msgActiveInfo.getMsg(), msgActiveInfo.getUrl());
                if (mPresenter != null) {
                    NotificationManager.getInstance().cancelNotificationById(PushCode.PUSH_ACTIVE.code);
                    mPresenter.updateMsgReadState(msgActiveInfo.getId(), ApiConstant.MSG_TYPE_SYS);
                }
            }
        }
    }

    @Override
    public void showPushActiveMessage(PushActiveMessageExtras pushActiveExtras) {
        if (isDestroyed() || pushActiveExtras == null) {
            return;
        }

        NooieLog.d("-->> HomeActivity showPushActiveMessage msg=" + pushActiveExtras.getMsg() + " url=" + pushActiveExtras.getUrl() + " title=" + pushActiveExtras.getTitle());
        checkShowAd();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeChangeEvent(HomeChangeEvent event) {//刷新数据
        onRefresh();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeFamilyChangeEvent(HomeFamilyChangeEvent event) {
        mHomePresenter.changeCurrentHome(event.getHomeId());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeEvent(HomeEvent event) {
        if (event.getEventType().equals(ConstantValue.HOME_ONREFRESH)) {//刷新数据
            onRefresh();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupManageEvent(GroupManageEvent event) {
        if (event.isChange()) {
            onRefresh();
        }
    }

    private void stopRefresh() {
        DeviceManager.getInstance().notifyFragmentStropRefresh();
    }

    public void onRefresh() {
        mHomePresenter.createDefaultHome(0);
    }

    @Override
    public void notifyLoadUserInfoState(String result) {

    }

    @Override
    public void loadHomeDetailSuccess(HomeBean homeBean) {
        stopRefresh();
        updateUI(homeBean);
    }

    @Override
    public void loadHomeDetailFailed(String error) {
        stopRefresh();
        ErrorHandleUtil.toastTuyaError(this, error);
    }

    @Override
    public void notifyControlGroupState(String result) {

    }

    @Override
    public void notifyGroupDpUpdate(long groupId, String dps) {

    }

    @Override
    public void notifyControlDeviceState(String result, String msg) {

    }

    @Override
    public void notifyControlDeviceSuccess(String result) {

    }

    @Override
    public void notifyDeviceDpUpdate(String deviceId, String dps) {

    }

    @Override
    public void notifyLoadHomesSuccess(List<HomeBean> homes) {
        DeviceManager.getInstance().notifyLoadHomesSuccess(homes);
    }

    @Override
    public void notifyLoadHomesFailed(String msg) {
        stopRefresh();
        ErrorHandleUtil.toastTuyaError(this, msg);
    }

    @Override
    public void notifyChangeHomeState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            updateUI(FamilyManager.getInstance().getCurrentHome());
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }

    @Override
    public void notifyLoadDeviceSuccess(String devId, DeviceBean deviceBean) {

    }

    @Override
    public void notifyLoadGroupSuccess(long groupId, GroupBean groupBean) {

    }

    @Override
    public void notifyLoadHomeListSuccess(String code, List<HomeBean> list) {

    }

    @Override
    public void onCheckNetworkStatus(String result, boolean isNetworkUsable) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equals(result)) {
            sendNetworkChangeEvent(isNetworkUsable);
        }
    }

    @Override
    public void onGetUnreadMsgSuccess(int state, MsgUnreadInfo info) {
        if (isDestroyed() || info == null || state == SDKConstant.ERROR) {
            return;
        }
        //int unreadCount = info.getSystemUnreadCount();
        int unreadCount = 0;
        for (int i = 0; i < info.getDevMsgUnreadInfos().size(); i++) {
            unreadCount += info.getDevMsgUnreadInfos().get(i).getUnreadCount();
        }
        EventBusActivityScope.getDefault(this).post(new MsgCountUpdateEvent(unreadCount));
    }

    @Override
    public void onGetWeatherSuccess(WeatherBean weatherBean) {
        NooieLog.e("homeactivity weatherbean " + weatherBean.toString());
        EventBusActivityScope.getDefault(this).post(new WeatherEvent(weatherBean.getCondition(), weatherBean.getTemp(), weatherBean.getIconUrl()));
        ToastUtil.showToast(this, R.string.refresh_success);
    }

    @Override
    public void onGetWeatherFail(String errorCode, String errorMsg) {
        NooieLog.e("homeactivity weatherbean onGetWeatherFail  errorCode " + errorCode + " errorMsg " + errorMsg);
        ToastUtil.showToast(this, R.string.refresh_fail);
    }

    @Override
    public void onLoadBannerSuccess(List<BannerResult.BannerInfo> urlList) {

    }

    @Override
    public void onLoadBannerFail(String msg) {

    }

    @Override
    public void doOnNetworkChanged() {
        if (isDestroyed()) {
            return;
        }
        //NooieLog.d("-->> HomeActivity onNetworkChanged isConnect=" + NetworkUtil.isConnected(NooieApplication.mCtx));
        if (mPresenter != null) {
            mPresenter.checkNetworkStatus();
        }
        ApHelper.getInstance().notifyNetworkChange();
    }

    @Override
    public void doOnNetworkDetected(Bundle data) {
        if (isDestroyed() || data == null) {
            return;
        }
        TrackerRouterInfo trackerRouterInfo = new TrackerRouterInfo();
        trackerRouterInfo.parse(data);
        boolean isPingSuccess = checkIsPingSuccess(trackerRouterInfo);
        NooieLog.d("-->> HomeActivity doOnNetworkDetected isPingSuccess=" + isPingSuccess);
        sendNetworkChangeEvent(isPingSuccess);
        //NooieLog.d("-->> HomeActivity doOnNetworkDetected trackRouterInfo address=" + trackerRouterInfo.getAddress() + " result=" + trackerRouterInfo.getResult());
    }

    private boolean checkIsPingSuccess(TrackerRouterInfo trackerRouterInfo) {
        boolean result = trackerRouterInfo == null || trackerRouterInfo.getResult() == 0;
        return result;
    }

    @Override
    public void doOnNetworkOperated(Bundle data) {
        NooieLog.d("-->> HomeActivity doOnNetworkOperated");
        if (data != null) {
            int connectionMode = data.getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE);
            if (connectionMode == ConstantValue.CONNECTION_MODE_QC) {
                NetworkUtil.disableCurrentWiFiByPrefix(NooieApplication.mCtx, ConstantValue.AP_FUTURE_CODE_PREFIX_GNCC, new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean result) {
                        NooieLog.d("-->> HomeActivity onNext disableCurrentWiFiByPrefix result=" + result);
                    }
                });
            }
        }
    }

    private void sendNetworkChangeEvent(boolean isConnected) {
        EventBusActivityScope.getDefault(this).post(new NetworkChangeEvent(isConnected ? NetworkChangeEvent.NETWORK_CHANGE_CONNECTED : NetworkChangeEvent.NETWORK_CHANGE_DISCONNECTED));
    }

    @Override
    public void showCheckLocationPermDialog() {
        switch (mCurrentPosition) {
            case HomeFragment.FIRST: {
                //添加第一个页面请求位置权限的对话框
                showCheckLocalPermDialog(getString(R.string.location_service_open), new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION) && EasyPermissions.somePermissionDenied(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                        requestPermission(ConstantValue.PERM_GROUP_LOCATION, ConstantValue.REQUEST_CODE_FOR_LOCATION_PERM);
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
                break;
            }
            case HomeFragment.SECOND: {
                showCheckLocalPermDialog(getString(R.string.connection_to_wifi_no_perm_for_ssid), new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION) && EasyPermissions.somePermissionDenied(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                        requestPermission(ConstantValue.PERM_GROUP_LOCATION, ConstantValue.REQUEST_CODE_FOR_LOCATION_PERM);
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
                break;
            }
        }
    }

    private void updateApDeviceHardVersion() {
        if (MyAccountHelper.getInstance().isLogin() && mPresenter != null) {
            mPresenter.getAllApDeviceHardVersion();
        }
    }

    private void stopUpdateApDeviceHardVersion() {
        if (mPresenter != null) {
            mPresenter.stopGetAllApDeviceHardVersion();
        }
    }

    private void refreshStatusBar(int currentPosition) {
       /* if (currentPosition == mCurrentPosition || ((currentPosition == HomeFragment.SECOND || currentPosition == HomeFragment.FOUR) && (mCurrentPosition == HomeFragment.SECOND || mCurrentPosition == HomeFragment.FOUR))) {
            return;
        }*/
        if (currentPosition == mCurrentPosition || ((currentPosition == HomeFragment.FIRST || currentPosition == HomeFragment.THIRD) && (mCurrentPosition == HomeFragment.FIRST || mCurrentPosition == HomeFragment.THIRD))) {
            return;
        }
        mCurrentPosition = currentPosition;
        setupStatusBar();
    }

    private int getHomePageAction() {
        return getStartParam() != null ? getStartParam().getInt(ConstantValue.INTENT_KEY_HOME_PAGE_ACTION, ConstantValue.HOME_PAGE_ACTION_NORMAL) : ConstantValue.HOME_PAGE_ACTION_NORMAL;
    }

    private String getDeviceModel() {
        return getStartParam() != null ? getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL) : "";
    }

    private void initFirebaseAnalyticsInfo() {
        if (MyAccountHelper.getInstance().isLogin()) {
            FirebaseAnalyticsManager.getInstance().setUserId(mUid);
            FirebaseAnalyticsManager.getInstance().setSessionTimeoutDuration(FirebaseConstant.DEFAULT_SESSION_TIME_OUT_DURATION);
        }
    }

    @Override
    public void onHomeAdded(long homeId) {
        NooieLog.e("------> ITuyaHomeChangeListener onHomeAdded homeId " + homeId);
        if (mHomePresenter != null) {
            mHomePresenter.loadHomeDetail(homeId);
            mHomePresenter.loadHomes();
        }
    }

    @Override
    public void onHomeInvite(long homeId, String homeName) {

    }

    @Override
    public void onHomeRemoved(long homeId) {
        NooieLog.e("------> ITuyaHomeChangeListener onHomeAdded homeId " + homeId);
        mHomePresenter.createDefaultHome(0);
    }

    @Override
    public void onHomeInfoChanged(long homeId) {
        NooieLog.e("------> ITuyaHomeChangeListener onHomeInfoChanged homeId " + homeId);
        //修改家庭名字
        mHomePresenter.loadHomes();
    }

    @Override
    public void onSharedDeviceList(List<DeviceBean> sharedDeviceList) {
        NooieLog.e("------> ITuyaHomeChangeListener onSharedDeviceList sharedDevices " + sharedDeviceList.size());
        mHomePresenter.loadHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
    }

    @Override
    public void onSharedGroupList(List<GroupBean> sharedGroupList) {
        NooieLog.e("------> ITuyaHomeChangeListener onSharedGroupList sharedGroups " + sharedGroupList.size());
        mHomePresenter.loadHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
    }

    @Override
    public void onServerConnectSuccess() {
        NooieLog.e("------> ITuyaHomeChangeListener onServerConnectSuccess FamilyManager.getInstance().getCurrentHomeId() " + FamilyManager.getInstance().getCurrentHomeId());
        if (FamilyManager.getInstance().getCurrentHomeId() != FamilyManager.DEFAULT_HOME_ID) {
            FamilyManager.getInstance().getHomeList(new ITuyaGetHomeListCallback() {
                @Override
                public void onSuccess(List<HomeBean> list) {
                    for (HomeBean homeBean : list) {
                        if (homeBean.getHomeId() == FamilyManager.getInstance().getCurrentHomeId()) {
                            mHomePresenter.loadHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
                            break;
                        }
                    }
                }

                @Override
                public void onError(String errorCode, String errorMsg) {
                    NooieLog.e("-------------> getHomeList errorCode " + errorCode + " errorMsg " + errorMsg);
                }
            });
        }
    }

    @Override
    public void onDeviceAdded(String devId) {
        NooieLog.e("------->>> ITuyaHomeStatusListener onDeviceRemoved devId " + devId);

        mHomePresenter.loadHomeDetail(FamilyManager.getInstance().getCurrentHomeId());

    }

    @Override
    public void onDeviceRemoved(String devId) {
        NooieLog.e("------->>> ITuyaHomeStatusListener onDeviceRemoved devId " + devId);
        mHomePresenter.loadHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
    }

    @Override
    public void onGroupAdded(long groupId) {
        NooieLog.e("------->>> ITuyaHomeStatusListener onGroupAdded groupId " + groupId);
        mHomePresenter.loadHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
    }

    @Override
    public void onGroupRemoved(long groupId) {

    }

    @Override
    public void onMeshAdded(String meshId) {

    }

    private void checkMsgUnread() {
        if (ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            return;
        }
        List<String> idList = new ArrayList<>();
        List<DeviceInfo> devices = NooieDeviceHelper.getAllDeviceInfo();
        for (DeviceInfo deviceInfo : CollectionUtil.safeFor(devices)) {
            idList.add(deviceInfo.getNooieDevice().getUuid());
        }
        if (mPresenter != null) {
            mPresenter.loadMsgUnread(idList, isFirstLaunch());
        }
    }
}
