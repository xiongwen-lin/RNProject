package com.afar.osaio.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.afar.osaio.smart.player.delegate.IPlayerDelegate;
import com.afar.osaio.smart.player.delegate.PlayState;
import com.nooie.common.utils.time.DateTimeUtil;
import com.afar.osaio.R;
import com.nooie.common.utils.tool.TaskUtil;
import com.afar.osaio.widget.adapter.UtcDateAdapter;
import com.afar.osaio.widget.listener.OnRecyclerItemClickListener;

import java.util.Calendar;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by victor on 2018/7/13
 * Email is victor.qiao.0604@gmail.com
 */
public class UtcDateSelectView extends LinearLayout {
    private RecyclerView recyclerView;
    private boolean isHorizontal;

    private UtcDateAdapter mDataAdapter;

    public UtcDateSelectView(Context context) {
        this(context, null);
    }

    public UtcDateSelectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UtcDateSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

    private void initData(Context context) {
        mDataAdapter = new UtcDateAdapter(context, isHorizontal);
        recyclerView.setAdapter(mDataAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(isHorizontal ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void initView(Context context) {
        int width = getResources().getDimensionPixelSize(R.dimen.date_select_height);
        //int itemSize = getResources().getDimensionPixelSize(R.dimen.item_date_indicator_width);
        //int padding = DisplayUtil.dip2px(context, 3);

        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        if (isHorizontal) {
            //params.height = width;
            params.gravity = Gravity.CENTER_VERTICAL;
        } else {
            //params.width = (int) (width /** 1.5*/);
            params.gravity = Gravity.CENTER_HORIZONTAL;
        }
        setLayoutParams(params);
        //setBackgroundColor(ContextCompat.getColor(context, R.color.blue));

        recyclerView = new RecyclerView(context);
        //recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
        params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        recyclerView.setLayoutParams(params);
        addView(recyclerView);
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
        mDataAdapter.notifyDataSetChanged();
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener listener) {
        mDataAdapter.setOnRecyclerItemClickListener(listener);
    }

    public void setOnStartScroolListener(RecyclerView.OnScrollListener listener) {
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

    public void scrollToLive() {
        if (mDataAdapter == null || recyclerView == null) {
            return;
        }
        int current = mDataAdapter.getItemCount() - 1;
        if (current >= 0 && current < mDataAdapter.getItemCount()) {
            recyclerView.smoothScrollToPosition(current);
        }
    }

    public void setCurrentItem(boolean isLive, long time) {
        Calendar calendar = DateTimeUtil.getUtcCalendar();
        calendar.setTimeInMillis(time);
        mDataAdapter.setSelectDate(isLive, calendar);
    }

    public void setTextColor(int textColor) {
        if (mDataAdapter != null) {
            mDataAdapter.setTextColor(textColor);
        }
    }

    public void updateCurrentState(Calendar calender, boolean isLive, long currentSeekDay) {
        if (mDataAdapter != null) {
            mDataAdapter.updateCurrentState(calender, isLive, currentSeekDay);
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
}
