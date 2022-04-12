package com.afar.osaio.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afar.osaio.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LiveVideoPanelView extends LinearLayout {

    @BindView(R.id.playerGuideContainer)
    RelativeLayout playerGuideContainer;
    @BindView(R.id.ivPlayerGuideControlPanel)
    ImageView ivPlayerGuideControlPanel;
    @BindView(R.id.playerGuideIconCenter)
    ImageView playerGuideIconCenter;
    @BindView(R.id.playerGuideIconLeft)
    ImageView playerGuideIconLeft;
    @BindView(R.id.playerGuideIconRight)
    ImageView playerGuideIconRight;
    @BindView(R.id.playerGuideIconTop)
    ImageView playerGuideIconTop;
    @BindView(R.id.playerGuideIconBottom)
    ImageView playerGuideIconBottom;
    @BindView(R.id.tvPlayerGuideTip)
    TextView tvPlayerGuideTip;

    private LiveVideoPanelListener mListener;

    public LiveVideoPanelView(Context context) {
        super(context);
        init();
    }

    public LiveVideoPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        View lvpView = LayoutInflater.from(getContext()).inflate(R.layout.layout_live_video_panel, this, false);
        addView(lvpView);
        bindView(lvpView);
    }

    public void bindView(View view) {
        ButterKnife.bind(this, view);
    }

    public void setupView() {

        playerGuideIconLeft.setTag(0);
        playerGuideIconRight.setTag(0);
        playerGuideIconTop.setTag(0);
        playerGuideIconBottom.setTag(0);
        int cTop = playerGuideIconCenter.getTop();
        int cBottom = playerGuideIconCenter.getBottom();
        int cLeft = playerGuideIconCenter.getLeft();
        int cRight = playerGuideIconCenter.getRight();
        int moveLen = (int)getResources().getDimension(R.dimen.dp_5);
        final Animation transLeft = new TranslateAnimation(cLeft, cLeft - getResources().getDimension(R.dimen.dp_5), 0, 0);
        transLeft.setDuration(600);
        final Animation transRight = new TranslateAnimation(cLeft, cLeft + getResources().getDimension(R.dimen.dp_5), 0, 0);
        transRight.setDuration(600);
        final Animation transTop = new TranslateAnimation(0, 0, cTop, cTop - getResources().getDimension(R.dimen.dp_5));
        transTop.setDuration(600);
        final Animation transBottom = new TranslateAnimation(0, 0, cTop, cTop + moveLen);
        transBottom.setDuration(600);

        ValueAnimator valueAnimator = ValueAnimator.ofInt(0,4);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(3*1000);
        valueAnimator.setRepeatCount(0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int start = (int)animation.getAnimatedValue();
                if (start == 0 && (int)playerGuideIconLeft.getTag() == 0) {
                    playerGuideIconLeft.setTag(1);
                    playerGuideIconCenter.startAnimation(transLeft);
                    playerGuideIconLeft.setImageResource(R.drawable.nooie360_left);
                } else if (start == 1 && (int)playerGuideIconRight.getTag() == 0) {
                    playerGuideIconRight.setTag(1);
                    playerGuideIconCenter.startAnimation(transRight);
                    playerGuideIconRight.setImageResource(R.drawable.nooie360_right);
                } else if (start == 2 && (int)playerGuideIconTop.getTag() == 0) {
                    playerGuideIconTop.setTag(1);
                    playerGuideIconCenter.startAnimation(transTop);
                    playerGuideIconTop.setImageResource(R.drawable.nooie360_on);
                } else if (start == 3 && (int)playerGuideIconBottom.getTag() == 0) {
                    playerGuideIconBottom.setTag(1);
                    playerGuideIconCenter.startAnimation(transBottom);
                    playerGuideIconBottom.setImageResource(R.drawable.nooie360_down);
                } else if (start == 4) {
                }
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                /*
                if ((int)playerGuideIconLeft.getTag() == 0) {
                    playerGuideIconLeft.setTag(1);
                    playerGuideIconLeft.setImageResource(R.drawable.add_cam_2);
                } else if ((int)playerGuideIconRight.getTag() == 0) {
                    playerGuideIconRight.setTag(1);
                    playerGuideIconRight.setImageResource(R.drawable.add_cam_2);
                } else if ((int)playerGuideIconTop.getTag() == 0) {
                    playerGuideIconTop.setTag(1);
                    playerGuideIconTop.setImageResource(R.drawable.add_cam_2);
                } else if ((int)playerGuideIconBottom.getTag() == 0) {
                    playerGuideIconBottom.setTag(1);
                    playerGuideIconBottom.setImageResource(R.drawable.add_cam_2);
                }
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            mListener.onVideoPanelClick();
                        }
                        hidePanel();
                    }
                }, 500);
                */
            }
        });
        valueAnimator.start();
    }

    public void hidePanel() {
        if (playerGuideIconCenter.getVisibility() != View.GONE) {
            ivPlayerGuideControlPanel.setVisibility(View.GONE);
            playerGuideIconCenter.setVisibility(View.GONE);
            playerGuideIconTop.setVisibility(View.GONE);
            playerGuideIconBottom.setVisibility(View.GONE);
            playerGuideIconLeft.setVisibility(View.GONE);
            playerGuideIconRight.setVisibility(View.GONE);
            tvPlayerGuideTip.setVisibility(View.GONE);
        }
    }

    public void setListener(LiveVideoPanelListener listener) {
        mListener = listener;
    }

    public interface LiveVideoPanelListener {
        void onVideoPanelClick();
    }
}
