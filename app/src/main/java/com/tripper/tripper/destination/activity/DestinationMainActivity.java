package com.tripper.tripper.destination.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tripper.tripper.R;
import com.tripper.tripper.destination.fragment.DestinationDetailsFragment;
import com.tripper.tripper.destination.fragment.DestinationListFragment;
import com.tripper.tripper.destination.interfaces.OnGetCurrentDestination;
import com.tripper.tripper.destination.interfaces.OnGetCurrentTripID;
import com.tripper.tripper.dialogs.ChangesNotSavedDialogFragment;
import com.tripper.tripper.models.Destination;
import com.tripper.tripper.models.Trip;
import com.tripper.tripper.trip.fragment.TripUpdateFragment;
import com.tripper.tripper.trip.interfaces.OnGetCurrentTrip;
import com.tripper.tripper.utils.ImageUtils;
import com.tripper.tripper.utils.NotificationUtils;
import com.tripper.tripper.utils.StartActivityUtils;

public class DestinationMainActivity extends AppCompatActivity implements OnGetCurrentTripID,
        OnGetCurrentDestination, OnGetCurrentTrip, DestinationListFragment.OnSetCurrentDestination, DestinationListFragment.GetCurrentTripTitle,
        DestinationListFragment.OnGetIsDestinationAdded, DestinationDetailsFragment.OnLandmarkAddedListener,
        ChangesNotSavedDialogFragment.OnHandleDialogResult, DestinationListFragment.OnGetMoveToDestinationID {

    public static final String TAG = DestinationMainActivity.class.getSimpleName();

    public static final String CURRENT_TRIP_PARAM = "CURRENT_TRIP_PARAM";
    public static final String CURRENT_LANDMARK_ID_PARAM = "CURRENT_LANDMARK_ID_PARAM";

    private static final String SAVE_TRIP = "SAVE_TRIP";
    private static final String SAVE_LANDMARK = "SAVE_LANDMARK";
    private static final String SAVE_IS_LANDMARK_ADDED = "SAVE_IS_LANDMARK_ADDED";
    public Destination currentDestination;
    private Trip currentTrip;
    private int moveToLandmarkId;
    private boolean isLandmarkAdded;

    private String imageFromGalleryPath;
    public static final String IMAGE_FROM_GALLERY_PATH = "IMAGE_FROM_GALLERY_PATH";
    public static final String LandmarkNewLocation = "LandmarkNewLocation";
    public static final String LandmarkNewGPSLocation = "LandmarkNewGPSLocation";
    public static final String LandmarkArrayList ="LandmarkArrayList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_main);

        if (savedInstanceState != null){
            currentDestination = savedInstanceState.getParcelable(SAVE_LANDMARK);
            currentTrip = savedInstanceState.getParcelable(SAVE_TRIP);
            isLandmarkAdded = savedInstanceState.getBoolean(SAVE_IS_LANDMARK_ADDED);
        }

        Toolbar myToolbar = findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        // Get action and MIME type
        String action = intent.getAction();
        String type = intent.getType();

        if(action != null && action.equals(NotificationUtils.NOTIFICATION_ADD_LANDMARK_ACTION_STR)){
            handleNotificationAction();
        }
        else {
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    handleSentImage(intent); // Handle single image being sent
                }
            } else {
                currentTrip = intent.getParcelableExtra(CURRENT_TRIP_PARAM);
                moveToLandmarkId = intent.getIntExtra(CURRENT_LANDMARK_ID_PARAM, StartActivityUtils.NOT_JUMP_TO_LANDMARK_ID);

                if (findViewById(R.id.landmark_main_fragment_container) != null) {
                    if (getFragmentManager().findFragmentById(R.id.landmark_main_fragment_container) == null) {
                        DestinationListFragment fragment = new DestinationListFragment();
                        getFragmentManager()
                                .beginTransaction()
                                .add(R.id.landmark_main_fragment_container, fragment, DestinationListFragment.TAG)
                                .commit();
                    }
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_LANDMARK, currentDestination);
        outState.putParcelable(SAVE_TRIP, currentTrip);
        outState.putBoolean(SAVE_IS_LANDMARK_ADDED, isLandmarkAdded);
    }

    //----------add Destination from gallery------------//
    private void handleSentImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            imageFromGalleryPath = ImageUtils.getRealPathFromURI(this, imageUri);
            if (findViewById(R.id.landmark_main_fragment_container) != null) {
                if (getFragmentManager().findFragmentById(R.id.landmark_main_fragment_container) == null)
                {
                    DestinationDetailsFragment fragment = new DestinationDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(IMAGE_FROM_GALLERY_PATH, imageFromGalleryPath);
                    fragment.setArguments(bundle);
                    getFragmentManager()
                            .beginTransaction()
                            .add(R.id.landmark_main_fragment_container, fragment, DestinationDetailsFragment.TAG)
                            .commit();
                }
            }
        }
    }

    //----------add Destination from gallery------------//
    private void handleNotificationAction() {
        if (findViewById(R.id.landmark_main_fragment_container) != null) {
            if (getFragmentManager().findFragmentById(R.id.landmark_main_fragment_container) == null) {
                DestinationDetailsFragment fragment = new DestinationDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString(NotificationUtils.NOTIFICATION_ADD_LANDMARK_ACTION_STR, NotificationUtils.NOTIFICATION_ADD_LANDMARK_ACTION_STR);
                fragment.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.landmark_main_fragment_container, fragment, DestinationDetailsFragment.TAG)
                        .commit();
            }
        }
    }

    @Override
    public void onSetCurrentLandmark(Destination destination) {
        currentDestination = destination;
    }

    @Override
    public int onGetCurrentTripID() {
        return currentTrip.getId();
    }

    @Override
    public Destination onGetCurrentDestination() {
        return currentDestination;
    }

    @Override
    public String getCurrentTripTitle() {
        return currentTrip.getTitle();
    }

    @Override
    public Trip onGetCurrentTrip() {
        return currentTrip;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean getIsLandmarkAdded() {
        boolean res = isLandmarkAdded;
        isLandmarkAdded = false;
        return res;
    }

    @Override
    public void onLandmarkAdded() {
        isLandmarkAdded = true;
    }

    @Override
    public int onGetMoveToLandmarkId() {
        int res = moveToLandmarkId;
        moveToLandmarkId = StartActivityUtils.NOT_JUMP_TO_LANDMARK_ID;
        return res;
    }

    @Override
    public void onBackPressed() {
        DestinationDetailsFragment destinationDetailsFragment = (DestinationDetailsFragment)getFragmentManager().findFragmentByTag(DestinationDetailsFragment.TAG);
        TripUpdateFragment tripUpdateFragment = (TripUpdateFragment)getFragmentManager().findFragmentByTag(TripUpdateFragment.TAG);
        if (destinationDetailsFragment != null && destinationDetailsFragment.isVisible()) {
            ChangesNotSavedDialogFragment notSavedDialog = new ChangesNotSavedDialogFragment();
            notSavedDialog.setTargetFragment(destinationDetailsFragment, ChangesNotSavedDialogFragment.NOT_SAVED_DIALOG);
            notSavedDialog.show(getFragmentManager(), "Not_saved_dialog");
        }
        else{
            if(tripUpdateFragment != null && tripUpdateFragment.isVisible()){
                ChangesNotSavedDialogFragment notSavedDialog = new ChangesNotSavedDialogFragment();
                notSavedDialog.setTargetFragment(tripUpdateFragment, ChangesNotSavedDialogFragment.NOT_SAVED_DIALOG);
                notSavedDialog.show(getFragmentManager(), "Not_saved_dialog");
            }
            else{
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onHandleDialogResult(int whichButton) {
        ChangesNotSavedDialogFragment.DialogOptions whichOptionEnum = ChangesNotSavedDialogFragment.DialogOptions.values()[whichButton];
        switch (whichOptionEnum){
            case YES:
                super.onBackPressed();
                break;
            case NO:
                break;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}