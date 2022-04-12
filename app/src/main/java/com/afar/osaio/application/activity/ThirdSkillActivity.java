package com.afar.osaio.application.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.electrician.activity.NameDeviceActivity;
import com.afar.osaio.smart.electrician.activity.ScanDeviceActivity;
import com.afar.osaio.smart.electrician.activity.WrongDeviceActivity;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.electrician.util.CommonUtil;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.BindTyDeviceResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.tuya.smart.sdk.bean.DeviceBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @Auther: 东北虎
 * @datetime: 2022/3
 * @desc:
 */
public class ThirdSkillActivity extends BaseActivity {

    private static String productId;
    @BindView(R.id.llSupportAlexa)
    LinearLayout llSupportAlexa;
    @BindView(R.id.llSupportGoogle)
    LinearLayout llSupportGoogle;
    @BindView(R.id.llSupportSmartThing)
    LinearLayout llSupportSmartThing;


    public static void toThirdSkillActivity(Context from,String uuid) {

      //  mDeviceId ="bb98d8b3e6ff98b2";
        Intent intent = new Intent(from, ThirdSkillActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_PRODUCTID, uuid);
        from.startActivity(intent);
        NooieLog.e("ThirdSkillActivity---mDeviceBean--uuid="+uuid);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_skill);
        ButterKnife.bind(this);
        initDevice();

    }

    public  void initDevice(){
        productId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_PRODUCTID);
        NooieLog.e("bindTyDevice----initDevice---productId="+productId);
        DeviceService.getService().getTuyaModel(productId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<BindTyDeviceResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.e("bindTyDevice----initDevice---onError()");
                        e.printStackTrace();
                        goHome();
                    }

                    @Override
                    public void onNext(BaseResponse<BindTyDeviceResult> baseResponse) {
                        if ( baseResponse != null && baseResponse.getData() != null && baseResponse.getCode() == StateCode.SUCCESS.code) {
                            if (baseResponse.getData().getList().size()>0){
                                initView(baseResponse.getData().getList().get(0));
                            }else{
                                goHome();
                            }

                        }else{
                            NooieLog.e("bindTyDevice----initDevice---onNext--error()---"+baseResponse.getMsg());
                            goHome();
                        }
                    }
                });

    }


    public   void initView(BindTyDeviceResult.BindTyDevice bindTyDevice){
        boolean isAlexa = bindTyDevice.getIs_alexa() == ApiConstant.THIRD_PARTY_CONTROL_SUPPORT;
        boolean isGoogleAssistant = bindTyDevice.getIs_google() == ApiConstant.THIRD_PARTY_CONTROL_SUPPORT;
        boolean isSmartThing = bindTyDevice.getIs_smart_thing() == ApiConstant.THIRD_PARTY_CONTROL_SUPPORT;
        NooieLog.e("bindTyDevice----isAlexa="+isAlexa+",isGoogleAssistant="+isGoogleAssistant+",isSmartThing="+isSmartThing);
        if (isAlexa || isGoogleAssistant || isSmartThing) {
            llSupportAlexa.setVisibility(isAlexa ? View.VISIBLE : View.GONE);
            llSupportGoogle.setVisibility(isGoogleAssistant ? View.VISIBLE : View.GONE);
            llSupportSmartThing.setVisibility(isSmartThing ? View.VISIBLE : View.GONE);
        }else{
            goHome();
        }
    }

    @OnClick({R.id.llSupportAlexa,R.id.llSupportGoogle,R.id.llSupportSmartThing,R.id.btnDeviceNext})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.llSupportAlexa:
                WebViewActivity.toWebViewActivity(this, "file:///android_asset/html/alexa.html", getResources().getString(R.string.connect_to_alexa));
                break;
            case R.id.llSupportGoogle: {
                WebViewActivity.toWebViewActivity(this, "file:///android_asset/html/google.html", getResources().getString(R.string.connect_to_assistant));
                break;
            }

            case R.id.llSupportSmartThing: {
                SmartThingActivity.toSmartThingActivity(this);
                break;
            }
            case R.id.btnDeviceNext: {
                goHome();
                break;
            }
        }
    }

    private   void goHome(){
        HomeActivity.toHomeActivity(ThirdSkillActivity.this, HomeActivity.TYPE_ADD_DEVICE);
    }

}
