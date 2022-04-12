package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.routerlocal.RouterDao;
import com.afar.osaio.smart.routerlocal.RouterInfo;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterConnectionWifiActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    public static void toRouterConnectionWifiActivity(Context from) {
        Intent intent = new Intent(from, RouterConnectionWifiActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_wifi);
        ButterKnife.bind(this);

        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_router_connect);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void dealwithRouterStatusInfo(JSONObject jsonObject) {
        try {
            toActivity(jsonObject.getString("lanMac"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void toActivity(String mac) {
        RouterDao routerDao = RouterDao.getInstance(this);
        RouterInfo routerInfo = routerDao.findRouter(mac);
        String routerName = routerInfo.getRouterName();
        if (!"".equals(routerName)) {
            // 已经添加过
            ToastUtil.showToast(this, getString(R.string.router_router_connect_wifi_msg));
        } else {
            RecoverRouterBackupActivity.toRecoverRouterBackupActivity(this);
            //ConnectionRouterActivity.toConnectionRouterActivity(RouterConnectionWifiActivity.this);
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnConnection, R.id.tvNotFindWifi})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnConnection:
                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),10086);
                break;
            case R.id.tvNotFindWifi:
                RouterOfflineHelpActivity.toRouterOfflineHelpActivity(this);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            showLoadingDialog();
            isConnectRouterWifi();
    }

    private void isConnectRouterWifi() {
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getSysStatusCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Dialog mShowConnectRouterFailDialog;

    private void showConnectRouterFailDialog() {
        hideConnectRouterFailDialog();
        mShowConnectRouterFailDialog = DialogUtils.connectRouterFailDialog(this, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),10086);
            }

            @Override
            public void onClickLeft() {
                hideConnectRouterFailDialog();
                finish();
            }
        }, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
    }

    private void hideConnectRouterFailDialog() {
        if (mShowConnectRouterFailDialog != null) {
            mShowConnectRouterFailDialog.dismiss();
            mShowConnectRouterFailDialog = null;
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                showConnectRouterFailDialog();
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        try {
            if ("error".equals(info) || "".equals(info)) {
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
            } else {
                dealwithRouterStatusInfo(new JSONObject(info));
            }
        } catch (JSONException e) {
            e.printStackTrace();
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

    @Override
    public void showLoadingDialog() {
        showLoading("");
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }
}
