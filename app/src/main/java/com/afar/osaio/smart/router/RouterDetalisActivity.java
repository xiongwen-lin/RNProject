package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.device.bean.RouterDetalisInfo;
import com.afar.osaio.smart.event.RouterOnLineStateEvent;
import com.afar.osaio.smart.home.adapter.RouterDetalisAdapter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

// AESUtils
public class RouterDetalisActivity extends RouterBaseActivity implements RouterDetalisAdapter.OnClickRouterDetalisItemListener,
        SendHttpRequest.getRouterReturnInfo {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.download_rate)
    TextView download_rate;
    @BindView(R.id.upload_rate)
    TextView upload_rate;
    @BindView(R.id.rate_util1)
    TextView rate_util1;
    @BindView(R.id.rate_util2)
    TextView rate_util2;
    @BindView(R.id.tvBind)
    TextView tvBind;
    @BindView(R.id.detailsRecy)
    RecyclerView detailsRecy;

    private List<RouterDetalisInfo> list;
    private RouterDetalisAdapter routerDetalisAdapter;
    private String routerReturnOnlineMsg = "";
    private String routerNetMsg = "";
    private String routerName = "";
    private String routerMac = "";
    private String isBind = "";
    private String routerConnectMsg = "";

    public final static int SCAN_PER_TIME_LEN = 5 * 1000;
    private final int[] flag = {0};

    public static void toRouterDetalisActivity(Context from, String routerName, String routerMac, String isBind) {
        Intent intent = new Intent(from, RouterDetalisActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, routerName);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_MAC, routerMac);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ISBIND, isBind);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtiy_router_detalis);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.set_icon);
        tvTitle.setText(R.string.router_detail_router);

        // 未绑定,绑定账号即可远程访问
        /*String url1 = getString(R.string.router_detail_unbind_tip_one);
        String url2 = getString(R.string.router_detail_unbind_tip_two);
        String url3 = getString(R.string.router_detail_unbind_tip_three);
        String urlString = url1 + url2 + url3;

        SpannableStringBuilder style = new SpannableStringBuilder();
        style.append(urlString);

        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                toBindRouter();
            }
        };
        style.setSpan(conditionClickableSpan, urlString.indexOf(url2), urlString.indexOf(url2) + url2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvBind.setText(style);


        //设置部分文字颜色
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_blue));
        style.setSpan(conditionForegroundColorSpan, urlString.indexOf(url2), urlString.indexOf(url2) + url2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvBind.setMovementMethod(LinkMovementMethod.getInstance());
        tvBind.setText(style);*/
        tvBind.setVisibility(View.GONE);

        detailsRecy.setLayoutManager(new GridLayoutManager(this, 2));
        detailsRecy.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void initData() {
        list = new ArrayList<>();
        routerDetalisAdapter = new RouterDetalisAdapter();
        routerDetalisAdapter.setClickRouterDetalisItemListener(this);
        list = routerDetalisInfo();
        routerDetalisAdapter.setData(list);
        detailsRecy.setAdapter(routerDetalisAdapter);

        routerName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        routerMac = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_MAC);
        isBind = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ISBIND);
        /*if ("0".equals(isBind)) {
            tvBind.setVisibility(View.VISIBLE);
        }*/
        tvTitle.setText(routerName);
        tvBind.setVisibility(View.GONE);
        interval();
        //setCheakRouterVersion();
        CloudSrvVersionCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getAccessDeviceCfg();
    }

    /**
     * 每隔SCAN_PER_TIME_LEN毫秒后执行next操作
     */
    private Subscription mCountSubscription;

    private void interval() {
        mCountSubscription = Observable.interval(SCAN_PER_TIME_LEN, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        getNetInfoCfg();
                        getSysStatusCfg();
                        if (flag[0] == 0) {
                            setCheakRouterVersion();
                            flag[0]++;
                        }
                    }
                });
    }

    private void toBindRouter() {
        RouterBindToDeviceActivity.toRouterBindToDeviceActivity(this);
    }

    private List<RouterDetalisInfo> routerDetalisInfo() {
        List<RouterDetalisInfo> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            RouterDetalisInfo routerDetalisInfo;
            if (i == 0) {
                routerDetalisInfo = new RouterDetalisInfo(NooieApplication.mCtx.getString(R.string.router_detail_connect_device), true, 0);
            } else if (i == 1) {
                routerDetalisInfo = new RouterDetalisInfo(NooieApplication.mCtx.getString(R.string.router_detail_parental_control), true, 0);
            } /*else if (i == 2) {
                routerDetalisInfo = new RouterDetalisInfo("Security Settings",  true, 0);
            } */ else if (i == 2) {
                routerDetalisInfo = new RouterDetalisInfo(NooieApplication.mCtx.getString(R.string.router_detail_wifi_manager), true, 0);
            } else if (i == 3) {
                routerDetalisInfo = new RouterDetalisInfo(NooieApplication.mCtx.getString(R.string.router_detail_guest_wifi), true, 0);
            } else if (i == 4) {
                routerDetalisInfo = new RouterDetalisInfo(NooieApplication.mCtx.getString(R.string.router_detail_net_setting), true, 0);
            } else {
                routerDetalisInfo = new RouterDetalisInfo(NooieApplication.mCtx.getString(R.string.router_detail_time_setting), true, 0);
            }
            list.add(routerDetalisInfo);
        }
        return list;
    }

    private void clickJumpForActivity(int position) {
        switch (position) {
            case 0:
                RouterDeviceConnectActivity.toRouterDeviceConnectActivity(this, routerReturnOnlineMsg);
                break;
            case 1:
                setParentalRules();
                break;
            /*case 2:
                RouterDeviceAccessControlActivity.toRouterDeviceAccessControlActivity(this, routerReturnOnlineMsg);
                break;*/
            case 2:
                RouterSetWifiActivity.toRouterSetWifiActivity(this, routerName, "wifiManagement"/*, routerReturnOnlineMsg*/);
                break;
            case 3:
                RouterGuestWifiActivity.toRouterGuestWifiActivity(this);
                break;
            case 4:
                RouterInternetModeSettingActivity.toRouterInternetModeSettingActivity(this, "network");
                break;
            case 5:
                RouterTimeZoneActivity.toRouterTimeZoneActivity(this);
                break;
        }
    }

    private void routerDetalisSetInfo() {
        RouterDetalisSetActivity.toRouterDetalisSetActivity(this, routerName, routerMac);
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                routerDetalisSetInfo();
                break;
        }
    }

    private void routerOnlineDevice() throws JSONException {
        JSONArray jsonArrayOnlineDevice = new JSONArray();
        jsonArrayOnlineDevice = dealwithOnlineDevice(routerReturnOnlineMsg);
        routerReturnOnlineMsg = "";
        routerReturnOnlineMsg = jsonArrayOnlineDevice.toString();
        routerDetalisAdapter.updataOnlineView(jsonArrayOnlineDevice.length());
    }

    private String newVersion = "";

    private void dealwithRouterUpdata(JSONObject jsonObject) {
        int num = 0;
        Message message = new Message();
        try {
            num = Integer.parseInt(jsonObject.getString("cloudFwStatus"));
            switch (num) {
                case 4:
                    newVersion = jsonObject.getString("newVersion");
                    message.what = 0;
                    handler.sendMessage(message);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Dialog mShowUpdataRouterDialog;

    private void showUpdataRouterDialog() {
        hideUpdataRouterDialog();
        mShowUpdataRouterDialog = DialogUtils.updataRouterDialog(this, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                RouterBindToDeviceActivity.toRouterBindToDeviceActivity(RouterDetalisActivity.this, "firmware", newVersion);
            }

            @Override
            public void onClickLeft() {

            }
        }, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
    }

    private void hideUpdataRouterDialog() {
        if (mShowUpdataRouterDialog != null) {
            mShowUpdataRouterDialog.dismiss();
            mShowUpdataRouterDialog = null;
        }
    }

    /**
     * 获取设备连接状态信息
     */
    private void getSysStatusCfg() {
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getSysStatusCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 是否开启父母控制
    private void setParentalRules() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setParentalRules("1", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getAccessDeviceCfg() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getAccessDeviceCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getNetInfoCfg() {
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getNetInfoCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void CloudSrvVersionCheck() {
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.cloudSrvVersionCheck();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setCheakRouterVersion() {
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getCloudSrvCheckStatus();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String[] downs;
    private String[] ups;
    private void rateDealwith() {
        try {
            downs = new JSONObject(routerNetMsg).getString("down").split("\\.");
            ups = new JSONObject(routerNetMsg).getString("up").split("\\.");
            // 保留后面三位小数，所以这里就大致算一下
            if (Integer.parseInt(downs[0]) >= 1024) {
                rate_util1.setText(R.string.router_detail_unit_mbps);
                download_rate.setText(String.valueOf((int)(Integer.parseInt(downs[0])/1024)) + "." + (Integer.parseInt(downs[0])%1024));
            } else {
                rate_util1.setText(R.string.router_detail_unit_kbps);
                download_rate.setText(new JSONObject(routerNetMsg).getString("down"));
            }

            if (Integer.parseInt(ups[0]) >= 1024) {
                rate_util2.setText(R.string.router_detail_unit_mbps);
                upload_rate.setText(String.valueOf((int)(Integer.parseInt(ups[0])/1024)) + "." + (Integer.parseInt(ups[0])%1024));
            } else {
                rate_util2.setText(R.string.router_detail_unit_kbps);
                upload_rate.setText(new JSONObject(routerNetMsg).getString("up"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 0) {
                    showUpdataRouterDialog();
                } else if (msg.what == 1) {
                    routerOnlineDevice();
                } else if (msg.what == 2) {
                    rateDealwith();
                } else if (msg.what == 3) {
                    //setCheakRouterVersion();
                } else if (msg.what == 4) {
                    // 停止定时获取
                    if (!mCountSubscription.isUnsubscribed()) {
                        mCountSubscription.unsubscribe();
                    }
                    EventBus.getDefault().post(new RouterOnLineStateEvent());
//                    showConnectionRouterDialog();
                } else if (msg.what == 5) {
                    checkWifiState();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        try {
            if (mCountSubscription.isUnsubscribed()) {
                interval();
            }

            if ("getAccessDeviceCfg".equals(topicurlString) && !"error".equals(info)) {
                routerReturnOnlineMsg = info;
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            } else if ("setParentalRules".equals(topicurlString) && !"error".equals(info)) {

                RouterParentalControlActivity.toRouterParentalControlActivity(this, routerReturnOnlineMsg);
            } else if ("getNetInfoCfg".equals(topicurlString) && !"error".equals(info)) {
                routerNetMsg = info;
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            } else if ("CloudSrvVersionCheck".equals(topicurlString) && !"error".equals(info)) {
                Message message = new Message();
                message.what = 3;
                handler.sendMessage(message);
            } else if (!"error".equals(info) && "getCloudSrvCheckStatus".equals(topicurlString)) {
                dealwithRouterUpdata(new JSONObject(info));
            } else if ("getSysStatusCfg".equals(topicurlString) && !"error".equals(info)) {
                routerConnectMsg = info;
                handler.sendEmptyMessage(5);
            } else if ("error".equals(info) || "".equals(info)) {
                Message message = new Message();
                message.what = 4;
                handler.sendMessage(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 检查wifi 状态
     */
    private void checkWifiState() {
        if (TextUtils.isEmpty(routerConnectMsg)) {
            return;
        }
        try {
            JSONObject wifiStateJO = new JSONObject(routerConnectMsg);
            String lanMac = wifiStateJO.getString("lanMac");

            if (!lanMac.equals(routerMac)) {
                EventBus.getDefault().post(new RouterOnLineStateEvent());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clickRouterDetalisItem(int position) {
        clickJumpForActivity(position);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCountSubscription.unsubscribe();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCountSubscription = null;
    }

    @Override
    public void showLoadingDialog() {
        showLoading("");
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }
}
