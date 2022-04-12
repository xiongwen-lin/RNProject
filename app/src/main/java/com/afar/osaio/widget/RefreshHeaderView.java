package com.afar.osaio.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.afar.osaio.R;
import com.aspsine.swipetoloadlayout.SwipeRefreshHeaderLayout;

/**
 * Created by victor on 2018/7/2
 * Email is victor.qiao.0604@gmail.com
 */
public class RefreshHeaderView extends SwipeRefreshHeaderLayout {

    private ImageView ivArrow;

    private int mHeaderHeight;

    private Animation rotateUp;

    private Animation rotateDown;
    private Animation rotateLoop;

    private boolean rotated = false;

    public RefreshHeaderView(Context context) {
        this(context, null);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHeaderHeight = getResources().getDimensionPixelOffset(R.dimen.refresh_header_height_twitter);
        rotateUp = AnimationUtils.loadAnimation(context, R.anim.rotate_up);
        rotateDown = AnimationUtils.loadAnimation(context, R.anim.rotate_down);
        rotateLoop = AnimationUtils.loadAnimation(context, R.anim.rotate_loop);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ivArrow = findViewById(R.id.ivArrow);
    }

    @Override
    public void onRefresh() {
        ivArrow.setVisibility(VISIBLE);
        ivArrow.clearAnimation();
        ivArrow.startAnimation(rotateLoop);
    }

    @Override
    public void onPrepare() {
        Log.d("RefreshHeader", "onPrepare()");
    }

    @Override
    public void onMove(int y, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            ivArrow.setVisibility(VISIBLE);
            if (y > mHeaderHeight) {
                if (!rotated) {
                    ivArrow.clearAnimation();
                    ivArrow.startAnimation(rotateUp);
                    rotated = true;
                }
            } else if (y < mHeaderHeight) {
                if (rotated) {
                    ivArrow.clearAnimation();
                    ivArrow.startAnimation(rotateDown);
                    rotated = false;
                }
            }
        }
    }

    @Override
    public void onRelease() {
        Log.d("RefreshHeader", "onRelease()");
    }

    @Override
    public void onComplete() {
        rotated = false;
        ivArrow.clearAnimation();
        //ivArrow.setVisibility(GONE);
    }

    @Override
    public void onReset() {
        rotated = false;
        ivArrow.clearAnimation();
        //ivArrow.setVisibility(GONE);
    }
}
