package com.afar.osaio.smart.router;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.event.RouterOnLineStateEvent;
import com.afar.osaio.widget.FButton;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterTimeZoneActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    public static void toRouterTimeZoneActivity(Context from) {
        Intent intent = new Intent(from, RouterTimeZoneActivity.class);
        from.startActivity(intent);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.show_time)
    TextView show_time;
    @BindView(R.id.copy_time)
    TextView copy_time;
    @BindView(R.id.sync_time_checkbox)
    CheckBox sync_time_checkbox;
    @BindView(R.id.btnSave)
    FButton btnSave;

    private long routerNtpTime = 0;
    private boolean isSyncTime = false;
    JSONObject jsonObject = new JSONObject();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_time_zone);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_time_settings);
        sync_time_checkbox.setTag(0);

        sync_time_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sync_time_checkbox.setTag(b ? 1 : 0);
                setSaveEnabled(b);
            }
        });

        btnSave.setEnabled(false);
        btnSave.setTextColor(getResources().getColor(R.color.unable_clickable_color));
    }

    private void initData() {
        getNtpCfg();
        //new TimeThread().start();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.ivLeft, R.id.btnSave, R.id.copy_time})
    public void onViewClicked(View view) {
        switch(view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnSave:
                // 路由器同步时间
                showLoadingDialog();
                if (isSyncTime) {
                    syncRouterTime();
                }
                if ((int)sync_time_checkbox.getTag() == 1) {
                    setNtpCfg();
                }
                break;
            case R.id.copy_time:
                isSyncTime = true;
                setSaveEnabled(true);
                break;
        }
    }

    private void getNtpCfg() {
        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getNtpCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void syncRouterTime() {
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.NTPSyncWithHost(show_time.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setNtpCfg() {
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.setNtpCfg("UTC" + timeZoneConversion()/*jsonObject.getString("tz")*/, "" + (int)sync_time_checkbox.getTag(), jsonObject.getString("server"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setSaveEnabled(boolean isEnabled) {
        if (isEnabled) {
            btnSave.setEnabled(true);
            btnSave.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            if (isSyncTime){
                btnSave.setEnabled(true);
                btnSave.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            }else{
                btnSave.setEnabled(false);
                btnSave.setTextColor(getResources().getColor(R.color.unable_clickable_color));
            }

        }
    }

    private void separateNtpTime(String info) {
        try {
            jsonObject = new JSONObject(info);
            String[] listTime = (jsonObject.getString("currentTime")).split(" ");
            // 路由器缺陷，当调用时日期小于10返回的数据会多一个空格
            if ("".equals(listTime[2])) {
                routerNtpTime = formatDate(listTime[6], listTime[1], listTime[3], listTime[4]);
            } else {
                routerNtpTime = formatDate(listTime[5], listTime[1], listTime[2], listTime[3]);
            }
            new TimeThread().start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * format date time
     *
     * @param year
     * @param monthString
     * @param dayString
     * @return
     */
    private long formatDate(String year, String monthString, String dayString, String time) {
        int month = -1;
        int day = -1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder builder = new StringBuilder();
        builder.append(year);
        builder.append("-");
        month = getTimeMonth(monthString);
        if (month < 10) {
            builder.append(0);
        }
        builder.append(month);
        builder.append("-");
        day = Integer.parseInt(dayString);
        if (day < 10) {
            builder.append(0);
        }
        builder.append(day);

        // 00:00:00
        builder.append(" ");
        builder.append(time);

        try {
            return dateFormat.parse(builder.toString()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTimeMonth(String monthString) {
        int num = 1;
        if (monthString.contains("Jan")) {
            num = 1;
        } else if (monthString.contains("Feb")) {
            num = 2;
        } else if (monthString.contains("Mar")) {
            num = 3;
        } else if (monthString.contains("Apr")) {
            num = 4;
        } else if (monthString.contains("May")) {
            num = 5;
        } else if (monthString.contains("Jun")) {
            num = 6;
        } else if (monthString.contains("Jul")) {
            num = 7;
        } else if (monthString.contains("Aug")) {
            num = 8;
        } else if (monthString.contains("Sep")) {
            num = 9;
        } else if (monthString.contains("Oct")) {
            num = 10;
        } else if (monthString.contains("Nov")) {
            num = 11;
        } else if (monthString.contains("Dec")) {
            num = 12;
        }
        return num;
    }

    private static final int msgKey = 1;
    private static final int msgRouterKey = 2;
    public class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Message msg = new Message();
                    if (isSyncTime) {
                        msg.what = msgKey;
                    } else {
                        msg.what = msgRouterKey;
                    }
                    mHandler.sendMessage(msg);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CharSequence sysTimeStr = "";
            switch (msg.what) {
                case msgKey:
                    long sysTime = System.currentTimeMillis();
                    sysTimeStr = DateFormat
                            .format("yyyy-MM-dd HH:mm:ss", sysTime);
                    break;
                case msgRouterKey:
                    routerNtpTime+=1000;
                    sysTimeStr = DateFormat
                            .format("yyyy-MM-dd HH:mm:ss", routerNtpTime);
                    break;
                default:
                    break;
            }
            show_time.setText(sysTimeStr);
        }
    };

    private String info;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 0) {
                    EventBus.getDefault().post(new RouterOnLineStateEvent());
                } else if (msg.what == 1) {
                    JSONObject jsonObject = null;
                    jsonObject = new JSONObject(info);
                    sync_time_checkbox.setChecked(Integer.parseInt(jsonObject.getString("enable")) == 1 ? true : false);
                    sync_time_checkbox.setTag(Integer.parseInt(jsonObject.getString("enable")));
                    setSaveEnabled(Integer.parseInt(jsonObject.getString("enable")) == 1 ? true : false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        long sysTime = System.currentTimeMillis();
        hideLoadingDialog();
        if ("NTPSyncWithHost".equals(topicurlString) && !"error".equals(info)) {
            if ((int)sync_time_checkbox.getTag() == 0) {
                finish();
            }
        } else if ("getNtpCfg".equals(topicurlString) && !"error".equals(info)) {
            separateNtpTime(info);
            this.info = info;
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        } else if ("setNtpCfg".equals(topicurlString) && !"error".equals(info)) {
            finish();
        } else {
            Message message = new Message();
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    @Override
    public String timeZoneConversion() {
        return super.timeZoneConversion();
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
