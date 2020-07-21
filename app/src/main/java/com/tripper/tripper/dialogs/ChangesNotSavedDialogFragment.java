package com.tripper.tripper.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.tripper.tripper.R;

public class ChangesNotSavedDialogFragment extends DialogFragment {

    public static final String TAG = ChangesNotSavedDialogFragment.class.getSimpleName();

    public static final int NOT_SAVED_DIALOG = 10;
    OnHandleDialogResult mCallback;

    public enum DialogOptions{
        YES,
        NO,
    }

    public interface OnHandleDialogResult{
        void onHandleDialogResult(int whichButton);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnHandleDialogResult) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHandleDialogResult");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog changesNotSavedDialogConfirm = new AlertDialog.Builder(getActivity())
                .setMessage(getResources().getString(R.string.unsaved_details_warning_dialog_message))
                .setTitle(getResources().getString(R.string.unsaved_details_warning_dialog_title))
                .setPositiveButton(getResources().getString(R.string.unsaved_details_warning_dialog_back_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mCallback.onHandleDialogResult(DialogOptions.YES.ordinal());
                    }
                })
                .setNegativeButton(getResources().getString(R.string.unsaved_details_warning_dialog_cancel_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mCallback.onHandleDialogResult(DialogOptions.NO.ordinal());
                    }
                })
                .create();
        return changesNotSavedDialogConfirm;
    }
}
