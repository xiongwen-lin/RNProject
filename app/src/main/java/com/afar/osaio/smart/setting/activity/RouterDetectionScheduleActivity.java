package com.afar.osaio.smart.setting.activity;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.device.bean.ParentalControlRuleInfo;
import com.afar.osaio.smart.routerlocal.UpdataRouterConnectDeviceInfo;
import com.afar.osaio.smart.setting.adapter.RouterDetectionScheduleAdapter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.suke.widget.SwitchButton;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class RouterDetectionScheduleActivity extends BaseActivity implements SendHttpRequest.getRouterReturnInfo, DialogUtils.OnClickConfirmButtonListener {

    public static void toRouterDetectionScheduleActivity(Context from, String deviceMac, String deviceName, String overFlag, List<ParentalControlRuleInfo> parentalControlRuleInfos) {
        Intent intent = new Intent(from, RouterDetectionScheduleActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_MAC, deviceMac);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING, overFlag);
        intent.putExtra(ConstantValue.INTENT_KEY_PARETAL_CONTROL_RULE_MSG, (Serializable) parentalControlRuleInfos);
        from.startActivity(intent);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivDeviceIcon)
    ImageView ivDeviceIcon;
    @BindView(R.id.tvDeviceName)
    TextView tvDeviceName;
    @BindView(R.id.btnDetectionScheduleOption)
    TextView btnDetectionScheduleOption;
    @BindView(R.id.rcvDetectionSchedule)
    SwipeMenuRecyclerView rcvDetectionSchedule;
    @BindView(R.id.tvDetectionScheduleTip)
    TextView tvDetectionScheduleTip;

    @BindView(R.id.layout_router_time)
    View layout_router_time;
    @BindView(R.id.btnRouterScheduleSwitch)
    SwitchButton btnRouterScheduleSwitch;

    private RouterDetectionScheduleAdapter mScheduleAdapter;
    private List<DetectionSchedule> schedules = new ArrayList<>();
    private List<ParentalControlRuleInfo> parentalControlRuleInfoList = new ArrayList<>();
    private GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
    private DetectionSchedule mSchedule;
    private String deviceMac = "";
    private String deviceName = "";
    private String overFlag = "";
    private int mPosition = -1;
    // 规则按钮开关
    private boolean isChecked = true;

    private AlertDialog removeScheduleDialog;
    private int removePostion = -1; // 删除

    private static final int MIN_DELAY_TIME = 1000; // 两次点击间隔不能少于1000ms
    private static long lastClickTime;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_detection_schedule);
        ButterKnife.bind(this);

        setSideslipMenu();
        initView();
        initData();
    }

    /**
     * 显示删除 对话框
     */
    private void showRemoveDialog() {
        if (null == removeScheduleDialog) {
            removeScheduleDialog = DialogUtils.showConfirmDialog(this, R.string.router_remove_schedule_tip, this);
        }

        removeScheduleDialog.show();
    }

    public void initData() {
        deviceMac = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_MAC);
        deviceName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        overFlag = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING);
        parentalControlRuleInfoList = (List<ParentalControlRuleInfo>) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_PARETAL_CONTROL_RULE_MSG);
        if (parentalControlRuleInfoList == null || parentalControlRuleInfoList.size() <= 0) {
            return;
        }

        if ("".equals(deviceMac)) {
            btnRouterScheduleSwitch.setChecked("1".equals(parentalControlRuleInfoList.get(0).getState()) ? true : false);
            layout_router_time.setVisibility(View.VISIBLE);
        }

        if ("device".equals(overFlag)) {
            ivDeviceIcon.setImageResource(R.drawable.ic_connect_device);
        }

        if ("router".equals(overFlag)) {
            ivDeviceIcon.setImageResource(R.drawable.device_add_icon_lp_device_with_router);
        }

        loadScheduleInfo(parentalControlRuleInfoList);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.detection_schedule_label);
        //ivRight.setVisibility(View.GONE);
        ivRight.setImageResource(R.drawable.add_icon);

        btnRouterScheduleSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                // 设置路由器时间的开关
                setWiFiScheduleCfg(isChecked);
            }
        });

        rcvDetectionSchedule.setLayoutManager(new LinearLayoutManager(this));
        btnDetectionScheduleOption.setVisibility(View.GONE);
        setupDetectionSchedule();
    }


    // 2. 设置item侧滑菜单
    private void setSideslipMenu() {
        // 设置菜单创建器
        rcvDetectionSchedule.setSwipeMenuCreator(swipeMenuCreator);
        // 设置菜单Item点击监听
        rcvDetectionSchedule.setSwipeMenuItemClickListener(mMenuItemClickListener);
    }

    // 3. 创建侧滑菜单
    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            SwipeMenuItem deleteItem = new SwipeMenuItem(RouterDetectionScheduleActivity.this)
                    .setBackgroundColor(getResources().getColor(R.color.background_color_e5e5e5)) // 背景颜色
                    .setImage(R.drawable.delete_black)
                    /*.setText("Remove") // 文字。
                    .setTextColor(Color.WHITE) // 文字颜色。
                    .setTextSize(16) // 文字大小。*/
                    .setWidth(300) // 宽
                    .setHeight(MATCH_PARENT); //高（MATCH_PARENT意为Item多高侧滑菜单多高 （推荐使用））
            swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。
        }
    };

    // 4. 创建侧滑菜单的点击事件
    private SwipeMenuItemClickListener mMenuItemClickListener = new SwipeMenuItemClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge) {
//            showLoadingDialog();
            mPosition = -1;
            // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
            menuBridge.closeMenu();
            removePostion = menuBridge.getAdapterPosition();

            showRemoveDialog();

          /*  showLoadingDialog();
            mPosition = -1;
            // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
            menuBridge.closeMenu();
            //在menuBridge中我们可以得到侧滑的这一项item的position (menuBridge.getAdapterPosition())
            mScheduleAdapter.removeSchedule(menuBridge.getAdapterPosition());
            NooieLog.d("xxxxxxxxxxxxxxxxruleName: " + parentalControlRuleInfoList.get(menuBridge.getAdapterPosition()).getDeviceRuleName());
            if (!"".equals(deviceMac)) {
                delParentalRules(parentalControlRuleInfoList.get(menuBridge.getAdapterPosition()).getDeviceRuleName());
            } else {
                delWiFiScheduleCfg(parentalControlRuleInfoList.get(menuBridge.getAdapterPosition()).getDeviceRuleName());
            }
            if (parentalControlRuleInfoList.size() == 1) {
                layout_router_time.setVisibility(View.GONE);
            }
            if (parentalControlRuleInfoList.size() == 10) {
                ivRight.setVisibility(View.VISIBLE);
            }
            parentalControlRuleInfoList.remove(menuBridge.getAdapterPosition());*/
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (null != mScheduleAdapter && mScheduleAdapter.getItemCount() >= 10) {
            ivRight.setVisibility(View.GONE);
        }
    }


    private void release() {
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        ivDeviceIcon = null;
        tvDeviceName = null;
        parentalControlRuleInfoList.clear();
        btnDetectionScheduleOption = null;
        if (rcvDetectionSchedule != null) {
            rcvDetectionSchedule.setAdapter(null);
        }
        tvDetectionScheduleTip = null;
        if (mScheduleAdapter != null) {
            mScheduleAdapter.setListener(null);
            mScheduleAdapter.clearData();
        }
    }

    private void setupDetectionSchedule() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvDetectionSchedule.setLayoutManager(layoutManager);
        mScheduleAdapter = new RouterDetectionScheduleAdapter();
        mScheduleAdapter.setListener(new RouterDetectionScheduleAdapter.DetectionScheduleListener() {
            @Override
            public void onItemClick(DetectionSchedule schedule, int position) {
                mPosition = position;
                schedule.setEffective(true);
                gotoCreateSchedule(schedule);
            }

            @Override
            public void onItemSwitch(DetectionSchedule schedule, boolean isChecked, int position) {
                mPosition = position;
                switchSchedule(schedule, isChecked);
            }
        });

        rcvDetectionSchedule.setAdapter(mScheduleAdapter);
    }

    private void loadScheduleInfo(List<ParentalControlRuleInfo> parentalControlRuleInfos) {
        int startTimeH = 0;
        int startTimrM = 0;
        int endTimeH = 0;
        int endTimeM = 0;
        int ruleDay = 0;
        List<Integer> weekDays = new ArrayList<>();
        List<DetectionSchedule> detectionScheduleList = new ArrayList<>();

        for (int i = 0; i < parentalControlRuleInfos.size(); i++) {
            startTimeH = 60 * Integer.parseInt(parentalControlRuleInfos.get(i).getRuleStartTimeH());
            startTimrM = Integer.parseInt(parentalControlRuleInfos.get(i).getRuleStartTimeM());
            endTimeH = 60 * Integer.parseInt(parentalControlRuleInfos.get(i).getRuleEndTimeH());
            endTimeM = Integer.parseInt(parentalControlRuleInfos.get(i).getRuleEndTimeM());

            if (weekDays.size() > 0) {
                weekDays.clear();
            }

            for (int j = 0; j < parentalControlRuleInfos.get(i).getRuleTimeDay().size(); j++) {
                ruleDay = Integer.parseInt(parentalControlRuleInfos.get(i).getRuleTimeDay().get(j));
                // 原日期采用国外方式,周天为一周第一天,也就是用了1代表  周1--6 用了 2--7代表,这边设置路由器需要转化下格式
                // ruleDay 为路由器获取数据 按 1--7 排列周一到周天（所以要变为原先的日期排序）
                if (ruleDay == 7) {
                    ruleDay = 1;
                } else {
                    ruleDay = ruleDay + 1;
                }
                weekDays.add(ruleDay);
            }

            mSchedule = new DetectionSchedule((startTimeH + startTimrM), (endTimeH + endTimeM), Integer.parseInt(parentalControlRuleInfos.get(i).getState()) == 1 ? true : false);
            mSchedule.setWeekDays(weekDays);
            detectionScheduleList.add(mSchedule);
        }
        loadDetectionSchedule(detectionScheduleList);
    }


    /**
     * 防止快速点击
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

    /**
     * @param schedule
     */
    private void gotoCreateSchedule(DetectionSchedule schedule) {
        int scheduleId = schedule != null ? schedule.getId() : 0;
        if (!isFastClick()) {
            RouterDeviceCreateDetectionScheduleActivity.toRouterDeviceCreateDetectionScheduleActivity(
                    RouterDetectionScheduleActivity.this,
                    ConstantValue.REQUEST_CODE_SELECT_SCHEDULE,
                    schedule, scheduleId);
        }

    }

    private void switchSchedule(DetectionSchedule schedule, boolean isChecked) {
        showLoadingDialog();
        this.isChecked = isChecked;
        mSchedule = schedule;
        if (!"".equals(deviceMac)) {
            delParentalRules(parentalControlRuleInfoList.get(mPosition).getDeviceRuleName());
        } else {
            delWiFiScheduleCfg(parentalControlRuleInfoList.get(mPosition).getDeviceRuleName());
        }
    }

    private void updateDetectionSchedule(DetectionSchedule schedule) {
        if (schedules == null || schedules.size() < 0) {
            return;
        }
        //考虑?在原记录上修改,界面效果直接替换原来记录,路由器需要删除原先记录,再添加新改记录
        if (!"".equals(deviceMac)) {
            delParentalRules(parentalControlRuleInfoList.get(mPosition).getDeviceRuleName());
        } else {
            delWiFiScheduleCfg(parentalControlRuleInfoList.get(mPosition).getDeviceRuleName());
        }
        mScheduleAdapter.updataSchedule(mPosition, schedule);
    }

    private void loadDetectionSchedule(List<DetectionSchedule> detectionScheduleList) {
        if (detectionScheduleList == null || detectionScheduleList.size() <= 0) {
            return;
        }

        for (int i = 0; i < detectionScheduleList.size(); i++) {
            if ("".equals(deviceMac)) {
                detectionScheduleList.get(i).setScheduleType("router");
            }
            schedules.add(detectionScheduleList.get(i));
        }
        mScheduleAdapter.setData(schedules);
    }


    /**
     * 设置
     *
     * @param schedule
     * @param isCheck
     */
    private void setParentalRules(DetectionSchedule schedule, boolean isCheck) {
        List<Integer> listDay = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();
        listDay = schedule.getWeekDays();
        if (listDay.size() < 0) {
            return;
        }

        if (listDay.size() == 0) {
            schedule.resetWeekDays(true);
            listDay = schedule.getWeekDays();
        }

        for (int i = 0; i < listDay.size(); i++) {
            // 原日期采用国外方式,周天为一周第一天,也就是用了1代表  周1--6 用了 2--7代表,这边设置路由器需要转化下格式
            stringBuffer.append("" + (listDay.get(i) != 1 ? listDay.get(i) - 1 : 7));
            if (i != (listDay.size() - 1)) {
                stringBuffer.append(";");
            }
        }
        if (!"".equals(deviceMac)) {
            setParentalRules(stringBuffer, schedule, isCheck);
        } else {
            setWiFiScheduleCfg(stringBuffer, schedule, isCheck);
        }
    }

    private void delParentalRules(String rulesName) {
        List<String> rules = new ArrayList<>();
        rules.clear();
        rules.add(rulesName);
        try {
            routerDataFromCloud.delParentalRules(rules);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setParentalRules(StringBuffer stringBuffer, DetectionSchedule schedule, boolean isCheck) {
        try {
            routerDataFromCloud.setParentalRules(deviceMac, deviceName, stringBuffer.toString(),
                    schedule.getStartH() + ":" + schedule.getStartM(),
                    schedule.getEndH() + ":" + schedule.getEndM(), isCheck ? "1" : "0", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getParentalRules() {
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getParentalRules();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setWiFiScheduleCfg(boolean isChecked) {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setWiFiScheduleCfg(isChecked ? "1" : "0", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getWiFiScheduleCfg() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getWiFiScheduleCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setWiFiScheduleCfg(StringBuffer stringBuffer, DetectionSchedule schedule, boolean isCheck) {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setWiFiScheduleCfg(deviceName, isCheck ? "1" : "0", "1",
                    stringBuffer.toString(), "" + schedule.getStartH(), "" + schedule.getStartM(),
                    "" + schedule.getEndH(), "" + schedule.getEndM());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void delWiFiScheduleCfg(String rulesName) {
        showLoadingDialog();
        List<String> rules = new ArrayList<>();
        rules.clear();
        rules.add(rulesName);
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.delWiFiScheduleCfg(rules);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 判断是否已经添加过时间列表
    private boolean isAddScheduleCfg(DetectionSchedule schedule) {
        if (parentalControlRuleInfoList.size() <= 0) {
            return false;
        }


        for (int i = 0; i < parentalControlRuleInfoList.size(); i++) {

            if (schedule.getStartH() == Integer.parseInt(parentalControlRuleInfoList.get(i).getRuleStartTimeH())
                    && schedule.getStartM() == Integer.parseInt(parentalControlRuleInfoList.get(i).getRuleStartTimeM())
                    && schedule.getEndH() == Integer.parseInt(parentalControlRuleInfoList.get(i).getRuleEndTimeH())
                    && schedule.getEndM() == Integer.parseInt(parentalControlRuleInfoList.get(i).getRuleEndTimeM())) {

                if (schedule.getWeekDays().size() == parentalControlRuleInfoList.get(i).getRuleTimeDay().size()) {
                    StringBuilder daysString = new StringBuilder();
                    StringBuilder days2String = new StringBuilder();

                    List<Integer> listOne = sortNum(parentalControlRuleInfoList.get(i).getRuleTimeDay());
                    List<Integer> listTwo = sortNumInteger(schedule.getWeekDays());

                    for (int h = 0; h < listOne.size(); h++) {
                        // 转换为字符串
                        daysString.append(listOne.get(h));
                    }

                    for (int j = 0; j < listTwo.size(); j++) {
                        // 转换为字符串
                        days2String.append(listTwo.get(j));
                    }

                    if (daysString.toString().equals(days2String.toString())) {
                        return true;
                    }

                }

            }
        }

        return false;
    }


    /**
     * 排序
     *
     * @param list
     * @return
     */
    private List<Integer> sortNum(List<String> list) {
        List<Integer> numList = new ArrayList<>();
        for (String item : list) {
            numList.add(Integer.valueOf(item));
        }
        Collections.sort(numList);
        return numList;
    }


    /**
     * 排序
     *
     * @param list
     * @return
     */
    private List<Integer> sortNumInteger(List<Integer> list) {
        List<Integer> numList = new ArrayList<>();
        for (Integer item : list) {
            // 原日期采用国外方式,周天为一周第一天,也就是用了1代表  周1--6 用了 2--7代表,这边设置路由器需要转化下格式
            numList.add(item != 1 ? (item - 1) : 7);
        }
        Collections.sort(numList);
        return numList;
    }


    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                setParentalRules(mSchedule, isChecked);
            } else if (msg.what == 2) {
                if (!"".equals(deviceMac)) {
                    getParentalRules();
                } else {
                    getWiFiScheduleCfg();
                }
            }
        }
    };

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                if (mScheduleAdapter != null && mScheduleAdapter.getItemCount() < 10) {
                    mPosition = -1;
                    gotoCreateSchedule(null);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_SELECT_SCHEDULE:
                    if (intent != null) {
                        DetectionSchedule schedule = (DetectionSchedule) intent.getSerializableExtra(ConstantValue.INTENT_KEY_DATA_TYPE);

                        if (isAddScheduleCfg(schedule)) {
                            ToastUtil.showToast(RouterDetectionScheduleActivity.this, "当前规则已存在");
                            return;
                        }
                        showLoadingDialog();
                        isChecked = true;
                        if (mPosition == -1) { // 新增时间表
                            if ("".equals(deviceMac)) {
                                schedule.setScheduleType("router");
                            }
                            schedules.add(schedule);
                            mScheduleAdapter.setData(schedules);
                            setParentalRules(schedule, true);
                        } else {
                            // 原有时间表上改动
                            mSchedule = schedule;
                            updateDetectionSchedule(schedule);
                        }
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        try {
            if (("setParentalRules".equals(topicurlString) || "setWiFiScheduleCfg".equals(topicurlString)) && !"error".equals(info)) {
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            } else if (("getParentalRules".equals(topicurlString) || "getWiFiScheduleCfg".equals(topicurlString)) && !"error".equals(info)) {
                if (parentalControlRuleInfoList != null && parentalControlRuleInfoList.size() > 0) {
                    parentalControlRuleInfoList.clear();
                }
                if (!"".equals(deviceMac)) {
                    if (null !=parentalControlRuleInfoList){
                        parentalControlRuleInfoList.addAll(UpdataRouterConnectDeviceInfo.sortParentalRules(info, deviceMac));
                    }
                } else {
                    if (null !=parentalControlRuleInfoList){
                        parentalControlRuleInfoList = UpdataRouterConnectDeviceInfo.getWifiRules(info);
                    }
                }
                hideLoadingDialog();
            } else if (("delParentalRules".equals(topicurlString) || "delWiFiScheduleCfg".equals(topicurlString)) && !"error".equals(info)) {
                Message message = new Message();
                if (mPosition != -1) {
                    message.what = 1;
                } else {
                    message.what = 2;
                }
                handler.sendMessage(message);
            }

            if ("error".equals(info) || "".equals(info)) {
                hideLoadingDialog();
                //showConnectionRouterDialog();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public void onClickLeft() {

    }

    @Override
    public void onClickRight() {
        showLoadingDialog();
        mScheduleAdapter.removeSchedule(removePostion);
        schedules.remove(removePostion);
        if (!"".equals(deviceMac)) {
            delParentalRules(parentalControlRuleInfoList.get(removePostion).getDeviceRuleName());
        } else {
            delWiFiScheduleCfg(parentalControlRuleInfoList.get(removePostion).getDeviceRuleName());
        }
        if (parentalControlRuleInfoList.size() == 1) {
            layout_router_time.setVisibility(View.GONE);
        }
        if (parentalControlRuleInfoList.size() == 10) {
            ivRight.setVisibility(View.VISIBLE);
        }
        parentalControlRuleInfoList.remove(removePostion);
    }

}
