package com.afar.osaio.smart.setting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.application.activity.AboutActivity;
import com.afar.osaio.application.activity.WebViewActivity;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.message.presenter.AppSettingPresenterImpl;
import com.afar.osaio.message.presenter.IAppSettingPresenter;
import com.afar.osaio.message.view.IAppSettingView;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.DashBoardBean;
import com.tuya.smart.home.sdk.callback.IGetHomeWetherCallBack;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.enums.TempUnitEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public class AppSettingsActivity extends BaseActivity implements IAppSettingView {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvCacheSize)
    TextView tvCacheSize;
    @BindView(R.id.tvTempUnitC)
    TextView tvTempUnitC;
    @BindView(R.id.tvTempUnitF)
    TextView tvTempUnitF;

    private IAppSettingPresenter appSettingPresenter;

    public static void toAppSettingsActivity(Context from) {
        Intent intent = new Intent(from, AppSettingsActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        appSettingPresenter.getCacheSize();
    }

    private void initData() {
        appSettingPresenter = new AppSettingPresenterImpl(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setVisibility(View.GONE);
        tvTitle.setText(R.string.app_settings_title);
        setTvTempStyle(TuyaHomeSdk.getUserInstance().getUser().getTempUnit());
    }

    @OnClick({R.id.ivLeft, R.id.containerConditionsOfUse, R.id.containerPrivacy, R.id.containerClearCache, R.id.containerAbout,R.id.tvTempUnitC,R.id.tvTempUnitF})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.tvTempUnitC:
                if (TuyaHomeSdk.getUserInstance().getUser().getTempUnit()!= TempUnitEnum.Celsius.getType()){
                    setTemp(TempUnitEnum.Celsius);
                }
                break;
            case R.id.tvTempUnitF:
                if (TuyaHomeSdk.getUserInstance().getUser().getTempUnit() != TempUnitEnum.Fahrenheit.getType()){
                    setTemp(TempUnitEnum.Fahrenheit);
                }
                break;
            case R.id.containerConditionsOfUse:
                WebViewActivity.toWebViewActivity(this, WebViewActivity.getUrl(CommonUtil.getTerms(NooieApplication.mCtx)), getString(R.string.terms_of_service));
                break;
            case R.id.containerPrivacy:
                WebViewActivity.toWebViewActivity(this, WebViewActivity.getUrl(CommonUtil.getPrivacyPolicyByCountry(NooieApplication.mCtx, CountryUtil.getCurrentCountry(NooieApplication.mCtx))), getString(R.string.privacy_policy));
                break;
            case R.id.containerClearCache:
                DialogUtils.showConfirmWithSubMsgDialog(this, R.string.app_settings_clear_cache, R.string.app_settings_clear_cache_tip,
                        R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
                            @Override
                            public void onClickRight() {
                                String cacheSize = tvCacheSize != null && tvCacheSize.getText() != null ? tvCacheSize.getText().toString() : "";
                                ArrayMap<String, Object> external = new ArrayMap<>();
                                external.put(EventDictionary.EXTERNAL_KEY_CACHE_SIZE, cacheSize);
                                EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_CLICK_CLEAR_CACHE, GsonHelper.convertToJson(external));
                                // clear cached action
                                appSettingPresenter.clearCache();
                            }

                            @Override
                            public void onClickLeft() {
                            }
                        });
                break;
            case R.id.containerAbout:
                AboutActivity.toAboutActivity(this);
                break;
        }
    }

    private  void  setTvTempStyle(int  type){
        if (type == TempUnitEnum.Fahrenheit.getType()){
            tvTempUnitC.setTextColor(getResources().getColor(R.color.theme_subtext_color));
            tvTempUnitF.setTextColor(getResources().getColor(R.color.theme_green));
            tvTempUnitC.setTextSize(14);
            tvTempUnitF.setTextSize(16);
        }else {
            tvTempUnitC.setTextColor(getResources().getColor(R.color.theme_green));
            tvTempUnitF.setTextColor(getResources().getColor(R.color.theme_subtext_color));
            tvTempUnitC.setTextSize(16);
            tvTempUnitF.setTextSize(14);
        }
    }

    private void  setTemp(TempUnitEnum  type){
        setTvTempStyle(type.getType());
        TuyaHomeSdk.getUserInstance().setTempUnit(type, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                ToastUtil.showToast(AppSettingsActivity.this,error);
            }

            @Override
            public void onSuccess() {
                ToastUtil.showToast(AppSettingsActivity.this,"ok");
            }
        });

    }
    @Override
    public void notifyClearCacheResult(String result) {
        if (isDestroyed()) {
            return;
        }

        if (result.equalsIgnoreCase(ConstantValue.SUCCESS)) {
            ToastUtil.showToast(this, R.string.cache_cleared);
        }

        appSettingPresenter.getCacheSize();
    }

    @Override
    public void notifyGetCacheSuccess(String size) {
        if (isDestroyed()) {
            return;
        }
        tvCacheSize.setText(size);
    }

    @Override
    public void notifyGetCacheFailed(String message) {
        if (isDestroyed()) {
            return;
        }
    }
}
