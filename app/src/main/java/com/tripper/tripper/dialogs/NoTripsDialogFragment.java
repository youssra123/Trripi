package com.tripper.tripper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tripper.tripper.R;

public class NoTripsDialogFragment extends DialogFragment {

    public static final String TAG = NoTripsDialogFragment.class.getSimpleName();

    public static final String NO_TRIPS_DIALOG_OPTION = "NO_TRIPS_DIALOG_OPTION";
    public static final String TITLE_FROM_NO_TRIPS_DIALOG = "TITLE_FROM_NO_TRIPS_DIALOG";
    private static final int NO_TRIPS_DIALOG = 0;
    public static final int CALLED_FROM_FRAGMENT = 1;
    public static final int CALLED_FROM_ACTIVITY = 2;
    public static final String CALLED_FROM_WHERE_ARGUMENT = "CALLED_FROM_WHERE_ARGUMENT";

    private boolean isCalledFromFragment = false;
    private EditText dialogEditText;
    private AlertDialog noTripsDialog;

    public static final String SAVE_IS_CALLED_FROM_FRAGMENT = "SAVE_IS_CALLED_FROM_FRAGMENT";

    public enum DialogOptions{
        DONE,
        CANCEL
    }

    public interface NoTripDialogClickListener {
         void onClickHandleParent(int whichButton, String newTripTitle);
    }

    private NoTripDialogClickListener callbackListener;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.no_trips_dialog, null);

        dialogEditText = dialogView.findViewById(R.id.no_trips_edit_text);

        if(savedInstanceState != null){
            isCalledFromFragment = savedInstanceState.getBoolean(SAVE_IS_CALLED_FROM_FRAGMENT);
        }

        else {
            isCalledFromFragment = getArguments().getInt(CALLED_FROM_WHERE_ARGUMENT) == CALLED_FROM_FRAGMENT;
        }


        if(!isCalledFromFragment) {
            try {
                callbackListener = (NoTripDialogClickListener) getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException("Calling activity must implement NoTripDialogClickListener interface");
            }
        }

        AlertDialog.Builder noTripsDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
                .setTitle(R.string.no_trips_dialog_title)
                .setView(dialogView)
                .setPositiveButton(R.string.description_dialog_done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // onClickHandleParent(DialogOptions.DONE.ordinal());
                    }
                })
                .setNegativeButton(R.string.description_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onClickHandle(DialogOptions.CANCEL.ordinal());
                    }
                });
        noTripsDialog = noTripsDialogBuilder.create();
        setCancelable(false);
        noTripsDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                //TODO: CHECK IF NEED TO ADD CANCEL LISTENER
                Button doneButton = noTripsDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (onClickHandle(DialogOptions.DONE.ordinal())) {
                            noTripsDialog.dismiss();
                        }
                    }
                });
            }
        });

        return noTripsDialog;
    }



    private boolean onClickHandle(int whichButton){
        boolean res = false;
        DialogOptions whichOptionEnum = DialogOptions.values()[whichButton];
        if(whichOptionEnum == DialogOptions.DONE && dialogEditText.getText().toString().trim().isEmpty()){
            dialogEditText.requestFocus();
            dialogEditText.setError(getResources().getString(R.string.trip_no_title_error_message));
        }
        else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(NO_TRIPS_DIALOG_OPTION, whichOptionEnum);
            resultIntent.putExtra(TITLE_FROM_NO_TRIPS_DIALOG, dialogEditText.getText().toString());
            if(isCalledFromFragment) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), getActivity().RESULT_OK, resultIntent);
            }
            else {
                callbackListener.onClickHandleParent(whichButton,dialogEditText.getText().toString());
            }
            res = true;
        }
        return res;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_IS_CALLED_FROM_FRAGMENT, isCalledFromFragment);
    }

}
