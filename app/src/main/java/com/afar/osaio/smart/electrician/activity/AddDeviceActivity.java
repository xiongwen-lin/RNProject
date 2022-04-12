package com.afar.osaio.smart.electrician.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.application.activity.ThirdSkillActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.bluetooth.activity.BaseBluetoothActivity;
import com.afar.osaio.smart.electrician.bean.ConnectModePopMenuItem;
import com.afar.osaio.smart.electrician.widget.ConnectModePopupWindows;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.scan.activity.AddCameraSelectActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.RelativePopupMenu;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * AddDeviceActivity
 *
 * @author Administrator
 * @date 2019/3/5
 */
public class AddDeviceActivity extends BaseBluetoothActivity {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvRight)
    TextView tvRight;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivState)
    ImageView ivState;
    @BindView(R.id.tvLightNoRight)
    TextView tvLightNoRight;
    @BindView(R.id.tvAddDeviceGuideInfo)
    TextView tvAddDeviceGuideInfo;
    @BindView(R.id.btnDone)
    FButton fButton;
    @BindView(R.id.gifIvAirPurifier)
    GifImageView gifIvAirPurifier;
    ConnectModePopupWindows mConnectModeMenu;

    private static final int RED_LIGHT_ON = 1;
    private static final int RED_LIGHT_OFF = 2;
    private boolean isFromLogin;
    private String mAddType;
    private String name;
    private String underLineText;
    private int connectMode = ConstantValue.EC_MODE;
    private boolean mIsNormalDenied = false;

    private Handler mHandler = new Handler(Looper.myLooper());

    private Runnable mTimer = new Runnable() {
        @Override
        public void run() {
            if ((Integer) ivState.getTag() == RED_LIGHT_ON) {
                ivState.setTag(RED_LIGHT_OFF);
            } else {
                ivState.setTag(RED_LIGHT_ON);
            }
            mHandler.postDelayed(mTimer, 500);
        }
    };


    public static void toAddDeviceActivity(Context from, String addType, boolean isFromLogin, String name) {
        Intent intent = new Intent(from, AddDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_FROM_LOGIN, isFromLogin);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, name);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.bind(this);
        setupView();
        initBle();

    }

    private void setupView() {
        ivRight.setVisibility(View.INVISIBLE);
        tvRight.setText(R.string.ez_mode);
        Drawable drawable = getResources().getDrawable(R.drawable.home_switch);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tvRight.setCompoundDrawables(null, null, drawable, null);

        mAddType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
        isFromLogin = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_FROM_LOGIN, false);
        name = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        setupConnectModeListMenu();


        if (mAddType.equals(ConstantValue.ADD_DEVICE)) {
            tvAddDeviceGuideInfo.setText(R.string.indicator_tips);
            ivState.setImageResource(R.drawable.add_plug);
        } else if (mAddType.equals(ConstantValue.ADD_POWERSTRIP)) {
            tvAddDeviceGuideInfo.setText(R.string.indicator_tips);
            ivState.setImageResource(R.drawable.add_strip);
        } else if (mAddType.equals(ConstantValue.ADD_LAMP)) {
            tvAddDeviceGuideInfo.setText(R.string.blub_tips);
            ivState.setImageResource(R.drawable.add_light);
        } else if (mAddType.equals(ConstantValue.ADD_SWITCH)) {
            tvAddDeviceGuideInfo.setText(R.string.indicator_tips);
            ivState.setImageResource(R.drawable.add_switch);
        } else if (mAddType.equals(ConstantValue.ADD_LIGHT_STRIP)) {
            tvAddDeviceGuideInfo.setText(R.string.blub_tips);
            ivState.setImageResource(R.drawable.add_light_strip);
        } else if (mAddType.equals(ConstantValue.ADD_LIGHT_MODULATOR)) {
            tvAddDeviceGuideInfo.setText(R.string.blub_tips);
            ivState.setImageResource(R.drawable.add_modulator);
        } else if (mAddType.equals(ConstantValue.ADD_PET_FEEDER)) {
            tvAddDeviceGuideInfo.setText(R.string.indicator_tips);
            ivState.setImageResource(R.drawable.ic_device_feeder_back);
        } else if (mAddType.equals(ConstantValue.ADD_AIR_PURIFIER)) {
            //tvRight.setClickable(false);
            tvAddDeviceGuideInfo.setText(R.string.add_air_purifier_tips);
            ivState.setVisibility(View.GONE);
            gifIvAirPurifier.setVisibility(View.VISIBLE);
            try {
                // 如果加载的是gif动图，第一步需要先将gif动图资源转化为GifDrawable
                // 将gif图资源转化为GifDrawable
                GifDrawable gifDrawable = new GifDrawable(getResources(), R.raw.ic_add_purifier);
                // gif1加载一个动态图gif
                gifIvAirPurifier.setImageDrawable(gifDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        updateUI();
    }

    private void setupClickableTv() {
        final SpannableStringBuilder style = new SpannableStringBuilder();

        if (connectMode == ConstantValue.EC_MODE) {
            underLineText = getString(R.string.ez_not_flash);
            if (mAddType.equals(ConstantValue.ADD_AIR_PURIFIER)) {
                underLineText = getString(R.string.no_sound);
            }
        } else if (connectMode == ConstantValue.AP_MODE) {
            underLineText = getString(R.string.ap_not_flash);
        }

        //设置文字
        style.append(underLineText);

        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                //NoRedLightActivity.toNoRedLightActivity(AddACameraActivity.this, mDeviceType.getType());
            }
        };
        /*style.setSpan(clickableSpan, 0, underLineText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLightNoRight.setText(style);*/

        //设置部分文字颜色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_gray));
        style.setSpan(foregroundColorSpan, 0, underLineText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //设置部分文字粗细
        StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        style.setSpan(styleSpan, 0, underLineText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvLightNoRight.setMovementMethod(LinkMovementMethod.getInstance());
        tvLightNoRight.setText(style);
    }

    private void updateUI() {
        setupClickableTv();
        if (connectMode == ConstantValue.EC_MODE) {
            fButton.setText(R.string.ez_flash);
            if (mAddType.equals(ConstantValue.ADD_AIR_PURIFIER)) {
                fButton.setText(R.string.hear_sound);
                underLineText = getString(R.string.no_sound);
            }
        } else if (connectMode == ConstantValue.AP_MODE) {
            fButton.setText(R.string.ap_flash);
        }
    }

    @OnClick({R.id.btnDone, R.id.tvLightNoRight, R.id.ivLeft, R.id.tvRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnDone:
                if (connectMode == ConstantValue.EC_MODE) {
                    InputWiFiPsdActivity.toInputWiFiPsdActivity(this, ConstantValue.EC_MODE, mAddType);
                } else if (connectMode == ConstantValue.AP_MODE) {
                    InputWiFiPsdActivity.toInputWiFiPsdActivity(this, ConstantValue.AP_MODE, mAddType);
                } else if (connectMode == ConstantValue.BLUE_MODE) {
                    checkBeforeScanningBluetooth();
                }
                break;
            case R.id.tvLightNoRight:
                if (connectMode == ConstantValue.EC_MODE || connectMode == ConstantValue.BLUE_MODE) {
                    ResetDeviceActivity.toResetDeviceActivity(this, mAddType);
                } else if (connectMode == ConstantValue.AP_MODE) {
                    HotSpotTipsActivity.toHotSpotTipsActivity(this, mAddType);
                }
                break;
            case R.id.ivLeft:
                onBackPressed();
                break;
            case R.id.tvRight:
                mConnectModeMenu.showAsDropDown(findViewById(R.id.tvRight), -DisplayUtil.dpToPx(NooieApplication.mCtx, 60), DisplayUtil.dpToPx(NooieApplication.mCtx, 5));
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(mTimer);
        ivState.setTag(RED_LIGHT_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer);
    }

    @Override
    public void onBackPressed() {
        if (isFromLogin) {
            HomeActivity.toHomeActivity(this, HomeActivity.TYPE_ADD_DEVICE);
            finish();
        }
        super.onBackPressed();
    }

    private void setupConnectModeListMenu() {
        List<ConnectModePopMenuItem> menuItems = new ArrayList<>();
        ConnectModePopMenuItem addDeviceItem = new ConnectModePopMenuItem();
        addDeviceItem.setId(ConnectModePopupWindows.MENU_FIRST);
        addDeviceItem.setTitle(getString(R.string.ez_mode));
        menuItems.add(addDeviceItem);
        if (!mAddType.equals(ConstantValue.ADD_AIR_PURIFIER)) { //TODO 空气净化器，不支持热点配网
            ConnectModePopMenuItem createGroupItem = new ConnectModePopMenuItem();
            createGroupItem.setId(ConnectModePopupWindows.MENU_SECOND);
            createGroupItem.setTitle(getString(R.string.ap_mode));
            menuItems.add(createGroupItem);
        }
        if (name != null && name.contains(getString(R.string.ble_wifi))) { //包含蓝牙
            ConnectModePopMenuItem BlueItem = new ConnectModePopMenuItem();
            BlueItem.setId(ConnectModePopupWindows.MENU_THIRD);
            BlueItem.setTitle(getString(R.string.blue_mode));
            menuItems.add(BlueItem);
        }

        mConnectModeMenu = new ConnectModePopupWindows(this);
        mConnectModeMenu.setHeight(RecyclerView.LayoutParams.WRAP_CONTENT)
                .setWidth(DisplayUtil.dpToPx(NooieApplication.mCtx, 175))
                .dimBackground(false)
                .needAnimationStyle(true)
                .setAnimationStyle(R.style.TRM_ANIM_STYLE)
                .addMenuList(menuItems)
                .setOnMenuItemClickListener(new ConnectModePopupWindows.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position) {
                        if (mConnectModeMenu != null) {
                            mConnectModeMenu.dismiss();
                        }
                        switch (position) {
                            case RelativePopupMenu.MENU_FIRST:
                                tvRight.setText(getResources().getString(R.string.ez_mode));
                                connectMode = ConstantValue.EC_MODE;
                                updateUI();
                                break;
                            case RelativePopupMenu.MENU_SECOND:
                                tvRight.setText(getResources().getString(R.string.ap_mode));
                                connectMode = ConstantValue.AP_MODE;
                                updateUI();
                                break;
                            case RelativePopupMenu.MENU_THIRD:
                                tvRight.setText(getResources().getString(R.string.blue_mode));
                                connectMode = ConstantValue.BLUE_MODE;
                                updateUI();
                                break;
                        }
                    }
                });
    }

    /**
     * 打开蓝牙开关、位置权限
     */
    private void checkBeforeScanningBluetooth() {
        NooieLog.d("checkBeforeScanningBluetooth()---start() ");
        if (!checkUseLocationEnable()) {
            NooieLog.d("checkBeforeScanningBluetooth()---!checkUseLocationEnable()");
            requestLocationPerm(getString(R.string.bluetooth_scan_location_request_title), getString(R.string.bluetooth_scan_location_request_content), getString(R.string.cancel_normal), getString(R.string.bluetooth_scan_location_request_allow), new DialogUtils.OnClickConfirmButtonListener() {
                @Override
                public void onClickLeft() {
                }

                @Override
                public void onClickRight() {
                }
            }, true);
        } else if (!BluetoothHelper.isBluetoothOn()) {
            NooieLog.d("checkBeforeScanningBluetooth()---!isBluetoothOn()");
            BluetoothHelper.startBluetooth(this, ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE);
        } else {
            SearchBlueActivity.toSearchBlueActivity(AddDeviceActivity.this, mAddType);
            // showTuYaBlePopupWindows();
        }
    }

    @Override
    public void showCheckLocationPermDialog() {
        showCheckLocalPermForBluetoothDialog(new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION) && EasyPermissions.somePermissionDenied(AddDeviceActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                requestPermission(ConstantValue.PERM_GROUP_LOCATION);
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
                checkBeforeScanningBluetooth();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
