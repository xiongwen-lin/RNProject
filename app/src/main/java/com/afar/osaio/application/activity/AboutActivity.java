package com.afar.osaio.application.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.BuildConfig;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.Util;
import com.nooie.common.utils.configure.CountryUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.imageView3)
    ImageView imageView3;
    @BindView(R.id.textView3)
    TextView textView3;
    @BindView(R.id.tvVersion)
    TextView tvVersion;

    public static void toAboutActivity(Context from) {
        Intent intent = new Intent(from, AboutActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.app_settings_about);
        ivRight.setVisibility(View.GONE);
        if (BuildConfig.DEBUG) {
            StringBuilder versionSb = new StringBuilder();
            versionSb.append(BuildConfig.VERSION_NAME);
            versionSb.append("(");
            versionSb.append(BuildConfig.VERSION_CODE);
            versionSb.append(")");
            tvVersion.setText(String.format(getString(R.string.about_version), versionSb.toString()));
        } else {
            tvVersion.setText(String.format(getString(R.string.about_version), Util.getLocalVersionName(NooieApplication.mCtx)));
        }
    }

    @OnClick({R.id.ivLeft, R.id.containerWebsite, R.id.containerEmail, R.id.containerConditionsOfUse, R.id.containerPrivacy})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.containerWebsite:
                Intent webIntent = new Intent();
                webIntent.setAction(Intent.ACTION_VIEW);
                Uri content_url = Uri.parse("https://osaio.net");
                webIntent.setData(content_url);
                startActivity(webIntent);
                break;
            case R.id.containerEmail:
                StringBuilder mailToSb = new StringBuilder();
                mailToSb.append("mailto:");
                mailToSb.append(getString(R.string.support_email_address));
                Uri uri = Uri.parse(mailToSb.toString());
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(Intent.createChooser(emailIntent, getString(R.string.about_select_email_application)));
                break;
            case R.id.containerConditionsOfUse:
                WebViewActivity.toWebViewActivity(AboutActivity.this, WebViewActivity.getUrl(CommonUtil.getTerms(NooieApplication.mCtx)), getString(R.string.terms_of_service));
                break;
            case R.id.containerPrivacy:
                WebViewActivity.toWebViewActivity(AboutActivity.this, WebViewActivity.getUrl(CommonUtil.getPrivacyPolicyByCountry(NooieApplication.mCtx, CountryUtil.getCurrentCountry(NooieApplication.mCtx))), getString(R.string.privacy_policy));
                break;
        }
    }
}
