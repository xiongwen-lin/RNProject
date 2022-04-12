package com.afar.osaio.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.smart.player.delegate.IPlayerDelegate;
import com.afar.osaio.smart.player.delegate.PlayState;
import com.afar.osaio.widget.adapter.UtcSelectDateAdapter;
import com.afar.osaio.widget.listener.UtcSelectDataListener;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.common.utils.tool.TaskUtil;

import java.util.Calendar;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by victor on 2018/7/13
 * Email is victor.qiao.0604@gmail.com
 */
public class UtcSelectDateView extends LinearLayout {
    private RecyclerView recyclerView;
    private boolean isHorizontal;

    private UtcSelectDateAdapter mDataAdapter;

    public UtcSelectDateView(Context context) {
        this(context, null);
    }

    public UtcSelectDateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UtcSelectDateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DateSelect);
        isHorizontal = array.getBoolean(R.styleable.DateSelect_isHorizontal, true);

        setOrientation(isHorizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

        initView(context);
        initData(context);
        if (array != null) {
            array.recycle();
        }
    }

    public void setData(List<CalenderBean> list) {
        if (mDataAdapter != null) {
            mDataAdapter.setData(list);
        }
        TaskUtil.delayAction(100, new TaskUtil.OnDelayTimeFinishListener() {
            @Override
            public void onFinish() {
                if (mDataAdapter == null || recyclerView == null) {
                    return;
                }
                if (mDataAdapter.getItemCount() > 0) {
                    recyclerView.smoothScrollToPosition(mDataAdapter.getItemCount() - 1);
                }
            }
        });
    }

    public void updateData() {
        if (mDataAdapter != null) {
            mDataAdapter.notifyDataSetChanged();
        }
    }

    public void setListener(UtcSelectDataListener listener) {
        if (mDataAdapter != null) {
            mDataAdapter.setListener(listener);
        }
    }

    public void setOnStartScrollListener(RecyclerView.OnScrollListener listener) {
        if (recyclerView != null) {
            recyclerView.addOnScrollListener(listener);
        }
    }

    public void scrollToCurrent() {
        if (mDataAdapter == null || recyclerView == null) {
            return;
        }
        CalenderBean calenderBean = mDataAdapter.currentSelectData();
        int current = calenderBean != null ? mDataAdapter.indexPosition(calenderBean) : mDataAdapter.getItemCount() - 1;
        if (current >= 0 && current < mDataAdapter.getItemCount()) {
            recyclerView.smoothScrollToPosition(current);
        }
    }

    public void scrollToLast() {
        if (mDataAdapter == null || recyclerView == null) {
            return;
        }
        int current = mDataAdapter.getItemCount() - 1;
        if (current >= 0 && current < mDataAdapter.getItemCount()) {
            recyclerView.smoothScrollToPosition(current);
        }
    }

    public void setCurrentItem(long time) {
        Calendar calendar = DateTimeUtil.getUtcCalendar();
        calendar.setTimeInMillis(time);
        if (mDataAdapter != null) {
            mDataAdapter.setSelectDate(calendar);
        }
    }

    public void setTextColor(int textColor) {
        if (mDataAdapter != null) {
            mDataAdapter.setTextColor(textColor);
        }
    }

    public void updateCurrentState(Calendar calender, long currentSeekDay) {
        if (mDataAdapter != null) {
            mDataAdapter.updateCurrentState(calender, currentSeekDay);
        }
    }

    public void updateSelectDataList(int[] recDateList) {
        if (mDataAdapter != null) {
            mDataAdapter.updateSelectDataList(recDateList);
        }
    }

    public void setPlayState(PlayState playState) {
        if (mDataAdapter != null) {
            mDataAdapter.setPlayState(playState);
        }
    }

    public void setPlayerDelegate(IPlayerDelegate playerDelegate) {
        if (mDataAdapter != null) {
            mDataAdapter.setPlayerDelegate(playerDelegate);
        }
    }

    public void release() {
        if (mDataAdapter != null) {
            mDataAdapter.release();
            mDataAdapter = null;
        }
        if (recyclerView != null) {
            recyclerView = null;
        }
        removeAllViews();
    }

    private void initView(Context context) {
        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        if (isHorizontal) {
            params.gravity = Gravity.CENTER_VERTICAL;
        } else {
            params.gravity = Gravity.CENTER_HORIZONTAL;
        }
        setLayoutParams(params);

        recyclerView = new RecyclerView(context);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(isHorizontal ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        recyclerView.setLayoutParams(params);
        addView(recyclerView);
    }

    private void initData(Context context) {
        mDataAdapter = new UtcSelectDateAdapter(context, isHorizontal);
        recyclerView.setAdapter(mDataAdapter);
    }
}
