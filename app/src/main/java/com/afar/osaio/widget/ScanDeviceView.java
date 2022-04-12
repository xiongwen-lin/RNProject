package com.afar.osaio.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.afar.osaio.R;
import com.afar.osaio.widget.base.BaseScanCameraView;
import com.afar.osaio.widget.listener.BaseScanCameraListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class ScanDeviceView extends BaseScanCameraView {

    public static final String TAG = "ScanDeviceView";

    public enum STATE {
        UNKNOWN(-1),
        PREPARE(0),
        RUNNING(1),
        PAUSE(2),
        STOP(3),
        CANCEL(4),
        RESTART(5);

        public int state;
        private STATE(int state) {
            this.state = state;
        }

        public STATE getState(int state) {
            if (state == PREPARE.state) {
                return PREPARE;
            } else if (state == RUNNING.state) {
                return RUNNING;
            } else if (state == PAUSE.state) {
                return PAUSE;
            } else if (state == STOP.state) {
                return STOP;
            } else if (state == CANCEL.state) {
                return CANCEL;
            } else if (state == CANCEL.state) {
                return RESTART;
            }

            return UNKNOWN;
        }
    }

    private static final String KEY_REPEAT = "repeat";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_FAILED = "failed";
    private static final String KEY_RESTART = "restart";

    @BindView(R.id.ivPointOne)
    ImageView ivPointOne;
    @BindView(R.id.ivPointTwo)
    ImageView ivPointTwo;
    @BindView(R.id.ivPointThree)
    ImageView ivPointThree;

    private static int POINT_DURATION = 2000;
    private int maxCount = calculateRepeatCount(150, 10);
    private int mPointOneCount = 1;
    private int mPointTwoCount = 1;
    private int mPointThreeCount = 1;
    private boolean isFirst = true;
    private boolean mScanSuccess = false;
    private STATE mState = STATE.PREPARE;

    private int fcWidth;
    private int fcHeight;
    private int pointOneTop;
    private int pointOneW;
    private int pointOneH;
    private int pointTwoTop;
    private int pointTwoW;
    private int pointTwoH;
    private int pointThreeTop;
    private int pointThreeW;
    private int pointThreeH;

    private BaseScanCameraListener mListener;
    private Map<String,STATE> mStateMap = new HashMap<>();
    private List<Animator> oneAnimators = new ArrayList<>();
    private List<Animator> twoAnimators = new ArrayList<>();
    private List<Animator> threeAnimators = new ArrayList<>();
    private AnimatorSet mSet1;
    private AnimatorSet mSet2;
    private AnimatorSet mSet3;
    private List<Animator> mSuccessAnimators1 = new ArrayList<>();
    private List<Animator> mSuccessAnimators2 = new ArrayList<>();
    private List<Animator> mSuccessAnimators3 = new ArrayList<>();
    private AnimatorSet mSuccessSet1;
    private AnimatorSet mSuccessSet2;
    private AnimatorSet mSuccessSet3;
    private List<Animator> mFailedAnimators1 = new ArrayList<>();
    private List<Animator> mFailedAnimators2 = new ArrayList<>();
    private List<Animator> mFailedAnimators3 = new ArrayList<>();
    final List<Animator> mFailedLastAnimators = new ArrayList<>();
    private AnimatorSet mFailedSet1;
    private AnimatorSet mFailedSet2;
    private AnimatorSet mFailedSet3;
    private AnimatorSet mFailedLastSet;
    private List<Animator> mBufferAnimators1 = new ArrayList<>();
    private List<Animator> mBufferAnimators2 = new ArrayList<>();
    private List<Animator> mBufferAnimators3 = new ArrayList<>();

    public ScanDeviceView(Context context) {
        super(context);
        init();
    }

    public ScanDeviceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (isFirst) {
            isFirst = false;
            setupDeviceScan();
        }
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_scan_device;
    }

    public void setupDeviceScan() {
        fcWidth = getWidth();
        fcHeight = getHeight();
        pointOneTop = (int)(fcHeight * 0.24);
        pointOneW = ivPointOne.getWidth();
        pointOneH = ivPointOne.getHeight();
        pointTwoTop = (int)(fcHeight * 0.7);
        pointTwoW = ivPointTwo.getWidth();
        pointTwoH = ivPointTwo.getHeight();
        pointThreeTop = (int)(fcHeight * 0.6);
        pointThreeW = ivPointThree.getWidth();
        pointThreeH = ivPointThree.getHeight();
        //NooieLog.d("--> " + TAG + " setupDeviceScan fcW=" + fcWidth + " fcH=" + fcHeight);
        ivPointOne.setY(pointOneTop);
        ivPointTwo.setX(fcWidth - pointTwoW);
        ivPointTwo.setY(pointThreeTop);
        ivPointThree.setY(pointThreeTop);

        mStateMap.put(KEY_REPEAT, STATE.PREPARE);
        mStateMap.put(KEY_SUCCESS, STATE.PREPARE);
        mStateMap.put(KEY_FAILED, STATE.PREPARE);
        mStateMap.put(KEY_RESTART, STATE.RESTART);
        setupScanRepeat();
        setupScanSuccess();
        setupScanFailed();
        setupScanBuffer();
    }

    public void setupScanRepeat() {
        Point pointOne1 = new Point(0, pointOneTop);
        Point pointOne2 = new Point((fcWidth - pointOneW), (int)(fcHeight * 0.49));
        Point pointOne3 = new Point((int)(fcWidth * 0.48), (fcHeight - pointOneH));
        Point pointOne4 = new Point(0, (int)(fcHeight * 0.57));
        Point pointOne5 = new Point((fcWidth - pointOneW), (int)(fcHeight * 0.27));

        Animator oneToTwoSet1 = createPointAnim(ivPointOne, pointOne1, pointOne2, -1);
        Animator twoToThreeSet1 = createPointAnim(ivPointOne, pointOne2, pointOne3, -1);
        Animator threeToFourSet1 = createPointAnim(ivPointOne, pointOne3, pointOne4, -1);
        Animator fourToFineSet1 = createPointAnim(ivPointOne, pointOne4, pointOne5, -1);
        Animator fineToEndSet1 = createPointAnim(ivPointOne, pointOne5, pointOne1, -1);

        oneAnimators.add(oneToTwoSet1);
        oneAnimators.add(twoToThreeSet1);
        oneAnimators.add(threeToFourSet1);
        oneAnimators.add(fourToFineSet1);
        oneAnimators.add(fineToEndSet1);

        fineToEndSet1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mStateMap.get(KEY_REPEAT) == STATE.RUNNING) {
                    mSet1 = repeatAnimatorSet(oneAnimators);
                } else {
                }
            }
        });

        Point pointTwo1 = new Point((fcWidth - pointTwoW), (int)(fcHeight * 0.6));
        Point pointTwo2 = new Point((int)(fcWidth * 0.48), (fcHeight - pointTwoH));
        Point pointTwo3 = new Point(0, (int)(fcHeight * 0.63));
        Point pointTwo4 = new Point((fcWidth - pointTwoW), (int)(fcHeight * 0.45));
        Point pointTwo5 = new Point(0, (int)(fcHeight * 0.45));
        Animator onwToTwoSet2 = createPointAnim(ivPointTwo, pointTwo1, pointTwo2, -1);
        Animator twoToThreeSet2 = createPointAnim(ivPointTwo, pointTwo2, pointTwo3, -1);
        Animator threeToFourSet2 = createPointAnim(ivPointTwo, pointTwo3, pointTwo4, -1);
        Animator fourToFineSet2 = createPointAnim(ivPointTwo, pointTwo4, pointTwo5, -1);
        Animator fineToEndSet2 = createPointAnim(ivPointTwo, pointTwo5, pointTwo1, -1);
        twoAnimators.add(onwToTwoSet2);
        twoAnimators.add(twoToThreeSet2);
        twoAnimators.add(threeToFourSet2);
        twoAnimators.add(fourToFineSet2);
        twoAnimators.add(fineToEndSet2);

        fineToEndSet2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mStateMap.get(KEY_REPEAT) == STATE.RUNNING) {
                    mSet2 = repeatAnimatorSet(twoAnimators);
                } else {
                }
            }
        });

        Point pointThree1 = new Point(0, pointThreeTop);
        Point pointThree2 = new Point((int)(fcWidth * 0.10), (int)(fcHeight * 0.30));
        Point pointThree3 = new Point((int)(fcWidth * 0.67), (int)(fcHeight * 0.15));
        Point pointThree4 = new Point((int)(fcWidth * 0.46), (int)(fcHeight * 0.7));
        Animator oneToTwoSet3 = createPointRotateAnim(ivPointThree, pointThree1, pointThree2, 45, -1);
        Animator twoToThreeSet3 = createPointRotateAnim(ivPointThree, pointThree2, pointThree3, 135, 3000);
        Animator threeToFourSet3 = createPointRotateAnim(ivPointThree, pointThree3, pointThree4, 315, 3000);
        Animator fourToEndSet3 = createPointRotateAnim(ivPointThree, pointThree4, pointThree1, 360, -1);
        threeAnimators.add(oneToTwoSet3);
        threeAnimators.add(twoToThreeSet3);
        threeAnimators.add(threeToFourSet3);
        threeAnimators.add(fourToEndSet3);

        fourToEndSet3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mStateMap.get(KEY_REPEAT) == STATE.RUNNING) {
                    mSet3 = repeatAnimatorSet(threeAnimators);
                } else {
                    if (mStateMap.get(KEY_REPEAT) == STATE.RESTART) {
                        return;
                    }
                    if (mStateMap.get(KEY_REPEAT) == STATE.STOP) {
                        //设置ivPointThree的旋转角度为0，避免在开始时会出现旋转
                        ivPointThree.setRotation(0);
                        mSet1 = repeatAnimatorSet(mBufferAnimators1);
                        mSet2 = repeatAnimatorSet(mBufferAnimators2);
                        mSet3 = repeatAnimatorSet(mBufferAnimators3);
                    }
                }
            }
        });
    }

    public void setupScanBuffer() {
        Point pointOne1 = new Point(0, pointOneTop);
        Point pointOne2 = new Point((fcWidth - pointOneW), (int)(fcHeight * 0.49));
        Point pointOne3 = new Point((int)(fcWidth * 0.48), (fcHeight - pointOneH));
        Point pointOne4 = new Point(0, (int)(fcHeight * 0.57));
        Point pointOne5 = new Point((fcWidth - pointOneW), (int)(fcHeight * 0.27));

        Animator oneToTwoSet1 = createPointAnim(ivPointOne, pointOne1, pointOne2, -1);
        Animator twoToThreeSet1 = createPointAnim(ivPointOne, pointOne2, pointOne3, -1);
        Animator threeToFourSet1 = createPointAnim(ivPointOne, pointOne3, pointOne4, -1);
        Animator fourToFineSet1 = createPointAnim(ivPointOne, pointOne4, pointOne5, -1);

        mBufferAnimators1.add(oneToTwoSet1);
        mBufferAnimators1.add(twoToThreeSet1);
        mBufferAnimators1.add(threeToFourSet1);
        mBufferAnimators1.add(fourToFineSet1);

        fourToFineSet1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });

        Point pointTwo1 = new Point((fcWidth - pointTwoW), (int)(fcHeight * 0.6));
        Point pointTwo2 = new Point((int)(fcWidth * 0.48), (fcHeight - pointTwoH));
        Point pointTwo3 = new Point(0, (int)(fcHeight * 0.63));
        Point pointTwo4 = new Point((fcWidth - pointTwoW), (int)(fcHeight * 0.45));
        Point pointTwo5 = new Point(0, (int)(fcHeight * 0.45));
        Animator onwToTwoSet2 = createPointAnim(ivPointTwo, pointTwo1, pointTwo2, -1);
        Animator twoToThreeSet2 = createPointAnim(ivPointTwo, pointTwo2, pointTwo3, -1);
        Animator threeToFourSet2 = createPointAnim(ivPointTwo, pointTwo3, pointTwo4, -1);
        Animator fourToFineSet2 = createPointAnim(ivPointTwo, pointTwo4, pointTwo5, -1);
        mBufferAnimators2.add(onwToTwoSet2);
        mBufferAnimators2.add(twoToThreeSet2);
        mBufferAnimators2.add(threeToFourSet2);
        mBufferAnimators2.add(fourToFineSet2);

        fourToFineSet2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });

        Point pointThree1 = new Point(0, pointThreeTop);
        Point pointThree2 = new Point((int)(fcWidth * 0.10), (int)(fcHeight * 0.30));
        Point pointThree3 = new Point((int)(fcWidth * 0.67), (int)(fcHeight * 0.15));
        Point pointThree4 = new Point((int)(fcWidth * 0.46), (int)(fcHeight * 0.7));
        Animator oneToTwoSet3 = createPointRotateAnim(ivPointThree, pointThree1, pointThree2, 45, -1);
        Animator twoToThreeSet3 = createPointRotateAnim(ivPointThree, pointThree2, pointThree3, 135, 3000);
        Animator threeToFourSet3 = createPointRotateAnim(ivPointThree, pointThree3, pointThree4, 315, 3000);
        mBufferAnimators3.add(oneToTwoSet3);
        mBufferAnimators3.add(twoToThreeSet3);
        mBufferAnimators3.add(threeToFourSet3);

        threeToFourSet3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mScanSuccess) {
                    startScanSuccess();
                } else {
                    startScanFailed();
                }
            }
        });
    }

    public void setupScanSuccess() {
        Point pointOne1 = new Point((fcWidth - pointOneW), (int)(fcHeight * 0.27));
        Point pointTwo1 = new Point(0, (int)(fcHeight * 0.45));
        Point pointThree1 = new Point((int)(fcWidth * 0.46), (int)(fcHeight * 0.7));

        Point pointOne2 = new Point((fcWidth/2 - pointOneW/2), (int)((fcHeight * 0.43) - (pointOneH - pointTwoH)/2));
        Animator oneToTwoSet1 = createPointAnim(ivPointOne, pointOne1, pointOne2, -1);
        mSuccessAnimators1.add(oneToTwoSet1);

        Point pointTwo2 = new Point((int)(fcWidth/2  - pointTwoW/2), (int)(fcHeight * 0.43));
        Animator onwToTwoSet2 = createPointAnim(ivPointTwo, pointTwo1, pointTwo2, -1);
        mSuccessAnimators2.add(onwToTwoSet2);

        Point pointThree2 = new Point((int)(fcWidth/2 - pointThreeW/2), (int)((fcHeight * 0.43) - (pointOneH - pointTwoH)/2 - 39.5));
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(ivPointThree, "rotation", 315, 360);
        List<Animator> togetherAnimators = new ArrayList<>();
        togetherAnimators.add(rotateAnimator);
        Animator oneToTwoSet3 = createPointTogetherAnim(ivPointThree, pointThree1, pointThree2, togetherAnimators, -1);
        mSuccessAnimators3.add(oneToTwoSet3);

        oneToTwoSet3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mStateMap.get(KEY_SUCCESS) == STATE.RESTART) {
                    return;
                }
                mStateMap.put(KEY_SUCCESS, STATE.STOP);
                if (mListener != null) {
                    mListener.onScanSuccess();
                }
            }
        });
    }

    public void setupScanFailed() {
        Point pointOne1 = new Point((fcWidth - pointOneW), (int)(fcHeight * 0.27));
        Point pointTwo1 = new Point(0, (int)(fcHeight * 0.45));
        Point pointThree1 = new Point((int)(fcWidth * 0.46), (int)(fcHeight * 0.7));

        Point pointOne2 = new Point((int)(fcWidth * 0.55 - pointOneW/2), (int)(fcHeight/2));
        Point pointOne3 = new Point((int)(fcWidth * 0.55 - pointOneW/2), (int)(fcHeight/2 - fcHeight * 0.05));

        int upDownDuration = 1000;
        Animator oneToTwoSet1 = createPointAnim(ivPointOne, pointOne1, pointOne2, -1);
        Animator twoToThreeSet1 = createPointAnim(ivPointOne, pointOne2, pointOne3, upDownDuration);
        Animator threeToEndSet1 = createPointAnim(ivPointOne, pointOne3, pointOne2, upDownDuration);
        mFailedAnimators1.add(oneToTwoSet1);

        Point pointTwo2 = new Point((int)(fcWidth * 0.55  + pointOneW/2 + fcWidth * 0.125), (int)(fcHeight/2 + pointOneH - pointTwoH));
        Point pointTwo3 = new Point((int)(fcWidth * 0.55  + pointOneW/2 + fcWidth * 0.125), (int)(fcHeight/2 + pointOneH - pointTwoH -fcHeight * 0.05));
        Animator oneToTwoSet2 = createPointAnim(ivPointTwo, pointTwo1, pointTwo2, -1);
        Animator twoToThreeSet2 = createPointAnim(ivPointTwo, pointTwo2, pointTwo3, upDownDuration);
        Animator threeToEndSet2 = createPointAnim(ivPointTwo, pointTwo3, pointTwo2, upDownDuration);
        mFailedAnimators2.add(oneToTwoSet2);

        Point pointThree2 = new Point((int)(fcWidth * 0.55 - pointOneW/2 - fcWidth * 0.09 - pointThreeW), (int)(fcHeight/2 - pointThreeH + pointOneH));
        Point pointThree3 = new Point((int)(fcWidth * 0.55 - pointOneW/2 - fcWidth * 0.09 - pointThreeW), (int)(fcHeight/2 - pointThreeH + pointOneH - fcHeight * 0.05));
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(ivPointThree, "rotation", 315, 360);
        List<Animator> togetherAnimators = new ArrayList<>();
        togetherAnimators.add(rotateAnimator);
        Animator oneToTwoSet3 = createPointTogetherAnim(ivPointThree, pointThree1, pointThree2, togetherAnimators, -1);
        Animator twoToThreeSet3 = createPointAnim(ivPointThree, pointThree2, pointThree3,upDownDuration);
        Animator threeToEndSet3 = createPointAnim(ivPointThree, pointThree3, pointThree2,upDownDuration);
        mFailedAnimators3.add(oneToTwoSet3);

        //逐渐加速,再由快到慢
        twoToThreeSet1.setInterpolator(new AccelerateInterpolator());
        threeToEndSet1.setInterpolator(new DecelerateInterpolator());
        twoToThreeSet2.setInterpolator(new AccelerateInterpolator());
        threeToEndSet2.setInterpolator(new DecelerateInterpolator());
        twoToThreeSet3.setInterpolator(new AccelerateInterpolator());
        threeToEndSet3.setInterpolator(new DecelerateInterpolator());

        mFailedLastAnimators.add(twoToThreeSet3);
        mFailedLastAnimators.add(threeToEndSet3);
        mFailedLastAnimators.add(twoToThreeSet1);
        mFailedLastAnimators.add(threeToEndSet1);
        mFailedLastAnimators.add(twoToThreeSet2);
        mFailedLastAnimators.add(threeToEndSet2);

        oneToTwoSet3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mFailedLastSet = repeatAnimatorSet(mFailedLastAnimators);
            }
        });

        threeToEndSet2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mStateMap.get(KEY_FAILED) == STATE.RESTART) {
                    return;
                }
                mStateMap.put(KEY_FAILED, STATE.STOP);
                if (mListener != null) {
                    mListener.onScanFailed();
                }
            }
        });
    }

    @Override
    public void startScanLoop() {
        if (mStateMap.get(KEY_REPEAT) != STATE.RUNNING && oneAnimators.size() > 0 && twoAnimators.size() > 0 && threeAnimators.size() > 0) {
            mStateMap.put(KEY_REPEAT, STATE.RUNNING);
            mSet1 = repeatAnimatorSet(oneAnimators);
            mSet2 = repeatAnimatorSet(twoAnimators);
            mSet3 = repeatAnimatorSet(threeAnimators);
        }
    }

    @Override
    public void stopScanLoop() {
        if (mStateMap.get(KEY_REPEAT) == STATE.RUNNING && mSet1 != null && mSet2 != null && mSet3 != null) {
            mStateMap.put(KEY_REPEAT, STATE.RESTART);
            if (mSet1.isRunning()) {
                mSet1.end();
            }
            if (mSet2.isRunning()) {
                mSet2.end();
            }
            if (mSet3.isRunning()) {
                mSet3.end();
            }
        }

        if (mStateMap.get(KEY_FAILED) == STATE.RUNNING && mFailedSet1 != null && mFailedSet2 != null && mFailedSet3 != null  && mFailedLastSet != null) {
            mStateMap.put(KEY_FAILED, STATE.RESTART);
            if (mFailedSet1.isRunning()) {
                mFailedSet1.end();
            }

            if (mFailedSet2.isRunning()) {
                mFailedSet2.end();
            }

            if (mFailedSet3.isRunning()) {
                mFailedSet3.end();
            }

            if (mFailedLastSet.isRunning()) {
                mFailedLastSet.end();
            }
        }

        if (mStateMap.get(KEY_SUCCESS) == STATE.RUNNING && mSuccessSet1 != null && mSuccessSet2 != null && mSuccessSet3 != null) {
            mStateMap.put(KEY_SUCCESS, STATE.RESTART);
            if (mSuccessSet1.isRunning()) {
                mSuccessSet1.end();
            }

            if (mSuccessSet2.isRunning()) {
                mSuccessSet2.end();
            }

            if (mSuccessSet3.isRunning()) {
                mSuccessSet3.end();
            }
        }
    }

    @Override
    public void startScanSuccess() {
        if (mStateMap.get(KEY_SUCCESS) != STATE.RUNNING && mSuccessAnimators1.size() > 0 && mSuccessAnimators2.size() > 0 && mSuccessAnimators3.size() > 0) {
            mStateMap.put(KEY_SUCCESS, STATE.RUNNING);
            mSuccessSet1 = repeatAnimatorSet(mSuccessAnimators1);
            mSuccessSet2 = repeatAnimatorSet(mSuccessAnimators2);
            mSuccessSet3 = repeatAnimatorSet(mSuccessAnimators3);
        }
    }

    public void stopScanSuccessAnim() {
        if (mStateMap.get(KEY_SUCCESS) == STATE.RUNNING && mSuccessSet1 != null && mSuccessSet2 != null && mSuccessSet3 != null) {
            mStateMap.put(KEY_SUCCESS, STATE.STOP);
            if (mSuccessSet1.isRunning()) {
                mSuccessSet1.end();
            }
            if (mSuccessSet2.isRunning()) {
                mSuccessSet2.end();
            }
            if (mSuccessSet3.isRunning()) {
                mSuccessSet3.end();
            }
        }
    }

    @Override
    public void startScanFailed() {
        if (mStateMap.get(KEY_FAILED) != STATE.RUNNING && mFailedAnimators1.size() > 0 && mFailedAnimators2.size() > 0 && mFailedAnimators3.size() > 0) {
            mStateMap.put(KEY_FAILED, STATE.RUNNING);
            mFailedSet1 = repeatAnimatorSet(mFailedAnimators1);
            mFailedSet2 = repeatAnimatorSet(mFailedAnimators2);
            mFailedSet3 = repeatAnimatorSet(mFailedAnimators3);
        }
    }

    public void stopScanFailedAnim() {
        if (mStateMap.get(KEY_FAILED) == STATE.RUNNING && mFailedSet1 != null && mFailedSet2 != null && mFailedSet3 != null && mFailedLastSet != null) {
            mStateMap.put(KEY_FAILED, STATE.STOP);
            if (mFailedSet1.isRunning()) {
                mFailedSet1.end();
            }
            if (mFailedSet2.isRunning()) {
                mFailedSet2.end();
            }
            if (mFailedSet3.isRunning()) {
                mFailedSet3.end();
            }
            if (mFailedLastSet.isRunning()) {
                mFailedLastSet.end();
            }
        }
    }

    @Override
    public void stopScan(boolean success) {
        mScanSuccess = success;
        mStateMap.put(KEY_REPEAT, STATE.STOP);
    }

    @Override
    public void closeScan() {}

    @Override
    public void release() {
    }

    /**
     * 创建点到点移动的动画
     * @param target
     * @param start
     * @param end
     * @param duration
     * @return
     */
    public Animator createPointAnim(ImageView target, Point start, Point end, int duration) {
        return createPointRotateAnim(target, start, end, -1, duration);
    }

    /**
     * 创建点到点移动，可旋转的动画
     * @param target
     * @param start
     * @param end
     * @param degree
     * @param duration
     * @return
     */
    public Animator createPointRotateAnim(ImageView target, Point start, Point end, int degree, int duration) {
        ObjectAnimator oneToTwoAnimX = ObjectAnimator.ofFloat(target, "x", start.x, end.x);
        ObjectAnimator oneToTwoAnimY = ObjectAnimator.ofFloat(target, "y", start.y, end.y);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration != -1 ? duration : POINT_DURATION);
        animatorSet.setInterpolator(new LinearInterpolator());
        if (degree != -1) {
            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(target, "rotation", degree);
            animatorSet.playTogether(oneToTwoAnimX, oneToTwoAnimY, rotateAnim);
        } else {
            animatorSet.playTogether(oneToTwoAnimX, oneToTwoAnimY);
        }

        return animatorSet;
    }

    /**
     * 创建点到点移动的动画，并可以添加移动过程的其他动画
     * @param target
     * @param start
     * @param end
     * @param togetherAnimators
     * @param duration
     * @return
     */
    public Animator createPointTogetherAnim(ImageView target, Point start, Point end, List<Animator> togetherAnimators, int duration) {
        ObjectAnimator oneToTwoAnimX = ObjectAnimator.ofFloat(target, "x", start.x, end.x);
        ObjectAnimator oneToTwoAnimY = ObjectAnimator.ofFloat(target, "y", start.y, end.y);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration != -1 ? duration : POINT_DURATION);
        animatorSet.setInterpolator(new LinearInterpolator());
        if (togetherAnimators != null) {
            togetherAnimators.add(oneToTwoAnimX);
            togetherAnimators.add(oneToTwoAnimY);
            animatorSet.playTogether(togetherAnimators);
        } else {
            animatorSet.playTogether(oneToTwoAnimX, oneToTwoAnimY);
        }
        return animatorSet;
    }

    /**
     * 开始有序动画
     * @param animators
     * @return
     */
    public AnimatorSet repeatAnimatorSet(List<Animator> animators) {
        AnimatorSet pointOneAll = new AnimatorSet();
        pointOneAll.playSequentially(animators);
        pointOneAll.start();
        return pointOneAll;
    }

    /**
     * 根据时长和每次动画周期计算最大循环次数
     * @param totalTime
     * @param duration
     * @return
     */
    public int calculateRepeatCount(int totalTime, int duration) {
        int maxCount = (int)(totalTime / duration + 0.5);
        return maxCount;
    }

    @Override
    public void setListener(BaseScanCameraListener listener) {
        mListener = listener;
    }
}
