package com.example.aashish.connect;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Kisan Thapa on 8/9/2017.
 */

public class ConnectionDetector {
    Context context;
    public ConnectionDetector(Context context) {
        this.context = context;
    }
    public boolean isConnected(){
        ConnectivityManager cManager = (ConnectivityManager)context.getSystemService(Service.CONNECTIVITY_SERVICE);

        if (cManager != null){
            NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
            if (networkInfo != null){
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }
        return false;
    }
}
