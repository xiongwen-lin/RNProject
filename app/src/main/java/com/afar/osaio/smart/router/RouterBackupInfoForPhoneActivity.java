package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.home.fragment.DeviceListFragment;
import com.afar.osaio.smart.home.fragment.SmartDeviceListFragment;
import com.afar.osaio.smart.routerlocal.RouterDao;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.SetingsBackupDialog;
import com.suke.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RouterBackupInfoForPhoneActivity extends RouterBaseActivity implements SetingsBackupDialog.ConfirmListener {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.switchSleep)
    SwitchButton switchSleep;
    @BindView(R.id.btnNext)
    FButton btnNext;

    private String deviceName = "";
    private String deviceMac = "";
    private SetingsBackupDialog setingsBackupDialog;

    public static void toRouterBackupInfoForPhoneActivity(Context from, String deviceName) {
        Intent intent = new Intent(from, RouterBackupInfoForPhoneActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_backup_phone);
        ButterKnife.bind(this);

        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_setting_backup);

        deviceName =  getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        deviceMac = prefs.getRouterMac();

        switchSleep.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                setBackupToPhone(isChecked);
            }
        });

        setBackupToPhone(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.ivLeft, R.id.switchSleep, R.id.btnNext})
    public void onViewClicked(View view) {
        switch(view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnNext:
                showBackupRouterDialog();
                break;
        }
    }

    private void setBackupToPhone(boolean isBackup) {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        if (isBackup) {
            prefs.setRouterBackup("true", deviceName);
        } else {
            prefs.setRouterBackup("false", "");
        }
    }


    private void showBackupRouterDialog() {

        if (null ==setingsBackupDialog){
            setingsBackupDialog = new SetingsBackupDialog(this,this);
        }

        if (!setingsBackupDialog.isShowing()){
            setingsBackupDialog.show();
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

    private void addRouterDevice(String deviceName, String deviceMac, int deviceType) {
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Integer integer) {
                        RouterDao.getInstance(NooieApplication.mCtx).addRouter(deviceName, deviceMac);
                        return Observable.just(true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean result) {
                        //DeviceListFragment.addRouterDevice(deviceName, deviceMac, 6);
                        if (NooieApplication.TEST_MODE) {
                            //SmartDeviceListFragment.addRouterDevice(deviceName, deviceMac, 6);
                        } else {
                            DeviceListFragment.addRouterDevice(deviceName, deviceMac, 6);
                        }
                    }
                });
    }

    @Override
    public void confirmListener() {
        addRouterDevice(deviceName, deviceMac, 6);
        HomeActivity.toHomeActivity(RouterBackupInfoForPhoneActivity.this);
    }
}
