package com.tripper.tripper.trip.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import com.tripper.tripper.R;
import com.tripper.tripper.models.Trip;
import com.tripper.tripper.trip.activity.CreateTripActivity;
import com.tripper.tripper.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TripCreateTitleFragment extends Fragment {

    public static final String TAG = TripCreateTitleFragment.class.getSimpleName();

    View tripCreateTitleView;
    private EditText tripStartDateEditText;
    private EditText tripTitleEditText;
    private DatePickerDialog tripDatePickerDialog;
    private FloatingActionButton tripContinueFloatingActionButton;
    SimpleDateFormat dateFormatter;
    private Activity tripCreateParentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        tripCreateTitleView = inflater.inflate(R.layout.fragment_trip_create_title, container, false);

        dateFormatter = DateUtils.getFormDateFormat();
        tripCreateParentActivity = getActivity();

        findViewsById();
        setListeners();

        Trip currentTrip = ((CreateTripActivity)tripCreateParentActivity).currentCreatedTrip;
        tripTitleEditText.setText(currentTrip.getTitle());
        tripStartDateEditText.setText(dateFormatter.format(currentTrip.getStartDate()));

        setDatePickerSettings(currentTrip.getStartDate());

        return tripCreateTitleView;
    }

    private void findViewsById(){
        tripContinueFloatingActionButton = tripCreateTitleView.findViewById(R.id.trip_create_title_continue_floating_action_button);
        tripStartDateEditText = tripCreateTitleView.findViewById(R.id.date_txt);
        tripTitleEditText = tripCreateTitleView.findViewById(R.id.trip_create_title_edittext);
    }

    private void setListeners(){
        tripContinueFloatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (tripTitleEditText.getText().toString().trim().isEmpty()){
                    tripTitleEditText.requestFocus();
                    tripTitleEditText.setError(getResources().getString(R.string.trip_no_title_error_message));
                }
                else {
                    onContinueButtonSelect();
                }
            }
        });

        tripStartDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateUtils.updateDatePicker(tripDatePickerDialog, DateUtils.stringToDate(tripStartDateEditText.getText().toString(), dateFormatter));
                tripDatePickerDialog.show();
            }
        });

        tripTitleEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String strTxt = s.toString();
                ((CreateTripActivity)tripCreateParentActivity).currentCreatedTrip.setTitle(strTxt);
            }
        });
    }

    private void onContinueButtonSelect() {
        if (tripCreateParentActivity.findViewById(R.id.trip_create_fragment_container) != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.trip_create_fragment_container, new TripCreateDetailsFragment(), TripCreateDetailsFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void setDatePickerSettings(Date currentDate) {
        tripDatePickerDialog = DateUtils.getDatePicker(getActivity(), currentDate, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                tripStartDateEditText.setText(dateFormatter.format(newDate.getTime()));
                ((CreateTripActivity)tripCreateParentActivity).currentCreatedTrip.setStartDate(DateUtils.stringToDate(tripStartDateEditText.getText().toString(), dateFormatter));
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

}
