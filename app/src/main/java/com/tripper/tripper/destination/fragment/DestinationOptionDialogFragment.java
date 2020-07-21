package com.tripper.tripper.destination.fragment;

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
import com.tripper.tripper.models.Destination;

public class DestinationOptionDialogFragment extends DialogFragment {

    public static final String TAG = DestinationOptionDialogFragment.class.getSimpleName();

    private String[] dialogOptionsArray;
    private AlertDialog optionsDialog;
    private Destination currentDestination;
    public static final String CUR_LANDMARK_PARAM = "CUR_LANDMARK";
  //  private Trip currentTrip;

    public enum DialogOptions{
        VIEW,
        EDIT,
        DELETE
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialogOptionsArray = getResources().getStringArray(R.array.destinations_settings_dialog_options);
//        Bundle mArgs = getArguments();
//        currentDestination = mArgs.getParcelable(CUR_LANDMARK_PARAM);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder optionsDialogBuilder = new AlertDialog.Builder(getActivity());
        optionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                DestinationOptionDialogFragment.DialogOptions whichOptionEnum = DestinationOptionDialogFragment.DialogOptions.values()[which];
                Intent resultIntent = new Intent();
                resultIntent.putExtra(DestinationListFragment.DESTINATION_DIALOG_OPTION, whichOptionEnum);
                getTargetFragment().onActivityResult(getTargetRequestCode(), getActivity().RESULT_OK, resultIntent);
            }
        });
        optionsDialogBuilder.setTitle(R.string.destination_options_dialog_title);

        optionsDialog = optionsDialogBuilder.create();
        ListView listView = optionsDialog.getListView();

        //TODO: remove divider at the end
        listView.setDivider(new ColorDrawable(ContextCompat.getColor
                (getActivity(), R.color.toolBarLineBackground))); // set color
        listView.setDividerHeight(2); // set height
        // Create the AlertDialog object and return it
        return optionsDialog;
    }
}
