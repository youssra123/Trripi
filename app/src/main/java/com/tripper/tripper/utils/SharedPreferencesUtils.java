package com.tripper.tripper.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesUtils {

    public static final String SAVE_ENABLE_NOTIFICATIONS_STATE = "SAVE_ENABLE_NOTIFICATIONS_STATE";
    public static final String SAVE_CLOSE_NOTIFICATIONS_STATE = "SAVE_CLOSE_NOTIFICATIONS_STATE";
    private static final String SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME";
    public static final String SAVE_CANCEL_NOTIFICATIONS_DIALOG_STATE = "SAVE_CANCEL_NOTIFICATIONS_DIALOG_STATE";
    public static final String SAVE_IS_NOTIFICATION_WINDOW_OPEN_STATE = "SAVE_IS_NOTIFICATION_WINDOW_OPEN_STATE";

    public static void saveEnableNotificationsState(Context appContext, boolean notificationState){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        prefsEditor.putBoolean(SAVE_ENABLE_NOTIFICATIONS_STATE, notificationState);
        prefsEditor.commit();
    }

    public static boolean getEnableNotificationsState(Context appContext){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(SAVE_ENABLE_NOTIFICATIONS_STATE, true);
    }

    public static void saveCancelNotificationsWarningDialogState(Context appContext, boolean toCancel){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        prefsEditor.putBoolean(SAVE_CANCEL_NOTIFICATIONS_DIALOG_STATE, toCancel);
        prefsEditor.commit();
    }

    public static boolean getCancelNotificationsWarningDialogState(Context appContext){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(SAVE_CANCEL_NOTIFICATIONS_DIALOG_STATE, false);
    }

    public static void saveCloseNotificationsState(Context appContext, boolean toClose){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        prefsEditor.putBoolean(SAVE_CLOSE_NOTIFICATIONS_STATE, toClose);
        prefsEditor.commit();
    }

    public static boolean getCloseNotificationsState(Context appContext){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(SAVE_CLOSE_NOTIFICATIONS_STATE, false);
    }

    public static void saveIsNotificationsWindowOpen(Context appContext, boolean isOpen){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        prefsEditor.putBoolean(SAVE_IS_NOTIFICATION_WINDOW_OPEN_STATE, isOpen);
        prefsEditor.commit();
    }

    public static boolean getIsNotificationsWindowOpen(Context appContext) {
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(SAVE_IS_NOTIFICATION_WINDOW_OPEN_STATE, false);
    }
}
