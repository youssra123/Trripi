package com.tripper.tripper.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.tripper.tripper.R;
import com.tripper.tripper.destination.activity.DestinationMainActivity;
import com.tripper.tripper.helpers.CancelNotificationActivity;

/**
 * Created by david on 1/10/2017.
 */

public class NotificationUtils {

    public static final String NOTIFICATION_ADD_LANDMARK_ACTION_STR = "NOTIFICATION_ADD_LANDMARK_ACTION_STR";
    public static final String NOTIFICATION_HIDE_ACTION_STR = "NOTIFICATION_HIDE_ACTION_STR";

    public static final int NOTIFICATION_ADD_LANDMARK_REQUEST = 1500;
    public static final int NOTIFICATION_HIDE_REQUEST = 1000;
    public static final int NOTIFICATION_ID = 2000;

    public static void initNotification(Context context, String textTitle){

        SharedPreferencesUtils.saveIsNotificationsWindowOpen(context, true);

        // Intent for add Destination action
        Intent resultIntentAddLandmark = new Intent(context, DestinationMainActivity.class);
        resultIntentAddLandmark.setAction(NOTIFICATION_ADD_LANDMARK_ACTION_STR);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(DestinationMainActivity.class);
        stackBuilder.addNextIntent(resultIntentAddLandmark);

        PendingIntent resultPendingIntentAddLandmark =
                PendingIntent.getActivity(
                        context,
                        NOTIFICATION_ADD_LANDMARK_REQUEST,
                        resultIntentAddLandmark,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Action addLandmarkAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_add_black_24dp,
                        context.getString(R.string.notification_add_destination),
                        resultPendingIntentAddLandmark)
                        .build();


        // Intent for cancel notification action
        Intent resultIntentCancelNotification = new Intent(context, CancelNotificationActivity.class);
        resultIntentCancelNotification.setAction(NOTIFICATION_HIDE_ACTION_STR);
        resultIntentCancelNotification.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        resultIntentCancelNotification.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntentCancelNotification =
                PendingIntent.getActivity(
                        context,
                        NOTIFICATION_HIDE_REQUEST,
                        resultIntentCancelNotification,
                        PendingIntent.FLAG_CANCEL_CURRENT
//                        PendingIntent.FLAG_UPDATE_CURRENT

                );

        NotificationCompat.Action cancelNotificationAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_clear_black_24dp,
                        context.getString(R.string.notification_hide),
                        resultPendingIntentCancelNotification)
                        .build();

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_tree_icon)
                        .setLargeIcon(ImageUtils.getBitmap(context, R.drawable.ic_logo))
                        .setColor(context.getResources().getColor(R.color.notificationBackground))
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(context.getString(R.string.notification_added_to_trip_message, textTitle))
                        .addAction(addLandmarkAction)
                        .addAction(cancelNotificationAction)
                        .addAction(0, "", null)
//                        .setCustomContentView(contentView)
                        .setOngoing(true);

//        mBuilder.setContentIntent(resultPendingIntentAddLandmark);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static void cancelNotification(Context context){
        SharedPreferencesUtils.saveIsNotificationsWindowOpen(context, false);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public static boolean areNotificationsEnabled(Context context){
        return SharedPreferencesUtils.getEnableNotificationsState(context);
    }
}
