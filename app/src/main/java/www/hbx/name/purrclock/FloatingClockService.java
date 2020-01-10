package www.hbx.name.purrclock;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class FloatingClockService extends Service {


    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View displayView;

    private Handler updateTimeHandler;

    private HashMap<String, Boolean> patterns;

    public FloatingClockService() {
        patterns = new HashMap<>();
        patterns.put("year", true);
        patterns.put("hrs", true);
        patterns.put("sec", true);
        patterns.put("btr", true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        isStarted = false;

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }


        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        layoutParams.width = 500;
        layoutParams.height = 80;
        layoutParams.x = 300;
        layoutParams.y = 300;

        updateTimeHandler = new Handler(this.getMainLooper(), updateTimeCallback);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String action = (String) extras.get("action");
            if (action != "" && action != null) {
                switch (action) {
                    case "show":
                        startFloatingWindow();
                        break;
                    case "hide":
                        stopFloatingWindow();
                        break;
                    default:
                        triggerTimePattern(action);
                }
            }
        }


        return super.onStartCommand(intent, flags, startId);
    }

    private void startFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            isStarted = true;

            LayoutInflater layoutInflater = LayoutInflater.from(this);
            displayView = layoutInflater.inflate(R.layout.clock_display, null);

            updateCurrentTime();

            displayView.setOnTouchListener(new FloatingOnTouchListener());

            windowManager.addView(displayView, layoutParams);

            updateTimeHandler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    private void stopFloatingWindow() {
        if (displayView != null) windowManager.removeView(displayView);
        isStarted = false;
    }

    private void triggerTimePattern(String type) {
        patterns.put(type, !patterns.get(type));
    }

    private int getBatteryLevel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = new ContextWrapper(getApplicationContext()).
                    registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            return (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (displayView != null) windowManager.removeView(displayView);
    }

    private void updateCurrentTime() {
        TextView textYear = displayView.findViewById(R.id.text_year);
        TextView textDate = displayView.findViewById(R.id.text_date);
        TextView textHrs = displayView.findViewById(R.id.text_hrs);
        TextView textSec = displayView.findViewById(R.id.text_sec);
        TextView textBattery = displayView.findViewById(R.id.text_battery);

        if (patterns.get("year")) {
            textYear.setText((new SimpleDateFormat("yyyy-")).format(new Date()));
        } else {
            textYear.setText("");
        }

        textDate.setText((new SimpleDateFormat("MM-dd")).format(new Date()));

        if (patterns.get("hrs")) {
            textHrs.setText((new SimpleDateFormat(" HH:mm")).format(new Date()));
        } else {
            textHrs.setText((new SimpleDateFormat(" a hh:mm" , Locale.ENGLISH)).format(new Date()));
        }

        if (patterns.get("sec")) {
            textSec.setText((new SimpleDateFormat(":ss")).format(new Date()));
        } else {
            textSec.setText("");
        }

        if (patterns.get("btr")) {
            textBattery.setText(" " + getBatteryLevel() + "%");
        } else {
            textBattery.setText("");
        }

    }


    private Handler.Callback updateTimeCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {

                if ( displayView != null) {
                    updateCurrentTime();
                }

                updateTimeHandler.sendEmptyMessageDelayed(0, 1000);

            }
            return false;
        }
    };

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;


        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) motionEvent.getRawX();
                    y = (int) motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) motionEvent.getRawX();
                    int nowY = (int) motionEvent.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }

            return false;
        }
    }
}
