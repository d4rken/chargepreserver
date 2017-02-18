package eu.thedarken.cp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class CPService extends Service {
    private boolean created;
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    private BroadcastReceiver batReceiver;
    private PowerManager.WakeLock wl;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        created = true;
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = settings.edit();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "brightlock");
        Log.d(getClass().getSimpleName(), "Charge Preserver service running");
        Toast.makeText(this.getApplicationContext(), "Service up and running", Toast.LENGTH_SHORT).show();

        batReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                Log.d(getClass().getSimpleName(), "New battery intent");
                Integer source = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                if (source == BatteryManager.BATTERY_PLUGGED_AC || source == BatteryManager.BATTERY_PLUGGED_USB) {
                    Log.d(getClass().getSimpleName(), "On AC or USB");
                    boolean isEnabled = Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
                    if (isEnabled) {
                        Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
                        Log.d(getClass().getSimpleName(), "Turning airplanemode off");
                        Intent reload = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                        reload.putExtra("state", false);
                        sendBroadcast(reload);
                        if (settings.getBoolean("ScreenOn", false) && !wl.isHeld()) {
                            wl.acquire();
                            Log.d(getClass().getSimpleName(), "Screen on WL aquired");
                        }
                    }
                } else {
                    Log.d(getClass().getSimpleName(), "On battery");
                    boolean isEnabled = Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
                    if (!isEnabled) {
                        Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
                        Log.d(getClass().getSimpleName(), "Turning airplanemode on");
                        Intent reload = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                        reload.putExtra("state", true);
                        sendBroadcast(reload);
                        if (wl.isHeld()) {
                            wl.release();
                            Log.d(getClass().getSimpleName(), "Screen on WL released");
                        }
                    }
                }
            }
        };

        this.registerReceiver(this.batReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }


    @Override
    public void onDestroy() {
        if (created) {
            this.unregisterReceiver(batReceiver);
            Log.d(getClass().getSimpleName(), "Receiver unregistered");
            if (wl.isHeld()) {
                wl.release();
                Log.d(getClass().getSimpleName(), "WL released");
            }
        }

        boolean isEnabled = Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
        if (isEnabled) {
            Log.d(getClass().getSimpleName(), "Turning airplanemode off");
            Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
            Intent reload = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            reload.putExtra("state", false);
            sendBroadcast(reload);
        }

        Log.d(getClass().getSimpleName(), "Charge Preserver service stopped");
        Toast.makeText(this.getApplicationContext(), "Service has been shutdown", Toast.LENGTH_SHORT).show();
        prefEditor.putBoolean("isService", false);
        prefEditor.commit();
    }


}