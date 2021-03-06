package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afar.osaio.R;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.device.bean.ParentalControlDeviceInfo;
import com.afar.osaio.smart.device.bean.ParentalControlRuleInfo;
import com.afar.osaio.smart.event.RouterOnLineStateEvent;
import com.afar.osaio.smart.home.adapter.ParentalControlAdapter;
import com.afar.osaio.smart.routerlocal.UpdataRouterConnectDeviceInfo;
import com.afar.osaio.smart.setting.activity.RouterDetectionScheduleActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class RouterParentalControlActivity extends RouterBaseActivity implements ParentalControlAdapter.OnClickParentalControlItemLicktener,
        SendHttpRequest.getRouterReturnInfo, DialogUtils.OnClickConfirmButtonListener {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tip)
    TextView tip;
    @BindView(R.id.recyviewItem)
    SwipeMenuRecyclerView swipeMenuRecyclerView;

    private ParentalControlAdapter parentalControlAdapter;
    List<ParentalControlDeviceInfo> deviceList = new ArrayList<>();
    List<String> rulesName = new ArrayList<>();
    private JSONArray onlineMsgJson;
    private String routerReturnString = "";
    private String onlineMsgString = "";
    private AlertDialog removeDeviceDialog;
    private int currentIndex = -1;// ????????????

    private static final int MIN_DELAY_TIME = 1000; // ??????????????????????????????1000ms
    private static long lastClickTime;


    /**
     * ??????????????????
     *
     * @return
     */
    public static boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= MIN_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }

    public static void toRouterParentalControlActivity(Context from, String onlineMsgString) {
        Intent intent = new Intent(from, RouterParentalControlActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_ONLINE_MSG, onlineMsgString);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_parental_control);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        initView();
        setSideslipMenu();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.add_icon);
        tvTitle.setText(R.string.router_parental_control);

        swipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // 2. ??????item????????????
    private void setSideslipMenu() {
        // ?????????????????????
        swipeMenuRecyclerView.setSwipeMenuCreator(swipeMenuCreator);
        // ????????????Item????????????
        swipeMenuRecyclerView.setSwipeMenuItemClickListener(mMenuItemClickListener);
    }

    // 3. ??????????????????
    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            SwipeMenuItem deleteItem = new SwipeMenuItem(RouterParentalControlActivity.this)
                    .setBackgroundColor(getResources().getColor(R.color.background_color_e5e5e5)) // ????????????
                    .setImage(R.drawable.delete_black)
                    /*.setText("Remove") // ?????????
                    .setTextColor(Color.WHITE) // ???????????????
                    .setTextSize(16) // ???????????????*/
                    .setWidth(300) // ???
                    .setHeight(MATCH_PARENT); //??????MATCH_PARENT??????Item???????????????????????? ?????????????????????
            swipeRightMenu.addMenuItem(deleteItem);// ???????????????????????????????????????
        }
    };

    // 4. ?????????????????????????????????
    private SwipeMenuItemClickListener mMenuItemClickListener = new SwipeMenuItemClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge) {
            // ??????????????????????????????????????????????????????Item???????????????????????????
            menuBridge.closeMenu();
            //???menuBridge???????????????????????????????????????item???position (menuBridge.getAdapterPosition())
            currentIndex = menuBridge.getPosition();
            showRemoveDialog();
        }
    };

    /**
     * ???????????? ?????????
     */
    private void showRemoveDialog() {
        if (null == removeDeviceDialog) {
            removeDeviceDialog = DialogUtils.showConfirmDialog(this,
                    R.string.router_parental_control_delete_device,
                    R.string.cancel, R.string.camera_share_remove, this);
        }

        removeDeviceDialog.show();
    }


    private void initData() {
        parentalControlAdapter = new ParentalControlAdapter();
        parentalControlAdapter.setParentalControlClickListener(this);
        swipeMenuRecyclerView.setAdapter(parentalControlAdapter);

        try {
            onlineMsgString = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ONLINE_MSG);
            onlineMsgJson = new JSONArray(onlineMsgString);
            if (getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ONLINE_MSG) == null
                    || "".equals(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ONLINE_MSG))) {
                onlineMsgString = "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        parentalControlAdapter.updata();
        getParentalRules();
    }

    private void setDeviceListInfo(ParentalControlDeviceInfo parentalControlDeviceInfo) {
        if (deviceList == null) {
            return;
        }

        if (deviceList.size() != 0) {
            for (int i = 0; i < deviceList.size(); i++) {
                if (deviceList.get(i).getDeviceMac().equals(parentalControlDeviceInfo.getDeviceMac())) {
                    // ????????????????????????????????????????????????
                    return;
                }
            }
        }
        deviceList.add(parentalControlDeviceInfo);
        parentalControlAdapter.setData(deviceList);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getString(R.string.router_parental_control_allow_internet)).append("(").append(deviceList.size()).append(")");
        tip.setText(stringBuilder);
        //getParentalRules();
    }

    private void getParentalRules() {
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getParentalRules();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void delParentalRules(List<String> rules) {
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.delParentalRules(rules);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void delDeviceAllParentalRules(String deviceMac) {
        showLoadingDialog();
        List<ParentalControlRuleInfo> parentalControlRuleInfos = UpdataRouterConnectDeviceInfo.sortParentalRules(routerReturnString, deviceMac);
        if (parentalControlRuleInfos == null || parentalControlRuleInfos.size() <= 0) {
            return;
        }
        rulesName.clear();
        for (int i = 0; i < parentalControlRuleInfos.size(); i++) {
            rulesName.add(parentalControlRuleInfos.get(i).getDeviceRuleName());
        }
        delParentalRules(rulesName);
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                if (!isFastClick()) {
                    RouterAddParentalControlActivity.toRouterAddParentalControlActivity(this, onlineMsgString, deviceList);
                }
                break;
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                parentalControlAdapter.setData(deviceList);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getString(R.string.router_parental_control_allow_internet)).append("(").append(deviceList.size()).append(")");
                tip.setText(stringBuilder);
            } else {
                EventBus.getDefault().post(new RouterOnLineStateEvent());
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        Message message = new Message();
        if (!"error".equals(info) && "getParentalRules".equals(topicurlString)) {
            routerReturnString = info;
            deviceList = UpdataRouterConnectDeviceInfo.sortParentalDevice(info, onlineMsgJson);
            message.what = 1;
        } else if (!"error".equals(info) && "delParentalRules".equals(topicurlString)) {
            message.what = 1;
        } else /*if ("error".equals(info) || "".equals(info))*/ {
            message.what = 0;
        }
        handler.sendMessage(message);
    }

    @Override
    public void ParentalControlItemClick(String deviceMac, String deviceName, boolean isProtected) {
        List<ParentalControlRuleInfo> parentalControlRuleInfos = UpdataRouterConnectDeviceInfo.sortParentalRules(routerReturnString, deviceMac);
        RouterDetectionScheduleActivity.toRouterDetectionScheduleActivity(this, deviceMac, deviceName, "device", parentalControlRuleInfos);
    }

    @Subscribe
    public void onGetConnectDeviceInfo(ParentalControlDeviceInfo parentalControlDeviceInfo) {
        setDeviceListInfo(parentalControlDeviceInfo);
    }

    @Override
    public void showLoadingDialog() {
        showLoading("");
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
    public void onClickLeft() {

    }

    @Override
    public void onClickRight() {
        if (currentIndex == -1) {
            return;
        }

        parentalControlAdapter.removeDevice(currentIndex);
        delDeviceAllParentalRules(deviceList.get(currentIndex).getDeviceMac());
        deviceList.remove(currentIndex);
    }
}
