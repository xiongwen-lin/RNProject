package com.afar.osaio.smart.electrician.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.application.activity.WebViewActivity;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.hybrid.webview.HybridWebViewActivity;
import com.afar.osaio.smart.scan.activity.AddCameraSelectActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.MediaPopupWindows;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.log.NooieLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanFailedActivity extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvScanFailedGuideInfo3)
    TextView tvScanFailedGuideInfo3;
    @BindView(R.id.tvScanFailedGuideInfo4)
    TextView tvScanFailedGuideInfo4;
    @BindView(R.id.tvScanFailedGuideInfo5)
    TextView tvScanFailedGuideInfo5;
    @BindView(R.id.btnScanConnectAp)
    FButton btnScanConnectAp;
    @BindView(R.id.btnContactUs)
    FButton btnContactUs;
    private MediaPopupWindows mPopMenus;

    private String mSSID;
    private String mPsd;
    private String mAddType;

    public static void toScanFailedActivity(Context from, String ssid, String psd, String addType) {
        Intent intent = new Intent(from, ScanFailedActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, psd);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_failed);
        ButterKnife.bind(this);
        initData();
        initView();
        setupClickableTv();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.paired_failed);
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            NooieLog.e("-----------ScanFailedActivity initData");
            finish();
        } else {
            mSSID = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
            mPsd = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
            mAddType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
            setupUI();
        }
    }

    private void setupUI() {
        tvScanFailedGuideInfo4.setVisibility(View.GONE);
        tvScanFailedGuideInfo5.setVisibility(View.GONE);
        btnScanConnectAp.setVisibility(View.GONE);
        if (!mAddType.equals(ConstantValue.ADD_DEVICE)) {
            btnContactUs.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnContactUs, R.id.tvScanReset, R.id.btnScanConnectAp})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
            case R.id.tvScanReset: {
                //  InputWiFiPsdActivity.toInputWiFiPsdActivity(this, ConstantValue.EC_MODE, mAddType);
                if (mAddType.equals(ConstantValue.ADD_DEFAULT)){ //TODO 失败返回wifi页，需求改为失败返回设备列表、设备详情页
                    AddCameraSelectActivity.toAddCameraSelectActivity(ScanFailedActivity.this );
                }else{
                    AddDeviceActivity.toAddDeviceActivity(ScanFailedActivity.this, mAddType, false, "");
                }
                finish();
                break;
            }
            case R.id.btnContactUs: {
                showPopMenu();
                break;
            }
            case R.id.btnScanConnectAp: {
                ConnectInApModeActivity.toConnectInApModeActivity(ScanFailedActivity.this, mSSID, mPsd, mAddType);
                break;
            }
        }
    }

    private void showPopMenu() {
        if (mPopMenus != null) {
            mPopMenus.dismiss();
        }

        mPopMenus = new MediaPopupWindows(this, new MediaPopupWindows.OnClickMediaListener() {
            @Override
            public void onFaceBookClick() {
                try {
                    getPackageManager().getPackageInfo("com.facebook.katana", 0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/3041529699447693")));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/groups/porik")));
                }
            }

            @Override
            public void onYoutubeClick() {
                try {
                    getPackageManager().getPackageInfo("com.google.android.youtube", 0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("youtube://www.youtube.com/channel/UCHvXyEdQGyPv5XD_n31pQiw")));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCHvXyEdQGyPv5XD_n31pQiw")));
                }
            }

            @Override
            public void onInstagramClick() {
                try {
                    getPackageManager().getPackageInfo("com.instagram.android", 0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("instagram://user?username=porik_official")));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/porik_official/")));
                }
            }

            @Override
            public void onEmailClick() {
                StringBuilder mailToSb = new StringBuilder();
                mailToSb.append("mailto:");
                mailToSb.append(getString(R.string.porik_email));
                Uri uri = Uri.parse(mailToSb.toString());
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(Intent.createChooser(intent, getString(R.string.about_select_email_application)));
            }
        });
        mPopMenus.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mPopMenus = null;
            }
        });

        mPopMenus.showAtLocation(this.findViewById(R.id.containerScanFailed),
                Gravity.TOP | Gravity.BOTTOM, 0, 0);
    }

    private void setupClickableTv() {
        final SpannableStringBuilder style = new SpannableStringBuilder();
        String faq = getString(R.string.faq);
        String text = String.format(getString(R.string.scan_failed_guide_info_3), faq);

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                WebViewActivity.toWebViewActivity(ScanFailedActivity.this, "file:///android_asset/html/FQA.html", getString(R.string.feedback_faq));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(conditionClickableSpan, text.indexOf(faq), text.indexOf(faq) + faq.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvScanFailedGuideInfo3.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_gray));
        style.setSpan(conditionForegroundColorSpan, text.indexOf(faq), text.indexOf(faq) + faq.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        //配置给TextView
        tvScanFailedGuideInfo3.setMovementMethod(LinkMovementMethod.getInstance());
        tvScanFailedGuideInfo3.setText(style);
    }
}
