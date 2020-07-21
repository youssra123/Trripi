package com.tripper.tripper.destination.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tripper.tripper.R;
import com.tripper.tripper.destination.activity.DestinationMainActivity;
import com.tripper.tripper.destination.activity.SingleDestinationMap;
import com.tripper.tripper.destination.interfaces.OnGetCurrentDestination;
import com.tripper.tripper.dialogs.DescriptionDialogFragment;
import com.tripper.tripper.dialogs.NoTripsDialogFragment;
import com.tripper.tripper.models.Destination;
import com.tripper.tripper.models.Trip;
import com.tripper.tripper.services.MyContentProvider;
import com.tripper.tripper.trip.interfaces.OnGetCurrentTrip;
import com.tripper.tripper.utils.DatabaseUtils;
import com.tripper.tripper.utils.DateUtils;
import com.tripper.tripper.utils.ImageUtils;
import com.tripper.tripper.utils.LocationUtils;
import com.tripper.tripper.utils.NotificationUtils;
import com.tripper.tripper.utils.StartActivityUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class DestinationDetailsFragment extends Fragment implements
        OnConnectionFailedListener, ConnectionCallbacks{

    public static final String TAG = DestinationDetailsFragment.class.getSimpleName();

    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int TAKE_PHOTO_FROM_CAMERA_ACTION = 1;
    private static final int LANDMARK_SINGLE_MAP_INTENT_ACTION = 2;
    private static final int REQUEST_LOCATION_PERMISSION_ACTION = 3;
    private static final int REQUEST_CAMERA_PERMISSION_ACTION = 4;
    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 5;
    private static final int DESCRIPTION_DIALOG = 6;
    private static final int NO_TRIPS_DIALOG = 7;


    public enum PhotoDialogOptions{
        CHANGE_PICTURE,
        TAKE_PHOTO
    }

    private EditText lmTitleEditText;
    private ImageView lmPhotoImageView;
    private EditText lmDateEditText;
    private EditText lmTimeEditText;
    private TextView lmAutomaticLocationTextView;
    private TextView getLmAutomaticLocationErrorTextView;
    private ImageButton lmGpsLocationImageButton;
    private EditText lmLocationDescriptionEditText;
    private Spinner lmTypeSpinner;
    private EditText lmDescriptionEditText;
    private FloatingActionButton lmDoneButton;
    private ViewSwitcher lmLoadingMapViewSwitcher;

    // Private parameters
    private Uri photoURI;
    private View parentView;
    private ImageView lmIconTypeSpinner;
    private boolean isMapClicked = false;
    private boolean isCalledFromUpdateLandmark;
    private boolean isCalledFromGallery = false;
    private boolean isCalledFromNotification = false;
    private AlertDialog.Builder optionsDialogBuilder;
    private boolean isRequestedPermissionFromCamera;
    private OnGetCurrentDestination mCallback;
    //    private OnGetCurrentTripID mCallbackGetCurTripId;
    private OnGetCurrentTrip mCallbackGetCurTrip;
    private OnLandmarkAddedListener mCallbackOnLandmarkAddedListener;
    private Bundle onCreatesOnSavedInstance;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean isRealAutomaticLocation;
    private String currentLmPhotoPath;
    private Date lmCurrentDate;
    private DatePickerDialog lmDatePicker;
    private TimePickerDialog lmTimePicker;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;
    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;
    private BroadcastReceiver broadcastReceiver;
    private AsyncTask<Void, Integer, String> updateLocationTask;
    private final Handler handler = new Handler();
    private Runnable r;

    // add Destination from gallery
    private TextView parentTripMessage;

    // Destination Details Final Parameters
    private Destination finalDestination;

    //Description Dialog
    public static final String initDescription = "initDescription";

    //Save State
    private String saveFinalLandmark = "saveLandmark";
    private String saveCurrentTrip = "saveCurrentTrip";
    private String saveLmCurrentDate= "saveLmCurrentDate";
    private String saveIsCalledFromGallery = "saveIsCalledFromGallery";
    private String saveIsCalledFromNotification = "saveIsCalledFromNotification";
    private String saveIsRequestedPermissionFromCamera = "saveIsRequestedPermissionFromCamera";
    private String savemLastLocation = "savemLastLocation";
    private String saveIsRealAutomaticLocation = "saveIsRealAutomaticLocation";

    private String saveNewTakePhotoPath = "saveNewTakePhotoPath";

    private String newTakePhotoPath;

    private Trip currentTrip;

    public interface OnLandmarkAddedListener {
        void onLandmarkAdded();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        if (savedInstanceState != null) {
            isCalledFromUpdateLandmark = savedInstanceState.getBoolean("isCalledFromUpdateLandmark");
            isRequestedPermissionFromCamera = savedInstanceState.getBoolean(saveIsRequestedPermissionFromCamera);
            isCalledFromGallery = savedInstanceState.getBoolean(saveIsCalledFromGallery);
            isCalledFromNotification = savedInstanceState.getBoolean(saveIsCalledFromNotification);
            mLastLocation = savedInstanceState.getParcelable(savemLastLocation);
            isRealAutomaticLocation = savedInstanceState.getBoolean(saveIsRealAutomaticLocation);
            finalDestination = savedInstanceState.getParcelable(saveFinalLandmark);
            currentTrip = savedInstanceState.getParcelable(saveCurrentTrip);
            lmCurrentDate = new Date(savedInstanceState.getLong(saveLmCurrentDate));
            currentLmPhotoPath = savedInstanceState.getString("savedImagePath");
            newTakePhotoPath = savedInstanceState.getString(saveNewTakePhotoPath);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        onCreatesOnSavedInstance = savedInstanceState;

        // Inflate the layout for this fragment
        parentView = inflater.inflate(R.layout.fragment_destination_details, container, false);

        // get all private views by id's
        findViewsById(parentView);

        // initialize the Destination spinner
        initLmSpinner(parentView, savedInstanceState);

        // init the details fragment dialogs
        initDialogs();

        // init hardware status
        initHardwareStatus();

        // Building the GoogleApi client
        buildGoogleApiClient();

        // set all listeners
        setListeners();

        // initialize Destination date parameters
        dateFormatter = DateUtils.getFormDateFormat();
        timeFormatter = DateUtils.getLandmarkTimeDateFormat();
        updateLandmarkDate(savedInstanceState != null ? lmCurrentDate : new Date());

        parentTripMessage.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            updateLmPhotoImageView(currentLmPhotoPath);

            if(isCalledFromGallery || isCalledFromNotification){
                updateParentTripMessage();
            }
        } else {
            // initialize the create/update boolean so we can check where we were called from
            isCalledFromUpdateLandmark = false;

            currentTrip = mCallbackGetCurTrip.onGetCurrentTrip();
            finalDestination = mCallback.onGetCurrentDestination();
            if (finalDestination != null) {
                // We were called from Update Destination need to update parameters
                updateLmParameters();
            }
            else {
                Bundle args = getArguments();
                if(args != null) {
                    if(args.getString(NotificationUtils.NOTIFICATION_ADD_LANDMARK_ACTION_STR) != null){
                        isCalledFromNotification = true;
                        currentTrip = DatabaseUtils.getLastTrip(getActivity());
                        updateParentTripMessage();
                    }else {
                        currentLmPhotoPath = args.getString(DestinationMainActivity.IMAGE_FROM_GALLERY_PATH);
                        isCalledFromGallery = true;

                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            FragmentCompat.requestPermissions(DestinationDetailsFragment.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                        } else {
                            handleLandmarkFromGallery();
                        }
                    }
                }
            }
        }


        // update the toolbar title.
        int toolBarStringRes = isCalledFromUpdateLandmark ? R.string.destination_update_destination_toolbar_title : R.string.destination_create_new_destination_toolbar_title;
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getResources().getString(toolBarStringRes));
        actionBar.show();

        // create the date picker after we have the updated current date.
        initDateAndTimePickerSettings(lmCurrentDate);

        return parentView;
    }

    private void initHardwareStatus(){
        initNetworkListener();
    }

    private void handleLandmarkFromGallery(){
        ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLmPhotoPath, lmPhotoImageView);

        currentTrip = DatabaseUtils.getLastTrip(getActivity());
        if(currentTrip == null){
            NoTripsDialogFragment dialogFragment = new NoTripsDialogFragment();
            dialogFragment.setTargetFragment(DestinationDetailsFragment.this, NO_TRIPS_DIALOG);

            Bundle args = new Bundle();
            args.putInt(NoTripsDialogFragment.CALLED_FROM_WHERE_ARGUMENT, NoTripsDialogFragment.CALLED_FROM_FRAGMENT);
            dialogFragment.setArguments(args);

            dialogFragment.show(getFragmentManager(), "noTrips");
        }
        else {
            handleLandmarkFromGalleryWhenThereAreTrips();
        }
    }

    private void handleLandmarkFromGalleryWhenThereAreTrips(){
        getDataFromPhotoAndUpdateLandmark(currentLmPhotoPath);
        updateParentTripMessage();
    }

    // find all needed views by id's
    private void findViewsById(View parentView) {
        lmTitleEditText = parentView.findViewById(R.id.landmark_details_title_edit_text);
        lmPhotoImageView = parentView.findViewById(R.id.landmark_details_photo_image_view);
        lmAutomaticLocationTextView = parentView.findViewById(R.id.landmark_details_automatic_location);
        getLmAutomaticLocationErrorTextView = parentView.findViewById(R.id.landmark_details_automatic_location_error);
        lmGpsLocationImageButton = parentView.findViewById(R.id.landmark_details_gps_location_image_button);
        lmLocationDescriptionEditText = parentView.findViewById(R.id.landmark_details_location_description_edit_text);
        lmDateEditText = parentView.findViewById(R.id.landmark_details_date_edit_text);
        lmTimeEditText = parentView.findViewById(R.id.landmark_details_time_edit_text);
        lmTypeSpinner = parentView.findViewById(R.id.landmark_details_type_spinner);
        lmIconTypeSpinner = parentView.findViewById(R.id.landmark_details_icon_type_spinner_item);
        lmDescriptionEditText = parentView.findViewById(R.id.landmark_details_description_edit_text);
        lmDoneButton = parentView.findViewById(R.id.landmark_details_floating_action_button);
        parentTripMessage = parentView.findViewById(R.id.parent_trip_message);
        lmLoadingMapViewSwitcher = parentView.findViewById(R.id.landmark_details_gps_view_switcher);
    }

    private void setListeners() {
        // Destination Photo Listener
        lmPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionsDialogBuilder.show();
            }
        });

        // Date Edit Text Listener
        lmDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateUtils.updateDatePicker(lmDatePicker, lmCurrentDate);
                lmDatePicker.show();
            }
        });

        // Date Edit Text Listener
        lmTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateUtils.updateTimePicker(lmTimePicker,lmCurrentDate);
                lmTimePicker.show();
            }
        });

        lmGpsLocationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(LocationUtils.checkPlayServices(getActivity(), true)){
                    lmLoadingMapViewSwitcher.showPrevious();
                    isMapClicked = true;
                    // if connected and already created location updates
                    if(mGoogleApiClient != null && mGoogleApiClient.isConnected() && mLocationRequest == null) {
                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if(!isCalledFromUpdateLandmark){
                                CreateLocationRequest();
                            }
                            else{
                                isMapClicked = false;
                                startGoogleMapIntent();
                            }
                        } else {
                            checkLocationPermission();
                        }
                    } else{
                        isMapClicked = false;
                        startGoogleMapIntent();
                    }
                }
                // else, play services not supported at the moment
            }
        });

        lmTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                TypedArray iconType = getResources().obtainTypedArray(R.array.landmark_view_details_icon_type_array);
                if(position > 0){
                    lmIconTypeSpinner.setVisibility(View.VISIBLE);
                    lmIconTypeSpinner.setImageResource(iconType.getResourceId(position, -1));
                }
                else{
                    lmIconTypeSpinner.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Destination Description TextView Got Clicked (Pop Up Editor)
        lmDescriptionEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment descriptionDialog = new DescriptionDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString(initDescription, lmDescriptionEditText.getText().toString());
                descriptionDialog.setArguments(bundle);
                descriptionDialog.setTargetFragment(DestinationDetailsFragment.this, DESCRIPTION_DIALOG);
                descriptionDialog.show(getFragmentManager(), "Description");
            }
        });


        // Destination Done button Listener (Available only if title or picture was insert)
        lmDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lmTitleEditText.getText().toString().trim().isEmpty() && (currentLmPhotoPath == null || currentLmPhotoPath.isEmpty())) {
                    lmTitleEditText.requestFocus();
                    lmTitleEditText.setError(getResources().getString(R.string.destination_no_title_or_photo_error_message));
                }
                else {
                    if (isCalledFromGallery || isCalledFromNotification) {
                        if(createAndInsertNewLandmark()) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_destination_added_message_success), Toast.LENGTH_LONG).show();
                            getActivity().finishAffinity();
                        }
                        else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_destination_added_message_fail), Toast.LENGTH_LONG).show();
                        }
                    } else if(!isCalledFromUpdateLandmark) {
                        if(!createAndInsertNewLandmark()){
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_destination_added_message_fail), Toast.LENGTH_LONG).show();
                        }
                        else {
                            getFragmentManager().popBackStackImmediate();
                        }

                    } else {
                        // Update the final Destination
                        setLandmarkParameters(finalDestination);

                        // Update the DataBase with the edited Destination
                        getActivity().getContentResolver().update(
                                ContentUris.withAppendedId(MyContentProvider.CONTENT_LANDMARK_ID_URI_BASE, finalDestination.getId()),
                                finalDestination.landmarkToContentValues(),
                                null,
                                null);

                        if(DateUtils.isFirstLaterThanSecond(lmCurrentDate, currentTrip.getEndDate())){
                            //update trip end date
                            updateTripEndDate(currentTrip.getId(), lmCurrentDate);
                        }

                        getFragmentManager().popBackStackImmediate();
                    }

                }
            }
        });
    }

    private void createUpdateLocationTask(){
        if(updateLocationTask != null ){
            if(updateLocationTask.isCancelled()){
                return;
            }
            if(updateLocationTask.getStatus() != AsyncTask.Status.FINISHED){
                updateLocationTask.cancel(true);
            }
            updateLocationTask = null;
        }

        updateLocationTask = new AsyncTask<Void, Integer, String>(){
            final String loadingAppendText[] = {".", "..", "..."};

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                r = new Runnable()
                {
                    int index = 0;
                    public void run()
                    {
                        if(updateLocationTask.isCancelled()){
                            handler.removeCallbacks(r);
                            return;
                        }
                        publishProgress(index++);
                    }
                };

                handler.postDelayed(r, 400);
            }



            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if(updateLocationTask == null || updateLocationTask.isCancelled() || updateLocationTask.getStatus() == Status.FINISHED){
                    return;
                }
                isRealAutomaticLocation = false;
                if(isAdded()){
                    lmAutomaticLocationTextView.setText(TextUtils.concat(getResources().getString(R.string.destination_details_automatic_location_loading_text), loadingAppendText[values[0] % 3]));
                    handler.postDelayed(r, 400);
                }
            }

            @Override
            protected void onPostExecute(String stringResult) {
                super.onPostExecute(stringResult);
                handler.removeCallbacks(r);
                isRealAutomaticLocation = handleAutomaticLocationOptions(
                        lmAutomaticLocationTextView,
                        getLmAutomaticLocationErrorTextView ,
                        mLastLocation,
                        stringResult
                );
            }

            @Override
            protected String doInBackground(Void... params) {
                return LocationUtils.updateLmLocationString(getActivity(), mLastLocation);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setLandmarkParameters(Destination destination){
        destination.setTitle(lmTitleEditText.getText().toString().trim());
        destination.setPhotoPath(currentLmPhotoPath);
        destination.setDate(lmCurrentDate);
        destination.setAutomaticLocation(getRealAutomaticLocation());
        destination.setGPSLocation(mLastLocation);
        destination.setLocationDescription(lmLocationDescriptionEditText.getText().toString().trim());
        destination.setDescription(lmDescriptionEditText.getText().toString().trim());
        destination.setTypePosition(lmTypeSpinner.getSelectedItemPosition());
    }

    private boolean createAndInsertNewLandmark(){
        Boolean result = true;

        // Create the new final Destination
        finalDestination = new Destination(currentTrip.getId(), lmTitleEditText.getText().toString().trim(), currentLmPhotoPath, lmCurrentDate,
                getRealAutomaticLocation(), mLastLocation, lmLocationDescriptionEditText.getText().toString().trim(),
                lmDescriptionEditText.getText().toString().trim(), lmTypeSpinner.getSelectedItemPosition());

        try {
            // Insert data to DataBase
            getActivity().getContentResolver().insert(
                    MyContentProvider.CONTENT_LANDMARKS_URI,
                    finalDestination.landmarkToContentValues());

            if(DateUtils.isFirstLaterThanSecond(lmCurrentDate, currentTrip.getEndDate())){
                //update trip end date
                updateTripEndDate(currentTrip.getId(), lmCurrentDate);
            }
        }
        catch (SQLException e){
            result = false;
        }

        if (result) {
            mCallbackOnLandmarkAddedListener.onLandmarkAdded();
        }
        return result;
    }

    private void handleLocationUpdateDone(){
        if(lmLoadingMapViewSwitcher.getCurrentView() != lmGpsLocationImageButton){
            lmLoadingMapViewSwitcher.showNext();
        }
    }

    // Update Destination , need to update Destination Parameters
    private void updateLmParameters() {

        // We were called from update Destination (not create)
        isCalledFromUpdateLandmark = true;

        // Don't need the map load since we wont request location updates
        handleLocationUpdateDone();

        lmTitleEditText.setText(finalDestination.getTitle());

        updateLmPhotoImageView(finalDestination.getPhotoPath());

        updateLandmarkDate(finalDestination.getDate());

        mLastLocation = finalDestination.getGPSLocation();

        isRealAutomaticLocation = handleAutomaticLocationOptions(lmAutomaticLocationTextView, getLmAutomaticLocationErrorTextView, mLastLocation, finalDestination.getAutomaticLocation());

        lmLocationDescriptionEditText.setText(finalDestination.getLocationDescription());

        lmTypeSpinner.setSelection(finalDestination.getTypePosition());

        TypedArray iconType = getResources().obtainTypedArray(R.array.landmark_view_details_icon_type_array);
        if(finalDestination.getTypePosition() > 0){
            lmIconTypeSpinner.setVisibility(View.VISIBLE);
            lmIconTypeSpinner.setImageResource(iconType.getResourceId(finalDestination.getTypePosition(), -1));
        }
        else{
            lmIconTypeSpinner.setVisibility(View.INVISIBLE);
        }

        lmDescriptionEditText.setText(finalDestination.getDescription());

    }

    private void updateLmPhotoImageView(String imagePath){
        currentLmPhotoPath = imagePath;
        if (!ImageUtils.isPhotoExist(currentLmPhotoPath)) {
            // check if photo not exist in order to force to user to enter new photo.
            currentLmPhotoPath = null;
        }
        ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLmPhotoPath, lmPhotoImageView);
    }

    private void initLmSpinner(View parentView, Bundle savedInstanceState) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentView.getContext(),
                R.array.destination_details_type_spinner_array, R.layout.destination_details_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lmTypeSpinner.setAdapter(adapter);
        if (savedInstanceState == null){
            lmIconTypeSpinner.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        if(LocationUtils.checkPlayServices(getActivity(), false)){
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
        }
        else{
            handleLocationUpdateDone();
            handleUnavailableLocationMessage(lmAutomaticLocationTextView, getLmAutomaticLocationErrorTextView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_GALLERY_PHOTO_ACTION:
                if (resultCode == DestinationMainActivity.RESULT_OK && data != null) {
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(imageUri, filePath, null, null, null);
                    try{
                        cursor.moveToFirst();

                        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                        ImageUtils.updatePhotoImageViewByPath(getActivity(), imagePath, lmPhotoImageView);
                        getDataFromPhotoAndUpdateLandmark(imagePath);

                        lmTitleEditText.setError(null);

                        // save the current photo path
                        currentLmPhotoPath = imagePath;
                    }catch(NullPointerException e) {
                        Log.wtf(TAG, "cursor.moveToFirst() is null :(");
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                break;
            case TAKE_PHOTO_FROM_CAMERA_ACTION:
                if (resultCode == DestinationMainActivity.RESULT_OK) {
                    currentLmPhotoPath = newTakePhotoPath;
                    try {

                        ImageUtils.insertImageToGallery(getActivity(),currentLmPhotoPath, mLastLocation);

                        ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLmPhotoPath, lmPhotoImageView);
                        lmTitleEditText.setError(null);
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    newTakePhotoPath = null;
//                    currentLmPhotoPath = null;
//                    ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLmPhotoPath, lmPhotoImageView);
                    if (resultCode != Activity.RESULT_CANCELED) {
                        Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case LANDMARK_SINGLE_MAP_INTENT_ACTION:
                if (resultCode == DestinationMainActivity.RESULT_OK && data != null) {
                    mLastLocation = data.getParcelableExtra(DestinationMainActivity.LandmarkNewGPSLocation);
                    if(mLastLocation != null) {
                        String locationText = data.getStringExtra(DestinationMainActivity.LandmarkNewLocation);
                        if (locationText != null) {
                            isRealAutomaticLocation = handleAutomaticLocationOptions(
                                    lmAutomaticLocationTextView,
                                    getLmAutomaticLocationErrorTextView,
                                    mLastLocation,
                                    locationText
                            );
                        } else {
                            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                                createUpdateLocationTask();
                            }
                        }
                    }
                }
                handleLocationUpdateDone();
                break;
            case DESCRIPTION_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    DescriptionDialogFragment.DialogOptions whichOptionEnum = (DescriptionDialogFragment.DialogOptions) data.getSerializableExtra(DescriptionDialogFragment.DESCRIPTION_DIALOG_OPTION);
                    switch (whichOptionEnum) {
                        case DONE:
                            lmDescriptionEditText.setText(data.getStringExtra(DescriptionDialogFragment.DESCRIPTION_FROM_DIALOG));
                            break;
                        case CANCEL:
                            break;
                    }
                }
                break;
            case NO_TRIPS_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    NoTripsDialogFragment.DialogOptions whichOptionEnum = (NoTripsDialogFragment.DialogOptions) data.getSerializableExtra(NoTripsDialogFragment.NO_TRIPS_DIALOG_OPTION);
                    switch (whichOptionEnum) {
                        case DONE:
                            String title = data.getStringExtra(NoTripsDialogFragment.TITLE_FROM_NO_TRIPS_DIALOG);
                            Trip newTrip = new Trip(title, Calendar.getInstance().getTime(), "", "", "");

                            int tripId = DatabaseUtils.addNewTrip(getActivity(), newTrip);
                            newTrip.setId(tripId);
                            currentTrip = newTrip;

                            handleLandmarkFromGalleryWhenThereAreTrips();

//                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_trip_added_message), Toast.LENGTH_LONG).show();
                            break;
                        case CANCEL:
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_trips_dialog_canceled_message), Toast.LENGTH_LONG).show();
                            getActivity().finishAffinity();
                    }
                }
        }
    }

