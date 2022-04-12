package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.QRCodeAsyncTask;
import com.nooie.common.utils.log.NooieLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterSetWifiManagerActivity extends RouterBaseActivity {

    public static void toRouterSetWifiManagerActivity(Context from, int ssidSwitch, int ssidSwitch2, int ssidSwitch5, String ssid, String password, String ssid5, String password5) {
        Intent intent = new Intent(from, RouterSetWifiManagerActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID_SWITCH, ssidSwitch);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID_SWITCH_2G, ssidSwitch2);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID_SWITCH_5G, ssidSwitch5);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, password);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID_5, ssid5);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD_5, password5);
        from.startActivity(intent);
    }

    public static void toRouterSetWifiManagerActivity(Context from, String activity, int ssidSwitch, int ssidSwitch2, int ssidSwitch5, String ssid, String password, String ssid5, String password5){
        Intent intent = new Intent(from, RouterSetWifiManagerActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING, activity);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID_SWITCH, ssidSwitch);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID_SWITCH_2G, ssidSwitch2);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID_SWITCH_5G, ssidSwitch5);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, password);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID_5, ssid5);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD_5, password5);
        from.startActivity(intent);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivQRCode)
    ImageView ivQRCode;
    @BindView(R.id.tvQR)
    TextView tvQR;
    @BindView(R.id.ivQRCode5G)
    ImageView ivQRCode5G;
    @BindView(R.id.tvQR5G)
    TextView tvQR5G;

    private String activity = "";
    private static final int RELOAD_QR_CODE_COUNT = 2;
    private int mReloadQRCodeCount = 0;
    private String ssid = "";
    private String password = "";
    private String ssid5 = "";
    private String password5 = "";
    private int ssidSwitch = 0;
    private int ssidSwitch2 = 0;
    private int ssidSwitch5 = 0;

    /**
     * Called when the activity is first created.
     */
    /*Bitmap bp = null;
    float scaleWidth;
    float scaleHeight;*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_set_wifi_manager);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        activity = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_wifi_management);
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        ssid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
        password = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
        ssid5 = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID_5);
        password5 = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD_5);
        ssidSwitch = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_SSID_SWITCH, 0);
        ssidSwitch2 = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_SSID_SWITCH_2G, 0);
        ssidSwitch5 = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_SSID_SWITCH_5G, 0);

        ivRight.setImageResource(R.drawable.help_icon);
        if (activity != null && !"".equals(activity)) {
            tvQR.setText(R.string.router_wifi_management_tip);
        } else {
            //ivRight.setImageResource(R.drawable.settings_black);
            tvQR.setText(ssid);
        }
        tvQR.setTextColor(getResources().getColor(R.color.black_80010c11));
        tvQR5G.setTextColor(getResources().getColor(R.color.black_80010c11));
        tvQR5G.setText(ssid5);

        createQRCode();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void createQRCode() {
        String wifiInfo = String.format("P:\"%s\";S:%s;", password, ssid);
        String wifiInfo5 = String.format("P:\"%s\";S:%s;", password5, ssid5);
        NooieLog.d("-->> RouterSetWifiManagerActivity createQRCode info=" + wifiInfo, wifiInfo5);
        if (ssidSwitch == 1) {
            ivQRCode.setVisibility(View.VISIBLE);
            tvQR.setVisibility(View.VISIBLE);
        } else {
            if (ssidSwitch2 == 1) {
                ivQRCode.setVisibility(View.VISIBLE);
                tvQR.setVisibility(View.VISIBLE);
            }

            if (ssidSwitch5 == 1) {
                ivQRCode5G.setVisibility(View.VISIBLE);
                tvQR5G.setVisibility(View.VISIBLE);
            }
        }

        loadQRCode(wifiInfo, true);
        mReloadQRCodeCount = 0;
        loadQRCode(wifiInfo5, false);
    }

    private void loadQRCode(final String info, boolean ssid2) {
        showLoading(false);
        QRCodeAsyncTask task = new QRCodeAsyncTask(new QRCodeAsyncTask.OnLoadFinishListener() {
            @Override
            public void onLoadBitmap(Bitmap bitmap) {
                if (isDestroyed() || checkNull(ivQRCode, ivQRCode5G)) {
                    return;
                }
                if (bitmap == null) {
                    if (mReloadQRCodeCount < RELOAD_QR_CODE_COUNT) {
                        mReloadQRCodeCount++;
                        loadQRCode(info, ssid2);
                    } else {
                        hideLoading();
                    }
                } else {
                    /*bp = bitmap;
                    DisplayMetrics dm = new DisplayMetrics();//创建矩阵
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    int width = bp.getWidth();
                    int height = bp.getHeight();
                    int w = dm.widthPixels; //得到屏幕的宽度
                    int h = dm.heightPixels; //得到屏幕的高度
                    scaleWidth = ((float) w) / width;
                    scaleHeight = ((float) h) / height;*/

                    if (ssid2) {
                        ivQRCode.setImageBitmap(bitmap);
                    } else {
                        ivQRCode5G.setImageBitmap(bitmap);
                    }
                    hideLoading();
                }
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, info);
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                RouterSetWifiManagerHelpActivity.toRouterSetWifiManagerHelpActivity(this);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
