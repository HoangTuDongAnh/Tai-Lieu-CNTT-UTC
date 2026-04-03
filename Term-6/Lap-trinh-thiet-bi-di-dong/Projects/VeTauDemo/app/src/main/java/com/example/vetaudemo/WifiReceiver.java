package com.example.vetaudemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int state = intent.getIntExtra(
                    WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN
            );

            if (state == WifiManager.WIFI_STATE_ENABLED) {
                Toast.makeText(context, "WiFi đã được bật", Toast.LENGTH_SHORT).show();
            } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                Toast.makeText(context, "WiFi đã được tắt", Toast.LENGTH_SHORT).show();
            }
        }
    }
}