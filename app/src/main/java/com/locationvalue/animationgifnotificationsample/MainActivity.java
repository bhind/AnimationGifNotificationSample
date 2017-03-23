package com.locationvalue.animationgifnotificationsample;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IRedrawable {

    public static final String IMAGE_PROC_ACTION = "com.locationvalue.animationgifnotificationsample.IMAGE_PROC_ACTION";
    public static final String IMAGE_FINISH_ACTION = "com.locationvalue.animationgifnotificationsample.IMAGE_FINISH_ACTION";
    private static final int TIME_PERIOD = 30000;
    private static final int TIME_DELAY = 100;
    private static final int NOTIFICATION_ID = 0;

    private Context mContext;
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private NotificationCompat.Builder mBuilder;
    private GifDecoder mDecoder;
    private RemoteViews mRemoteViews;
    private NotificationManagerCompat mManager;
    private Bitmap mCanvas;
    private int mFrameCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        this.bindViews();
    }
    private void bindViews() {
        findViewById(R.id.button_notification).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_notification:
                this.sendNotification();
                break;
        }
    }
    private void sendNotification() {
        try {
            this.mDecoder = new GifDecoder();
            this.mDecoder.read(this.mContext.getResources().openRawResource(R.raw.nyancat));
        } catch (Throwable e) {
            // TODO
            Log.d("coco", e.toString());
            return;
        }
        this.mBuilder = new NotificationCompat.Builder(mContext);
        this.mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        this.redrawImage();
        this.setRedrawSchedule();
    }

    /**
     * IRedrawable
     */
    synchronized public void redrawImage() {
        this.mCanvas = this.mDecoder.getFrame(this.mFrameCount);

        this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.activity_notification);
        this.mRemoteViews.setImageViewBitmap(R.id.gifView, this.mCanvas);
        Notification notification = this.mBuilder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.bigContentView = this.mRemoteViews;
        }

        this.mManager = NotificationManagerCompat.from(mContext.getApplicationContext());

        this.mManager.notify(NOTIFICATION_ID, notification);
        this.mFrameCount = ++this.mFrameCount % this.mDecoder.getFrameCount();

    }
    private void setRedrawSchedule() {
        Intent intent = new Intent(this, ImageProcService.class);
        this.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final IRedrawable redrawable = (IRedrawable) mContext;
                switch(intent.getAction()) {
                    case MainActivity.IMAGE_PROC_ACTION:
                        redrawable.redrawImage();
                        break;
                    case MainActivity.IMAGE_FINISH_ACTION:
                        redrawable.finish();

                }
            }
        };
        this.mFilter = new IntentFilter();
        this.mFilter.addAction(IMAGE_PROC_ACTION);
        this.mFilter.addAction(IMAGE_FINISH_ACTION);
        this.registerReceiver(this.mReceiver, this.mFilter);
        intent.putExtra("loop_period", TIME_PERIOD);
        intent.putExtra("loop_interval", TIME_DELAY);
        this.startService(intent);
    }

    /**
     * IRedrawable
     */
    public void finish() {
        this.unregisterReceiver();
    }
    synchronized private void unregisterReceiver() {
        if(this.mReceiver != null) {
            this.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }

    }
}
