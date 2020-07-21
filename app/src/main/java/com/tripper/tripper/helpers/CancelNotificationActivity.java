package com.tripper.tripper.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.tripper.tripper.R;
import com.tripper.tripper.utils.NotificationUtils;
import com.tripper.tripper.utils.SharedPreferencesUtils;

public class CancelNotificationActivity extends Activity {

    public CheckBox cancelShowAgainCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_notification);

        if(!SharedPreferencesUtils.getCancelNotificationsWarningDialogState(this)){
            createAndShowDialog();
        }
        else {
            NotificationUtils.cancelNotification(this);
            finishAffinity();
        }
    }
    private void createAndShowDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater alertDialogBuilderInflater = LayoutInflater.from(this);
        View checkboxView = alertDialogBuilderInflater.inflate(R.layout.cancel_notification_dialog, null);

        cancelShowAgainCB = checkboxView.findViewById(R.id.notification_cancel_checkbox);
        alertDialogBuilder.setView(checkboxView);
        alertDialogBuilder.setTitle(getResources().getString(R.string.notification_hide_dialog_title));
        alertDialogBuilder.setMessage(getResources().getString(R.string.notification_hide_dialog_message));

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.notification_hide_dialog_yes_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                SharedPreferencesUtils.saveCancelNotificationsWarningDialogState(CancelNotificationActivity.this, cancelShowAgainCB.isChecked());
                NotificationUtils.cancelNotification(CancelNotificationActivity.this);
                finishAffinity();
            }
        });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.notification_hide_dialog_no_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferencesUtils.saveCancelNotificationsWarningDialogState(CancelNotificationActivity.this, cancelShowAgainCB.isChecked());
                finishAffinity();
            }
        });

        alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finishAffinity();
            }
        });

        alertDialogBuilder.show();

    }
}
