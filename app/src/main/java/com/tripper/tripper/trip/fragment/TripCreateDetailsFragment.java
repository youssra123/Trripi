package com.tripper.tripper.trip.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tripper.tripper.R;
import com.tripper.tripper.dialogs.DescriptionDialogFragment;
import com.tripper.tripper.models.Trip;
import com.tripper.tripper.trip.activity.CreateTripActivity;
import com.tripper.tripper.utils.DatabaseUtils;
import com.tripper.tripper.utils.ImageUtils;
import com.tripper.tripper.utils.NotificationUtils;
import com.tripper.tripper.utils.SharedPreferencesUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_OK;



public class TripCreateDetailsFragment extends Fragment {

    // tag
    public static final String TAG = TripCreateDetailsFragment.class.getSimpleName();

   //photo defines
    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int TAKE_PHOTO_FROM_CAMERA_ACTION = 2;
    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 4;
    private static final int REQUEST_CAMERA_PERMISSION_ACTION = 3;
    static final int DESCRIPTION_DIALOG = 1;

    //photo handle
    private Uri photoURI;
    private boolean isRequestedPermissionFromCamera;
    private String saveIsRequestedPermissionFromCamera = "saveIsRequestedPermissionFromCamera";
    private String SAVE_NEW_TAKE_PHOTO_PATH = "SAVE_NEW_TAKE_PHOTO_PATH";

    private View tripCreateDetailsView;
    private Activity tripCreateParentActivity;
    private ImageView tripPhotoImageView;
    private FloatingActionButton tripDoneFloatingActionButton;
    private EditText tripPlaceEditText;
    private EditText tripDescriptionEditText;
    private String tripPhotoPath;
    private String newTakePhotoPath;

    private AlertDialog.Builder photoOptionsDialogBuilder;
    public static final String initDescription = "initDescription";


    // Trip Photo Dialog Options
    public enum PhotoDialogOptions{
        CHANGE_PICTURE,
        TAKE_PHOTO
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            isRequestedPermissionFromCamera = savedInstanceState.getBoolean(saveIsRequestedPermissionFromCamera);
            newTakePhotoPath = savedInstanceState.getString(SAVE_NEW_TAKE_PHOTO_PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateDetailsView = inflater.inflate(R.layout.fragment_trip_create_details, container, false);
        tripCreateParentActivity = getActivity();

        findViewsById();

        // init the details fragment dialogs
        initDialogs();

        Trip currentTrip = ((CreateTripActivity)tripCreateParentActivity).currentCreatedTrip;
        if(currentTrip != null){
            tripPlaceEditText.setText(currentTrip.getPlace());
            tripPhotoPath = currentTrip.getPicture();
            ImageUtils.updatePhotoImageViewByPath(tripCreateParentActivity, tripPhotoPath, tripPhotoImageView);

            tripDescriptionEditText.setText(currentTrip.getDescription());
        }

        setListeners();
        return tripCreateDetailsView;
    }


    private void findViewsById(){
        tripDoneFloatingActionButton = tripCreateDetailsView.findViewById(R.id.trip_create_details_done_floating_action_button);
        tripPhotoImageView = tripCreateDetailsView.findViewById(R.id.trip_create_details_photo_image_view);
        //tripReturnFloatingActionButton = (FloatingActionButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_return_floating_action_button);
        tripPlaceEditText = tripCreateDetailsView.findViewById(R.id.trip_create_details_place_edit_text);
        tripDescriptionEditText = tripCreateDetailsView.findViewById(R.id.trip_create_details_description_edit_text);
    }

