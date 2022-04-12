package com.afar.osaio.widget.helper;

import androidx.recyclerview.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerMarginClickHelper {

    public void setOnMarginClickListener(final RecyclerView recyclerView, final View.OnClickListener onClickListener){
        if(recyclerView == null || onClickListener == null){
            return;
        }

        final GestureDetector gestureDetector = new GestureDetector(recyclerView.getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if(onClickListener != null){
                    onClickListener.onClick(recyclerView);
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });;

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //发现只有点击了空白处，v是自身recyclerView
                if (view instanceof RecyclerView && gestureDetector != null){
                    return  gestureDetector.onTouchEvent(motionEvent);
                }
                return false;
            }
        });
    }
}
