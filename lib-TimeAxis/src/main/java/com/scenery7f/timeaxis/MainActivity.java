package com.scenery7f.timeaxis;

import android.app.Activity;
import android.os.Bundle;

import com.scenery7f.timeaxis.view.TimerShaft;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimerShaft timerShaft = (TimerShaft) findViewById(R.id.timer_shaft);
        /*timerShaft.setOnTimeShaftListener(new TimerShaft.OnTimeShaftListener() {
            @Override
            public void timeChangeOver(String timeStr, long time) {
                TestUtil.showToast(MainActivity.this, timeStr);
            }

            @Override
            public void timeChangeAction() {
                TestUtil.log(MainActivity.this.getClass(), "正在移动时间轴");
            }
        });*/


        /*LinearLayout back = (LinearLayout) findViewById(R.id.activity_main);
        TimerShaft s = new TimerShaft(this, Color.YELLOW, Color.RED);
        back.addView(s);

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(start.getTimeInMillis() + 1000 * 60 * 60);

        PeriodTime periodTime = new PeriodTime(start, end);
        List<PeriodTime> list = new ArrayList<>();
        list.add(periodTime);
        timerShaft.setRecordList(list);
        s.setRecordList(list);*/
    }


}
