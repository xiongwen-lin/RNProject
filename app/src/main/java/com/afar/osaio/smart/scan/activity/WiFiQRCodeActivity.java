package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.data.StringHelper;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.afar.osaio.util.QRCodeAsyncTask;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.Util;
import com.afar.osaio.widget.FButton;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.log.NooieLog;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WiFiQRCodeActivity extends BaseActivity {

    private static final int RELOAD_QR_CODE_COUNT = 2;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivQRCode)
    ImageView ivQRCode;
    @BindView(R.id.containerQRCode)
    FrameLayout containerQRCode;
    @BindView(R.id.ivCnr1)
    ImageView ivCnr1;
    @BindView(R.id.ivCnr2)
    ImageView ivCnr2;
    @BindView(R.id.ivCnr3)
    ImageView ivCnr3;
    @BindView(R.id.ivCnr4)
    ImageView ivCnr4;
    @BindView(R.id.ivBigQRCode)
    ImageView ivBigQRCode;
    @BindView(R.id.btnDone)
    FButton btnDone;
    @BindView(R.id.tvNothing)
    TextView tvNothing;
    @BindView(R.id.container)
    LinearLayout container;
    @BindView(R.id.tv1)
    TextView tvWifiQrTopTip;
    @BindView(R.id.tv2)
    TextView tvWifiQrSubTip;

    private int brightness;
    private String mSsid;
    private String mPsd;
    private IpcType mDeviceType;
    private int mQRCodeWidth;
    private int mReloadQRCodeCount = 0;

    /**
     * Called when the activity is first created.
     */
    Bitmap bp = null;
    float scaleWidth;
    float scaleHeight;

    public static void toWiFiQRCodeActivity(Context from, String ssid, String psd, String model, int connectionMode) {
        Intent intent = new Intent(from, WiFiQRCodeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, psd);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_qr_code);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        brightness = DisplayUtil.getScreenBrightness(NooieApplication.mCtx);
        DisplayUtil.setScreenBrightness(this, 255);
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            return;
        }

        mDeviceType = IpcType.getIpcType(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL));
        mSsid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
        mPsd = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
    }

    private void initView() {
        mQRCodeWidth = DisplayUtil.pxToDp(NooieApplication.mCtx, (int) (DisplayUtil.SCREEN_WIDTH_PX * 0.85));
        int screen_h = DisplayUtil.SCREEN_HIGHT_PX - DisplayUtil.dpToPx(NooieApplication.mCtx,
                DisplayUtil.HEADER_BAR_HEIGHT_DP) - DisplayUtil.getStatusBarHeight(NooieApplication.mCtx);
        screen_h = Math.max(DisplayUtil.SCREEN_CONTENT_MIN_HEIGHT_PX, screen_h);
        container.setMinimumHeight(screen_h);

        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_scan_wifi_code);
        ivRight.setVisibility(View.GONE);

        /*
        if (mDeviceType == IpcType.IPC_720) {
            tvWifiQrTopTip.setText(getString(R.string.add_camera_scan_qr_tip));
            tvWifiQrSubTip.setText(getString(R.string.add_camera_scan_qr_sub_tip));
        } else {
            tvWifiQrTopTip.setText(getString(R.string.add_camera_scan_qr_tip_1080));
            tvWifiQrSubTip.setText(getString(R.string.add_camera_scan_qr_sub_tip_1080));
        }
        */
        tvWifiQrTopTip.setText(getString(R.string.add_camera_scan_qr_tip_1080));
        //tvWifiQrSubTip.setText(getString(R.string.add_camera_scan_qr_sub_tip_1080));
        tvWifiQrSubTip.setText(getString(R.string.add_camera_scan_qr_sub_tip_1));
        tvNothing.setText(R.string.add_camera_nothing_happen);
        //tvNothing.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        /*
        if (mDeviceType == IpcType.EC810PRO) {
            tvWifiQrSubTip.setText(R.string.wifi_qr_code_tip_ec_810_pro);
            tvNothing.setText(R.string.wifi_qr_code_scan_no_response_ec_810_pro);
        }
         */

        ivCnr1.setVisibility(View.GONE);
        ivCnr2.setVisibility(View.GONE);
        ivCnr3.setVisibility(View.GONE);
        ivCnr4.setVisibility(View.GONE);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) containerQRCode.getLayoutParams();
        params.width = DisplayUtil.dpToPx(this, mQRCodeWidth);
        params.height = DisplayUtil.dpToPx(this, mQRCodeWidth);
        containerQRCode.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        containerQRCode.setLayoutParams(params);

        //setupClickableTv();
        createQRCode();
    }

    private void setupClickableTv() {
        final SpannableStringBuilder style = new SpannableStringBuilder();
        String text = getString(R.string.add_camera_nothing_happen);

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                NothingHappenActivity.toNothingHappenActivity(WiFiQRCodeActivity.this, mSsid, mPsd, mDeviceType.getType(), getConnectionMode());
            }
        };
        style.setSpan(clickableSpan, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvNothing.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.gray_cc616161));
        style.setSpan(foregroundColorSpan, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvNothing.setMovementMethod(LinkMovementMethod.getInstance());
        tvNothing.setText(style);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DisplayUtil.setScreenBrightness(this, brightness);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        containerQRCode = null;
        ivCnr1 = null;
        ivCnr2 = null;
        ivCnr3 = null;
        ivCnr4 = null;
        ivBigQRCode = null;
        btnDone = null;
        tvNothing = null;
        container = null;
        tvWifiQrTopTip = null;
        tvWifiQrSubTip = null;
    }

    private void createQRCode() {
        String zone = CountryUtil.getCurrentTimezone() + ".00";
        String area = TextUtils.isEmpty(GlobalData.getInstance().getRegion()) ? ApHelper.getInstance().getCurrentRegion().toUpperCase() : GlobalData.getInstance().getRegion().toUpperCase();
        String wifiInfo = String.format("WIFI:U:%s;Z:%s;R:%s;T:WPA;P:\"%s\";S:%s;", mUid, zone, area, mPsd, mSsid);
        String lenInfo = String.format(Locale.ENGLISH, "L:%d;%d;%d;", StringHelper.getStringByteSize(mPsd, StringHelper.CharSet_UTF_8), StringHelper.getStringByteSize(mSsid, StringHelper.CharSet_UTF_8), StringHelper.getStringByteSize(wifiInfo, StringHelper.CharSet_UTF_8));
        String info = lenInfo + wifiInfo;
        NooieLog.d("-->> WiFiQRCodeActivity createQRCode info=" + info);

        if (TextUtils.isEmpty(mUid)) {
            ToastUtil.showToast(WiFiQRCodeActivity.this, R.string.add_camera_wifi_qr_param_error);
            Util.delayTask(2000, new Util.OnDelayTaskFinishListener() {
                @Override
                public void onFinish() {
                    if (isDestroyed()) {
                        return;
                    }
                    finish();
                }
            });
            return;
        }

        loadQRCode(info);
    }

    private void loadQRCode(final String info) {
        showLoading(false);
        QRCodeAsyncTask task = new QRCodeAsyncTask(new QRCodeAsyncTask.OnLoadFinishListener() {
            @Override
            public void onLoadBitmap(Bitmap bitmap) {
                if (isDestroyed() || checkNull(ivQRCode, ivBigQRCode)) {
                    return;
                }
                if (bitmap == null) {
                    if (mReloadQRCodeCount < RELOAD_QR_CODE_COUNT) {
                        mReloadQRCodeCount++;
                        loadQRCode(info);
                    } else {
                        hideLoading();
                    }
                } else {
                    bp = bitmap;
                    DisplayMetrics dm = new DisplayMetrics();//创建矩阵
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    int width = bp.getWidth();
                    int height = bp.getHeight();
                    int w = dm.widthPixels; //得到屏幕的宽度
                    int h = dm.heightPixels; //得到屏幕的高度
                    scaleWidth = ((float) w) / width;
                    scaleHeight = ((float) h) / height;

                    ivQRCode.setImageBitmap(bitmap);
                    ivBigQRCode.setImageBitmap(bitmap);
                    hideLoading();
                }
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, info);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:  //当屏幕检测到第一个触点按下之后就会触发到这个事件。
                break;
        }
        return super.onTouchEvent(event);
    }

    @OnClick({R.id.ivLeft, R.id.btnDone, R.id.containerQRCode, R.id.ivBigQRCode, R.id.tvNothing})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.btnDone:
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_CONNECT_CAMERA_NEXT);
                NooieScanActivity.toNooieScanActivity(WiFiQRCodeActivity.this, mSsid, mPsd, mDeviceType.getType(), getConnectionMode(), null);
                break;
            case R.id.ivLeft:
                finish();
                break;
            case R.id.containerQRCode:
            case R.id.ivBigQRCode:
                if (ivBigQRCode.isShown()) {
                    DialogUtils.hideWithAlphaAnim(ivBigQRCode);
                } else {
                    DialogUtils.showWithAlphaAnim(ivBigQRCode, null);
                }
                break;
            case R.id.tvNothing:
                //EventTrackingApi.getInstance().trackNormalEvent("", NooieDeviceHelper.createDistributionNetworkExternal(false));
                NothingHappenActivity.toNothingHappenActivity(WiFiQRCodeActivity.this, mSsid, mPsd, mDeviceType.getType(), getConnectionMode());
                break;
        }
    }

    private int getConnectionMode() {
        int connectionMode = getCurrentIntent() != null ? getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC) : ConstantValue.CONNECTION_MODE_QC;
        return connectionMode;
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }
}
