package com.afar.osaio.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afar.osaio.R;
import com.nooie.common.utils.log.NooieLog;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class IconAnimView extends LinearLayout {

    @BindView(R.id.ivIconAnim)
    ImageView ivIconAnim;

    public IconAnimView(Context context) {
        super(context);
        init();
    }

    public IconAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        View lvpView = LayoutInflater.from(getContext()).inflate(R.layout.layout_icon_anim, this, false);
        addView(lvpView);
        bindView(lvpView);
        setupView(R.drawable.talk_level_list);
    }

    public void bindView(View view) {
        ButterKnife.bind(this, view);
    }

    public void setupView(int levelRes) {
        ivIconAnim.setImageResource(levelRes);
        ivIconAnim.setImageLevel(1);
    }

    public final static int MAX_ICON_INDEX = 40;
    public final static int PER_FRAME_LEN = 24;
    public void updateIconAnim(boolean isRun, int index) {
        if (isRun) {
            if (index <  MAX_ICON_INDEX) {
                NooieLog.d("-->> IconAnimView updateIconAnim level=" + index * PER_FRAME_LEN);
                ivIconAnim.setImageLevel(index * PER_FRAME_LEN);
                ivIconAnim.requestLayout();
            } else {
                //ivIconAnim.setImageLevel(index);
            }
        } else {
            if (index > 0) {
                ivIconAnim.setImageLevel(index * PER_FRAME_LEN);
                ivIconAnim.requestLayout();
            } else {
            }
        }
    }

    public void setIvIconOnOrOff(boolean on) {
        if (on) {
            ivIconAnim.setImageLevel(1000);
        } else {
            ivIconAnim.setImageLevel(1);
        }
    }

    public void setIvIconEnable(boolean enable) {
        ivIconAnim.setEnabled(enable);
        if (enable) {
            ivIconAnim.setImageLevel(1);
        } else {
            ivIconAnim.setImageLevel(1150);
        }
    }

    public void resetIvIcon() {
        cancelStartIconAnim();
        cancelEndIconAnim();
        ivIconAnim.setImageLevel(1);
    }

    private Subscription mStartIconAnimTask;
    private int mCountIndex = 0;
    public void runStartIconAnim() {
        cancelEndIconAnim();
        cancelStartIconAnim();
        mCountIndex = 0;
        ivIconAnim.setImageLevel(1);
        mStartIconAnimTask = Observable.just(1000)
                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {
                        return observable.delay(PER_FRAME_LEN, TimeUnit.MILLISECONDS);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        NooieLog.d("-->> IconAnimView runStartIconAnim onNext time=" + mCountIndex);
                        if (mCountIndex < MAX_ICON_INDEX) {
                            updateIconAnim(true, mCountIndex);
                            mCountIndex++;
                        } else {
                            cancelStartIconAnim();
                        }
                    }
                });
    }

    public void cancelStartIconAnim() {
        if (mStartIconAnimTask != null && !mStartIconAnimTask.isUnsubscribed()) {
            mStartIconAnimTask.unsubscribe();
        }
    }

    private Subscription mEndIconAnimTask;
    public void runEndIconAnim() {
        cancelStartIconAnim();
        cancelEndIconAnim();
        mCountIndex = MAX_ICON_INDEX - 1;
        ivIconAnim.setImageLevel(1000);
        mEndIconAnimTask = Observable.just(1000)
                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {
                        return observable.delay(PER_FRAME_LEN, TimeUnit.MILLISECONDS);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        NooieLog.d("-->> IconAnimView runStartIconAnim onNext time=" + mCountIndex);
                        if (mCountIndex > 0) {
                            updateIconAnim(false, mCountIndex);
                            mCountIndex--;
                        } else {
                            cancelEndIconAnim();
                        }
                    }
                });
    }

    public void cancelEndIconAnim() {
        if (mEndIconAnimTask != null && !mEndIconAnimTask.isUnsubscribed()) {
            mEndIconAnimTask.unsubscribe();
        }
    }

    public void release() {
        cancelStartIconAnim();
        cancelEndIconAnim();
        ivIconAnim = null;
        removeAllViews();
    }
}
