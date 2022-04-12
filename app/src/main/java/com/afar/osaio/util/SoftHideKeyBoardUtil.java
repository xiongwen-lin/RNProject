package com.afar.osaio.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.nooie.common.utils.log.NooieLog;

public class SoftHideKeyBoardUtil {
    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    /*
    public static void assistActivity (Activity activity) {
        new SoftHideKeyBoardUtil(activity);
    }
     */

    private static SoftHideKeyBoardUtil INSTANCE = new SoftHideKeyBoardUtil();

    public static SoftHideKeyBoardUtil getInstance() {
        return INSTANCE;
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;
    private SoftHideKeyBoardListener mListener;
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener;

    private SoftHideKeyBoardUtil() {
    }

    /*
    private SoftHideKeyBoardUtil(Activity activity) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }
     */

    public void register(Activity activity, SoftHideKeyBoardListener listener) {
        NooieLog.d("-->> debug SoftHideKeyBoardUtil register");
        try {
            if (mListener != null) {
                unRegister();
                return;
            }
            mListener = listener;
            FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    possiblyResizeChildOfContent();
                }
            };
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
            frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
        } catch (Exception e) {
        }
    }

    public void unRegister() {
        NooieLog.d("-->> debug SoftHideKeyBoardUtil unRegister");
        try {
            if (mOnGlobalLayoutListener != null && mChildOfContent != null && mChildOfContent.getViewTreeObserver() != null) {
                mChildOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
                mOnGlobalLayoutListener = null;
            }
            mChildOfContent = null;
            frameLayoutParams = null;
            mListener = null;
        } catch (Exception e) {
        }
    }

    private void possiblyResizeChildOfContent() {
        try {
            int usableHeightNow = computeUsableHeight();
            //NooieLog.d("-->> debug SoftHideKeyBoardUtil usableHeightNow=" + usableHeightNow + " ush=" + mChildOfContent.getRootView().getHeight());
            if (usableHeightNow != usableHeightPrevious) {
                int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
                int heightDifference = usableHeightSansKeyboard - usableHeightNow;
                if (heightDifference > (usableHeightSansKeyboard/4)) {
                    // keyboard probably just became visible
                    //frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                } else {
                    // keyboard probably just became hidden
                    //frameLayoutParams.height = usableHeightSansKeyboard;
                }
                NooieLog.d("-->> debug SoftHideKeyBoardUtil visible=" + (heightDifference > (usableHeightSansKeyboard/4)));
                if (mListener != null) {
                    mListener.onKeyBoardChange((heightDifference > (usableHeightSansKeyboard/4)));
                }
                //mChildOfContent.requestLayout();
                usableHeightPrevious = usableHeightNow;
            }
        } catch (Exception e) {
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);// 全屏模式下： return r.bottom
    }

    public interface SoftHideKeyBoardListener {

        void onKeyBoardChange(boolean isVisible);
    }
}