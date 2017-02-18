package eu.thedarken.cp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class CPMain extends Activity {
    private Intent svc;
    private CheckBox cbService, cbScreenOn;
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    private boolean isService;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        svc = new Intent(this, CPService.class);
        Log.d(getClass().getSimpleName(), "Charge Preserver launched");

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = settings.edit();
        isService = settings.getBoolean("isService", false);

        cbService = (CheckBox) findViewById(R.id.cb_Service);
        cbScreenOn = (CheckBox) findViewById(R.id.cb_ScreenOn);

        if (isService) {
            cbService.setBackgroundColor(Color.GREEN);
            cbService.setTextColor(Color.BLACK);
            cbService.setChecked(true);
            cbScreenOn.setEnabled(false);
        } else {
            cbService.setBackgroundColor(Color.RED);
            cbService.setTextColor(Color.WHITE);
            cbService.setChecked(false);
            cbScreenOn.setEnabled(true);
        }

        cbService.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {

                    prefEditor.putBoolean("isService", true);
                    prefEditor.commit();
                    cbService.setBackgroundColor(Color.GREEN);
                    cbService.setTextColor(Color.BLACK);
                    startService(svc);
                    cbScreenOn.setEnabled(false);
                } else {
                    prefEditor.putBoolean("isService", false);
                    prefEditor.commit();
                    cbService.setBackgroundColor(Color.RED);
                    cbService.setTextColor(Color.WHITE);
                    stopService(svc);
                    cbScreenOn.setEnabled(true);
                }
            }
        });
        cbScreenOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    prefEditor.putBoolean("ScreenOn", true);
                    prefEditor.commit();
                } else {
                    prefEditor.putBoolean("ScreenOn", false);
                    prefEditor.commit();
                }
            }
        });
    }
}