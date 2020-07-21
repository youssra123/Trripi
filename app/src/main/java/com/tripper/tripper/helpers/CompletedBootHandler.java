package com.tripper.tripper.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tripper.tripper.models.Trip;
import com.tripper.tripper.utils.DatabaseUtils;
import com.tripper.tripper.utils.NotificationUtils;
import com.tripper.tripper.utils.SharedPreferencesUtils;


public class CompletedBootHandler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Trip latestTrip = DatabaseUtils.getLastTrip(context);
        if(latestTrip != null && SharedPreferencesUtils.getIsNotificationsWindowOpen(context)){
            NotificationUtils.initNotification(context, latestTrip.getTitle());
        }
    }
}
