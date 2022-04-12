package com.afar.osaio.account.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.account.contract.FirstStartInstructionContract;
import com.afar.osaio.account.presenter.FirstStartInstructionPresenter;
import com.afar.osaio.application.activity.WebViewActivity;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.log.NooieLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirstStartInstructionActivity extends BaseActivity implements FirstStartInstructionContract.View {

    @BindView(R.id.tvPrivacy)
    TextView tvPrivacy;

    private FirstStartInstructionContract.Presenter mPresenter;

    public static void toFirstStartInstructionActivity(Context from) {
        Intent intent = new Intent(from, FirstStartInstructionActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start_instruction);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
    public void releaseRes() {
        super.releaseRes();
    }

    @OnClick({R.id.btnPrivacyAgree, R.id.btnPrivacyReject})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnPrivacyAgree:
                GlobalPrefs.setAppIsStarted(true);
                //SignInActivity.toSignInActivity(this, "", "", true);
                StartUpGuideActivity.toStartUpGuideActivity(this);
                finish();
                finish();
                break;
            case R.id.btnPrivacyReject:
                finish();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull FirstStartInstructionContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public int getStatusBarMode() {
        return ConstantValue.STATUS_BAR_DARK_MODE;
    }

    private void initData() {
        new FirstStartInstructionPresenter(this);
    }

    private void initView() {
        setupPrivacyClickableTv();
    }

    private void setupPrivacyClickableTv() {
        SpannableStringBuilder style = new SpannableStringBuilder();
        String conditionUse = getString(R.string.terms_of_service);
        String privacy = getString(R.string.privacy_policy);
        String text = String.format(getString(R.string.first_start_instruction_tip), conditionUse, privacy);

        //设置文字
        style.append(text);

        try {
            int termsStart = text.indexOf(conditionUse);
            int termsEnd = termsStart + conditionUse.length();
            boolean termSetStyleValid = termsStart >= 0 && termsEnd > termsStart;
            if (termSetStyleValid) {
                //设置部分文字点击事件
                ClickableSpan conditionClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        WebViewActivity.toWebViewActivity(FirstStartInstructionActivity.this, WebViewActivity.getUrl(CommonUtil.getTerms(NooieApplication.mCtx)), getString(R.string.terms_of_service));
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(false);
                        ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
                    }
                };
                style.setSpan(conditionClickableSpan, termsStart, termsEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvPrivacy.setText(style);
                //设置部分文字颜色
                ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green));
                style.setSpan(conditionForegroundColorSpan, termsStart, termsEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            int privacyStart = text.indexOf(privacy);
            int privacyEnd =  privacyStart + privacy.length();
            boolean privacySetStyleValid = privacyStart >= 0 && termsEnd > termsStart;
            if (privacySetStyleValid) {
                //设置部分文字点击事件
                ClickableSpan privacyClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        WebViewActivity.toWebViewActivity(FirstStartInstructionActivity.this, WebViewActivity.getUrl(CommonUtil.getPrivacyPolicyByCountry(NooieApplication.mCtx, CountryUtil.getCurrentCountry(NooieApplication.mCtx))), getString(R.string.privacy_policy));
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(false);
                        ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
                    }
                };
                style.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvPrivacy.setText(style);
                //设置部分文字颜色
                ForegroundColorSpan privacyForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green));
                style.setSpan(privacyForegroundColorSpan, privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        //配置给TextView
        tvPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
        tvPrivacy.setText(style);
    }
}
