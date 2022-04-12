package com.afar.osaio.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * SelectWeekView
 *
 * @author Administrator
 * @date 2019/3/26
 */
public class SelectWeekView extends LinearLayout {

    @BindView(R.id.btnSelectMon)
    TextView btnSelectMon;
    @BindView(R.id.btnSelectTues)
    TextView btnSelectTues;
    @BindView(R.id.btnSelectWed)
    TextView btnSelectWed;
    @BindView(R.id.btnSelectThur)
    TextView btnSelectThur;
    @BindView(R.id.btnSelectFri)
    TextView btnSelectFri;
    @BindView(R.id.btnSelectSat)
    TextView btnSelectSat;
    @BindView(R.id.btnSelectSun)
    TextView btnSelectSun;

    private Map<Integer,TextView> mWeekDays = new HashMap<>();

    public SelectWeekView(Context context) {
        this(context, null);
    }
    
    public SelectWeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_select_week, this, false);
        ButterKnife.bind(this, view);
        setupView();
        addView(view);
    }

    public static final int WEEK_UNSELECTED = 0;
    public static final int WEEK_SELECTED = 1;

    private void setupView() {
        btnSelectMon.setTag(WEEK_UNSELECTED);
        btnSelectTues.setTag(WEEK_UNSELECTED);
        btnSelectWed.setTag(WEEK_UNSELECTED);
        btnSelectThur.setTag(WEEK_UNSELECTED);
        btnSelectFri.setTag(WEEK_UNSELECTED);
        btnSelectSat.setTag(WEEK_UNSELECTED);
        btnSelectSun.setTag(WEEK_UNSELECTED);

        mWeekDays.clear();
        mWeekDays.put(Calendar.MONDAY, btnSelectMon);
        mWeekDays.put(Calendar.TUESDAY, btnSelectTues);
        mWeekDays.put(Calendar.WEDNESDAY, btnSelectWed);
        mWeekDays.put(Calendar.THURSDAY, btnSelectThur);
        mWeekDays.put(Calendar.FRIDAY, btnSelectFri);
        mWeekDays.put(Calendar.SATURDAY, btnSelectSat);
        mWeekDays.put(Calendar.SUNDAY, btnSelectSun);
    }

    @OnClick({R.id.btnSelectMon, R.id.btnSelectTues, R.id.btnSelectWed, R.id.btnSelectThur, R.id.btnSelectFri, R.id.btnSelectSat, R.id.btnSelectSun})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btnSelectMon: {
                changeBtnState(btnSelectMon);
                break;
            }
            case R.id.btnSelectTues: {
                changeBtnState(btnSelectTues);
                break;
            }
            case R.id.btnSelectWed: {
                changeBtnState(btnSelectWed);
                break;
            }
            case R.id.btnSelectThur: {
                changeBtnState(btnSelectThur);
                break;
            }
            case R.id.btnSelectFri: {
                changeBtnState(btnSelectFri);
                break;
            }
            case R.id.btnSelectSat: {
                changeBtnState(btnSelectSat);
                break;
            }
            case R.id.btnSelectSun: {
                changeBtnState(btnSelectSun);
                break;
            }
        }
    }

    private void changeBtnState(TextView view) {
        if ((int)view.getTag() == WEEK_SELECTED) {
            view.setTag(WEEK_UNSELECTED);
            view.setBackgroundResource(R.drawable.circle_round_white_state_15);
            view.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        } else {
            view.setTag(WEEK_SELECTED);
            view.setBackgroundResource(R.drawable.circle_round_green_state_15);
            view.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        }
    }

    public void setBtnSelected(List<Integer> weekDays) {
        for (Integer weekDay : CollectionUtil.safeFor(weekDays)) {
            if (mWeekDays.containsKey(weekDay) && mWeekDays.get(weekDay) != null) {
                mWeekDays.get(weekDay).setTag(WEEK_SELECTED);
                mWeekDays.get(weekDay).setBackgroundResource(R.drawable.circle_round_green_state_15);
                mWeekDays.get(weekDay).setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            }
        }
    }

    public List<Integer> getSelectedDays() {
        List<Integer> selectedDays = new ArrayList();
        for (Map.Entry<Integer, TextView> weekDay : mWeekDays.entrySet()) {
            if (weekDay.getValue().getTag() != null && (int)(weekDay.getValue().getTag()) == WEEK_SELECTED) {
                selectedDays.add(weekDay.getKey());
            }
        }
        return selectedDays;
    }

    public int convertWeekDay(int i) {
        int weekDay = -1;
        switch (i) {
            case 0:
                weekDay = Calendar.MONDAY;
                break;
            case 1:
                weekDay = Calendar.TUESDAY;
                break;
            case 2:
                weekDay = Calendar.WEDNESDAY;
                break;
            case 3:
                weekDay = Calendar.THURSDAY;
                break;
            case 4:
                weekDay = Calendar.FRIDAY;
                break;
            case 5:
                weekDay = Calendar.SATURDAY;
                break;
            case 6:
                weekDay = Calendar.SUNDAY;
                break;
        }

        return weekDay;
    }

    public void release() {
        btnSelectMon = null;
        btnSelectTues = null;
        btnSelectWed = null;
        btnSelectThur = null;
        btnSelectFri = null;
        btnSelectSat = null;
        btnSelectSun = null;
        if (mWeekDays != null) {
            mWeekDays.clear();
            mWeekDays = null;
        }
    }

    public void setSelectedAll(){
        changeBtnState(btnSelectMon);
        changeBtnState(btnSelectTues);
        changeBtnState(btnSelectWed);
        changeBtnState(btnSelectThur);
        changeBtnState(btnSelectFri);
        changeBtnState(btnSelectSat);
        changeBtnState(btnSelectSun);
    }

    //设置定点定时选中的状态
    public void setActionTimerSelectedDays(String loops){
        if (!TextUtils.isEmpty(loops) && loops.length() == 7){
            char[] chars = loops.toCharArray();
            for (int i = 0; i < chars.length; i++) {

                if (i == 0 && chars[i] == '1'){
                    changeBtnState(btnSelectSun);
                }

                if (i == 1 && chars[i] == '1'){
                    changeBtnState(btnSelectMon);
                }

                if (i == 2 && chars[i] == '1'){
                    changeBtnState(btnSelectTues);
                }

                if (i == 3 && chars[i] == '1'){
                    changeBtnState(btnSelectWed);
                }

                if (i == 4 && chars[i] == '1'){
                    changeBtnState(btnSelectThur);
                }

                if (i == 5 && chars[i] == '1'){
                    changeBtnState(btnSelectFri);
                }

                if (i == 6 && chars[i] == '1'){
                    changeBtnState(btnSelectSat);
                }
            }
        }
    }
}