    private void setListeners(){
        // Done Button Listener
        tripDoneFloatingActionButton.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Trip currentTrip = ((CreateTripActivity)tripCreateParentActivity).currentCreatedTrip;
            Trip newTrip = new Trip(currentTrip.getTitle().trim(), currentTrip.getStartDate(), tripPlaceEditText.getText().toString().trim(), tripPhotoPath, tripDescriptionEditText.getText().toString().trim());

            int tripId = DatabaseUtils.addNewTrip(getActivity(), newTrip);

            //TODO: MAKE SURE IT'S O.K
            newTrip.setId(tripId);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(TripsListFragment.NEW_CREATED_TRIP, newTrip);

            tripCreateParentActivity.setResult(RESULT_OK, resultIntent);
            Toast.makeText(getActivity(), getResources().getString(R.string.toast_trip_added_message), Toast.LENGTH_LONG).show();

            // update the notification with new title only if its the last trip
            Trip latestTrip = DatabaseUtils.getLastTrip(getActivity());
            if( (latestTrip != null && latestTrip.getId() == tripId)) {
                //a new trip is created, so reopen the quick Destination option
                SharedPreferencesUtils.saveCloseNotificationsState(getActivity(), false);

                if (NotificationUtils.areNotificationsEnabled(getActivity())) {
                    NotificationUtils.initNotification(getActivity(), newTrip.getTitle());
                }
            }

            tripCreateParentActivity.finish();

        }
    });


        tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoOptionsDialogBuilder.show();
            }
        });

        // trip place Listener
        tripPlaceEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                ((CreateTripActivity)tripCreateParentActivity).currentCreatedTrip.setPlace(s.toString());
            }
        });

        tripDescriptionEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //   popUpDescriptionTextEditor();
                DialogFragment descriptionDialog = new DescriptionDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString(initDescription, tripDescriptionEditText.getText().toString());
                descriptionDialog.setArguments(bundle);
                descriptionDialog.setTargetFragment(TripCreateDetailsFragment.this, DESCRIPTION_DIALOG);
                descriptionDialog.show(getFragmentManager(), "Description");

            }
        });
    }

    private void initDialogs() {
        String[] dialogOptionsArray = getResources().getStringArray(R.array.trip_photo_dialog_options);
        photoOptionsDialogBuilder = new AlertDialog.Builder(getActivity());
        photoOptionsDialogBuilder.setTitle(R.string.trip_photo_dialog);
        photoOptionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                PhotoDialogOptions photoDialogOptions = PhotoDialogOptions.values()[position];
                switch (photoDialogOptions){
                    case CHANGE_PICTURE:
                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            isRequestedPermissionFromCamera = false;
                            FragmentCompat.requestPermissions(TripCreateDetailsFragment.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                        } else {
                            Intent takePictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(takePictureIntent, PICK_GALLERY_PHOTO_ACTION);
                        }
                        break;
                    case TAKE_PHOTO:
                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            isRequestedPermissionFromCamera = true;
                            FragmentCompat.requestPermissions(TripCreateDetailsFragment.this,
                                    new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_ACTION);
                        } else {
                            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {
                                handleTakePhotoIntent();
                            } else {
                                FragmentCompat.requestPermissions(TripCreateDetailsFragment.this,
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
            File photoFile = null;
            try {
                photoFile = ImageUtils.createImageFile();
                newTakePhotoPath = photoFile.toString();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.tripper.tripper.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICK_GALLERY_PHOTO_ACTION:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(imageUri, filePath, null, null, null);
                    try {
                        cursor.moveToFirst();

                        updateTripPhotoPath(cursor.getString(cursor.getColumnIndex(filePath[0])));
                        ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
                    } catch(NullPointerException e) {
                        Log.wtf(TAG, "cursor.moveToFirst() is null :(");
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                break;
            case TAKE_PHOTO_FROM_CAMERA_ACTION:
                if (resultCode == Activity.RESULT_OK) {
                    updateTripPhotoPath(newTakePhotoPath);
                    try {
                        ImageUtils.insertImageToGallery(getActivity(),tripPhotoPath, null);

                        ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    newTakePhotoPath = null;
                    if (resultCode != Activity.RESULT_CANCELED) {
                        Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case DESCRIPTION_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    DescriptionDialogFragment.DialogOptions whichOptionEnum = (DescriptionDialogFragment.DialogOptions) data.getSerializableExtra(DescriptionDialogFragment.DESCRIPTION_DIALOG_OPTION);
                    switch (whichOptionEnum) {
                        case DONE:
                            String currentDescription =  data.getStringExtra(DescriptionDialogFragment.DESCRIPTION_FROM_DIALOG);
                            tripDescriptionEditText.setText(currentDescription);
                            ((CreateTripActivity)tripCreateParentActivity).currentCreatedTrip.setDescription(currentDescription);
                            break;
                        case CANCEL:
                            break;
                    }
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_ACTION: {
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        handleTakePhotoIntent();
                    } else {
                        FragmentCompat.requestPermissions(TripCreateDetailsFragment.this,
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
                    if (isRequestedPermissionFromCamera) {
                        handleTakePhotoIntent();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION);
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void updateTripPhotoPath(String photoPath){
        tripPhotoPath = photoPath;
        ((CreateTripActivity)tripCreateParentActivity).currentCreatedTrip.setPicture(tripPhotoPath);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean(saveIsRequestedPermissionFromCamera, isRequestedPermissionFromCamera);
        state.putString(SAVE_NEW_TAKE_PHOTO_PATH, newTakePhotoPath);
    }



}