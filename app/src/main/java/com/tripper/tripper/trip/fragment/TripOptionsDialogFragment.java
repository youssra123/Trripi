package com.tripper.tripper.trip.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;

import com.tripper.tripper.R;

public class TripOptionsDialogFragment extends DialogFragment {

    public static final String TAG = TripOptionsDialogFragment.class.getSimpleName();

    public static final String CUR_TRIP_PARAM = "CUR_TRIP";

    public enum DialogOptions{
        VIEW,
        EDIT,
        DELETE
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] dialogOptionsArray = getResources().getStringArray(R.array.trips_settings_dialog_options);

        AlertDialog.Builder optionsDialogBuilder = new AlertDialog.Builder(getActivity());
        optionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                TripOptionsDialogFragment.DialogOptions whichOptionEnum = TripOptionsDialogFragment.DialogOptions.values()[which];
                Intent resultIntent = new Intent();
                resultIntent.putExtra(TripsListFragment.TRIP_DIALOG_OPTION, whichOptionEnum);
                getTargetFragment().onActivityResult(getTargetRequestCode(), getActivity().RESULT_OK, resultIntent);
            }
        });
        optionsDialogBuilder.setTitle(R.string.trip_options_dialog_title);
        AlertDialog optionsDialog = optionsDialogBuilder.create();
        ListView listView = optionsDialog.getListView();


        listView.setDivider(new ColorDrawable(ContextCompat.getColor
                (getActivity(), R.color.toolBarLineBackground)));
        listView.setDividerHeight(2);
        listView.setFooterDividersEnabled(false);
        return optionsDialog;
    }
}