//    private boolean compressAndSaveImage(File file, Bitmap bitmap) {
//        boolean result = false;
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//            if (result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
//                Log.w("image manager", "Compression success");
//            }
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

    private void handleUnavailableLocationMessage(TextView textView, TextView errorTextView){
        String locationUnAvailableMessage = "<i>" + getResources().getString(R.string.destination_location_is_unavailable) + "</i>";
        textView.setText(Html.fromHtml(locationUnAvailableMessage));
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || !LocationUtils.checkPlayServices(getActivity(), false)){
            errorTextView.setText(getResources().getString(R.string.destination_sub_gps_no_permissions_message));
        }
        else { // gps permission was granted
            if (isCalledFromUpdateLandmark) {
                errorTextView.setText(getResources().getString(R.string.destination_sub_gps_on_update_message));
            } else {
                errorTextView.setText(getResources().getString(R.string.destination_sub_gps_message));
            }
        }
        errorTextView.setVisibility(View.VISIBLE);
    }

    public boolean handleLocationTextViewStringOptions(TextView textView, TextView errorTextView, Location location, String locationText){
        boolean isResultOk = false;
        if (locationText != null && !locationText.isEmpty()){
            textView.setText(locationText);

            if(locationText.equals(LocationUtils.locationToLatLngString(getActivity(), location))) {
                if(!LocationUtils.IsNetworkEnabled(getActivity())) {
                    errorTextView.setText(getResources().getString(R.string.destination_sub_network_message));
                    errorTextView.setVisibility(View.VISIBLE);
                }
                else{
                    errorTextView.setVisibility(View.GONE);
                }
                return isResultOk;
            }

            if (locationText.equals(getResources().getString(R.string.destination_location_is_unavailable))) {
                handleUnavailableLocationMessage(textView, errorTextView);
                return isResultOk;
            }

            errorTextView.setVisibility(View.GONE);
            isResultOk = true;
        } else{
            if(location != null){
                textView.setText(LocationUtils.locationToLatLngString(getActivity(), location));
                if(!LocationUtils.IsNetworkEnabled(getActivity())) {
                    errorTextView.setText(getResources().getString(R.string.destination_sub_network_message));
                    errorTextView.setVisibility(View.VISIBLE);
                }
                else{
                    errorTextView.setVisibility(View.GONE);
                }
            }
            else{
                handleUnavailableLocationMessage(textView, errorTextView);
            }
        }
        return isResultOk;
    }

    private boolean handleAutomaticLocationOptions(TextView textView, TextView errorTextView, Location location, String locationText){
        return handleLocationTextViewStringOptions(
                textView,
                errorTextView,
                location,
                locationText
        );
    }

    private void getDataFromPhotoAndUpdateLandmark(String imagePath) {
        ExifInterface exifInterface = ImageUtils.getImageExif(imagePath);
        Date imageDate = ImageUtils.getImageDateFromExif(exifInterface);
        if(imageDate != null) {
            updateLandmarkDate(imageDate);
        }
        Location imageLocation = ImageUtils.getImageLocationFromExif(exifInterface);
        if(imageLocation != null) {
            mLastLocation = imageLocation;
            String automaticLocationStr = LocationUtils.updateLmLocationString(getActivity(), mLastLocation);
            isRealAutomaticLocation = handleAutomaticLocationOptions(
                    lmAutomaticLocationTextView,
                    getLmAutomaticLocationErrorTextView,
                    mLastLocation,
                    automaticLocationStr
            );
        }
    }

    //---------------- Date functions ---------------//
    private void initDateAndTimePickerSettings(Date currentDate) {
        lmDatePicker = DateUtils.getDatePicker(getActivity(), currentDate, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = new GregorianCalendar();
                newDate.setTime(lmCurrentDate);
                newDate.set(year, monthOfYear, dayOfMonth);
                updateLandmarkDate(newDate.getTime());
            }
        });

        lmTimePicker = DateUtils.getTimePicker(getActivity(), currentDate, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar newDate = new GregorianCalendar();
                newDate.setTime(lmCurrentDate);
                newDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                newDate.set(Calendar.MINUTE, minute);
                updateLandmarkDate(newDate.getTime());
            }
        });
    }


    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (FragmentCompat.shouldShowRequestPermissionRationale(DestinationDetailsFragment.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.location_permission_title))
                        .setMessage(getString(R.string.location_permission_message))
                        .setPositiveButton(getString(R.string.location_permission_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                FragmentCompat.requestPermissions(DestinationDetailsFragment.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION_PERMISSION_ACTION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                FragmentCompat.requestPermissions(DestinationDetailsFragment.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION_ACTION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_ACTION: {
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        handleTakePhotoIntent();
                    }
                    else {
                        FragmentCompat.requestPermissions(DestinationDetailsFragment.this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                    }
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case REQUEST_READ_STORAGE_PERMISSION_ACTION: {
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    if(isCalledFromGallery){
                        handleLandmarkFromGallery();
                        return;
                    }
                    else if (isRequestedPermissionFromCamera) {
                        handleTakePhotoIntent();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION);
                    }
                } else {
                    if(isCalledFromGallery){
                        Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_destination_added_from_gallery_no_permission), Toast.LENGTH_SHORT).show();
                        getActivity().finishAffinity();
                    }
                    else {
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case REQUEST_LOCATION_PERMISSION_ACTION: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient != null) {
                            if(!isCalledFromUpdateLandmark){
                                CreateLocationRequest();
                            }else{
                                isMapClicked = false;
                                startGoogleMapIntent();
                            }
                        }
                        else{
                            handleLocationUpdateDone();
                        }
                    }
                } else {
                    handleLocationUpdateDone();
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void initDialogs() {
        String[] dialogOptionsArray = getResources().getStringArray(R.array.destination_details_photo_dialog_options);

        // Use the Builder class for convenient dialog construction
        optionsDialogBuilder = new AlertDialog.Builder(getActivity());
        optionsDialogBuilder.setTitle(R.string.destination_details_photo_dialog);
        optionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                PhotoDialogOptions photoDialogOptions = PhotoDialogOptions.values()[position];
                switch (photoDialogOptions){
                    case CHANGE_PICTURE:
                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            isRequestedPermissionFromCamera = false;
                            FragmentCompat.requestPermissions(DestinationDetailsFragment.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                        } else {
                            Intent takePictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(takePictureIntent, PICK_GALLERY_PHOTO_ACTION);
                        }
                        break;
                    case TAKE_PHOTO:
                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            isRequestedPermissionFromCamera = true;
                            FragmentCompat.requestPermissions(DestinationDetailsFragment.this,
                                    new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_ACTION);
                        } else {
                            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {

                                handleTakePhotoIntent();
                            } else {
                                FragmentCompat.requestPermissions(DestinationDetailsFragment.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                            }
                        }
                        break;
                }
            }
        });

    }

    public void handleTakePhotoIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = ImageUtils.createImageFile();
                newTakePhotoPath = photoFile.toString();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.tripper.tripper.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // grant permission to the camera to use the photoURI
                List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getActivity().grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                // open the camera
                startActivityForResult(takePictureIntent, TAKE_PHOTO_FROM_CAMERA_ACTION);
            }
        }
    }

    private void CreateLocationRequest(){
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                // if called from Create Destination and it's the first time
                if(!isCalledFromUpdateLandmark){
                    if (mLastLocation == null){
                        mLastLocation = location;
                    }
                    if (mLastLocation != null && !isRealAutomaticLocation){
                        createUpdateLocationTask();
                    }
                }
                handleLocationUpdateDone();
                if(isMapClicked){
                    isMapClicked = false;
                    startGoogleMapIntent();
                }
            }
        };
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // if not going in here, was supposed to be checked before reaching here, might be a problem
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
        }
        if(!LocationUtils.IsGpsEnabled(getActivity())){
            handleLocationUpdateDone();
            isRealAutomaticLocation = handleAutomaticLocationOptions(lmAutomaticLocationTextView,
                    getLmAutomaticLocationErrorTextView,
                    mLastLocation,
                    lmAutomaticLocationTextView.getText().toString());
            if(isMapClicked){
                isMapClicked = false;
                startGoogleMapIntent();
            }
        }
    }

    private void initNetworkListener(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive( Context context, Intent intent )
            {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetInfo != null && activeNetInfo.isConnectedOrConnecting())
                {
                    handleNetworkStartedEvent();
                }
            }
        };
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    private void handleNetworkStartedEvent(){
        if (mLastLocation != null && !isRealAutomaticLocation) {
            lmAutomaticLocationTextView.setText("");
            getLmAutomaticLocationErrorTextView.setVisibility(View.GONE);
            createUpdateLocationTask();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Once connected with google api, get the location
//        checkLocationPermission();
//        displayLocation();
        // if gps enabled, called from Create Destination and have gps permission
        Log.i(TAG, "onConnected");
        if(mLocationRequest == null         // LocationUtils.IsGpsEnabled(getActivity()) &&
                && !isCalledFromUpdateLandmark
                && ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            CreateLocationRequest();
        }else{
            handleLocationUpdateDone();
            isRealAutomaticLocation = handleAutomaticLocationOptions(
                    lmAutomaticLocationTextView,
                    getLmAutomaticLocationErrorTextView,
                    mLastLocation,
                    lmAutomaticLocationTextView.getText().toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mGoogleApiClient != null) {
            if(mLocationListener != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
            }
            mGoogleApiClient.disconnect();
        }
        clearAllTasks();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    private void clearAllTasks(){
        if(updateLocationTask != null){
            if(!updateLocationTask.isCancelled()) {
                updateLocationTask.cancel(true);
            }
            updateLocationTask = null;
        }
        if(r != null){
            handler.removeCallbacks(r);
        }
    }

    public String getRealAutomaticLocation(){
        return isRealAutomaticLocation? lmAutomaticLocationTextView.getText().toString().trim() : null;
    }

    private void startGoogleMapIntent(){
        Intent mapIntent = new Intent(getActivity(), SingleDestinationMap.class);
        Bundle gpsLocationBundle = new Bundle();
        Destination newDestination = new Destination(currentTrip.getId(), lmTitleEditText.getText().toString().trim(), currentLmPhotoPath, lmCurrentDate,
                getRealAutomaticLocation(), mLastLocation, lmLocationDescriptionEditText.getText().toString().trim(),
                lmDescriptionEditText.getText().toString().trim(), lmTypeSpinner.getSelectedItemPosition());
                ArrayList<Destination> destinationArray = new ArrayList(1);
        destinationArray.add(newDestination);
        gpsLocationBundle.putParcelableArrayList(DestinationMainActivity.LandmarkArrayList, destinationArray);
        mapIntent.putExtras(gpsLocationBundle);
        handleLocationUpdateDone();
        startActivityForResult(mapIntent, LANDMARK_SINGLE_MAP_INTENT_ACTION);
    }

//    /**
//     * Method to verify google play services on the device
//     * */
//    private boolean checkPlayServices() {
//        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
//        int result = googleAPI.isGooglePlayServicesAvailable(getActivity());
//        if(result != ConnectionResult.SUCCESS) {
//            if(googleAPI.isUserResolvableError(result)) {
//                googleAPI.getErrorDialog(getActivity(), result,
//                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
//            }
//
//            return false;
//        }
//        return true;
//    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("savedImagePath", currentLmPhotoPath);
        state.putBoolean("isCalledFromUpdateLandmark", isCalledFromUpdateLandmark);
        state.putBoolean(saveIsRequestedPermissionFromCamera, isRequestedPermissionFromCamera);
        state.putBoolean(saveIsCalledFromGallery, isCalledFromGallery);
        state.putBoolean(saveIsCalledFromNotification, isCalledFromNotification);
        state.putParcelable(saveFinalLandmark, finalDestination);
        state.putBoolean(saveIsRealAutomaticLocation, isRealAutomaticLocation);
        state.putParcelable(savemLastLocation, mLastLocation);
        state.putParcelable(saveCurrentTrip, currentTrip);
        state.putLong(saveLmCurrentDate, lmCurrentDate.getTime());
        state.putString(saveNewTakePhotoPath, newTakePhotoPath);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        mCallback = StartActivityUtils.onAttachCheckInterface(activity, OnGetCurrentDestination.class);
        mCallbackGetCurTrip = StartActivityUtils.onAttachCheckInterface(activity, OnGetCurrentTrip.class);
        mCallbackOnLandmarkAddedListener = StartActivityUtils.onAttachCheckInterface(activity, OnLandmarkAddedListener.class);
    }

    //--------------helper methods--------//
    private void updateLandmarkDate(Date newDate) {
        lmCurrentDate = newDate;
        lmDateEditText.setText(dateFormatter.format(lmCurrentDate));
        lmTimeEditText.setText(timeFormatter.format(lmCurrentDate));
    }

    private void updateTripEndDate(int tripId, Date newEndDate){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MyContentProvider.Trips.END_DATE_COLUMN, DateUtils.databaseDateToString(newEndDate));
        getActivity().getContentResolver().update
                (ContentUris.withAppendedId(MyContentProvider.CONTENT_TRIP_ID_URI_BASE, tripId), contentValues, null, null);
        currentTrip.setEndDate(newEndDate);
    }

    private void updateParentTripMessage(){
        if (currentTrip != null) {
            String message = getResources().getString(R.string.parent_trip_message) + " " + "<b>" + currentTrip.getTitle() + "</b>" + " trip";
            parentTripMessage.setText(Html.fromHtml(message));
            parentTripMessage.setVisibility(View.VISIBLE);
        }
    }
}
