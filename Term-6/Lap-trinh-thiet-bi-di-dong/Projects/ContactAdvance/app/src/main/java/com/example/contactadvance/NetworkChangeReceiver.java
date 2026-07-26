package com.example.contactadvance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isNetworkAvailable(context)) {
            showLongerToast(context, "Mất kết nối mạng!");
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

        return false;
    }

    private void showLongerToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        new Handler().postDelayed(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show(), 2000);
    }
}