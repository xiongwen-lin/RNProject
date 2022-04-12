package com.afar.osaio.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.afar.osaio.R;
import com.aspsine.swipetoloadlayout.SwipeLoadMoreFooterLayout;

/**
 * Created by victor on 2018/7/2
 * Email is victor.qiao.0604@gmail.com
 */
public class LoadMoreFooterView extends SwipeLoadMoreFooterLayout {
    private ImageView ivArrow;

    private int mFooterHeight;

    private Animation rotateUp;

    private Animation rotateDown;

    private boolean rotated = false;

    public LoadMoreFooterView(Context context) {
        this(context, null);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFooterHeight = getResources().getDimensionPixelOffset(R.dimen.load_more_footer_height_classic);
        rotateUp = AnimationUtils.loadAnimation(context, R.anim.rotate_up);
        rotateDown = AnimationUtils.loadAnimation(context, R.anim.rotate_down);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ivArrow = findViewById(R.id.ivArrow);
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onMove(int y, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            ivArrow.setVisibility(VISIBLE);
            if (-y >= mFooterHeight) {
                if (!rotated) {
                    ivArrow.clearAnimation();
                    ivArrow.startAnimation(rotateUp);
                    rotated = true;
                }
            } else {
                if (rotated) {
                    ivArrow.clearAnimation();
                    ivArrow.startAnimation(rotateDown);
                    rotated = false;
                }
            }
        }
    }

    @Override
    public void onLoadMore() {
    }

    @Override
    public void onRelease() {

    }

    @Override
    public void onComplete() {
        rotated = false;
        ivArrow.clearAnimation();
        ivArrow.setVisibility(GONE);
    }

    @Override
    public void onReset() {
        rotated = false;
        ivArrow.clearAnimation();
        ivArrow.setVisibility(GONE);
    }
}
