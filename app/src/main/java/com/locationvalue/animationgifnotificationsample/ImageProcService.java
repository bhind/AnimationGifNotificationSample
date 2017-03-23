package com.locationvalue.animationgifnotificationsample;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by aoshika on 2017/03/23.
 */

public class ImageProcService extends IntentService {
    static final private int DEFAULT_LOOP_PERIOD = 10000;
    static final private int DEFAULT_LOOP_INTERVAL = 100;
    public ImageProcService(String name) {
        super(name);
    }
    public ImageProcService() {
        super("ImageProcService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final long now = System.currentTimeMillis();
        long endtime = now + DEFAULT_LOOP_PERIOD;
        long interval = DEFAULT_LOOP_INTERVAL;
        final Bundle extras = intent.getExtras();
        final Integer _loop_period_buf = (Integer)extras.get("loop_period");
        if(_loop_period_buf != null) {
            endtime = now + _loop_period_buf;
        }
        final Integer _loop_interval_buf = (Integer)extras.get("loop_interval");
        if(_loop_interval_buf != null) {
            interval = _loop_interval_buf;
        }

        while(System.currentTimeMillis() < endtime) {
            try {
                Thread.sleep(interval);

                final Intent broadcastintent = new Intent();
                broadcastintent.setAction(MainActivity.IMAGE_PROC_ACTION);
                this.getBaseContext().sendBroadcast(broadcastintent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
