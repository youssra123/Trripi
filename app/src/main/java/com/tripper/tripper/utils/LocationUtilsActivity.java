package com.tripper.tripper.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tripper.tripper.R;

public class LocationUtilsActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    // Defines
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public static final int REQUEST_LOCATION_PERMISSION_ACTION = 2;
    private GoogleApiClient mGoogleApiClient;

    public static String CURRENT_LOCATION_RESULT = "CURRENT_LOCATION_RESULT";
    public static final String TAG = com.tripper.tripper.utils.LocationUtilsActivity.class.getSimpleName();

    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;
    private Location mLastLocation;

//    private ProgressBar spinner;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_utils);
//            spinner = (ProgressBar) findViewById(R.id.getting_location_progress_bar);
        initProgressDialog();
        handleCurrentLocation();

    }

    private void initProgressDialog(){
        progressDialog = new ProgressDialog(this);

        // Set progress dialog style spinner
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // Set the progress dialog title and message
        progressDialog.setTitle(getResources().getString(R.string.toast_widget_location_utils_open_title));
        progressDialog.setMessage(getResources().getString(R.string.toast_widget_location_utils_open_massage));

        // Set the progress dialog background color
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progressDialog.setIndeterminate(false);

        progressDialog.setCancelable(false);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.toast_widget_location_cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                returnCurrentLocation(null);
            }
        });
    }
    private void handleCurrentLocation(){
//        locationUtilsInstance = new LocationUtils();

        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!(LocationUtils.IsGpsEnabled(this))){
            Toast.makeText(this, getResources().getString(R.string.toast_location_is_off_massage), Toast.LENGTH_LONG).show();
            finishAffinity();
            return;
        }
        // check if supporting google api at the moment
        if (checkPlayServices()) {
            // if connected and already created location updates
            buildGoogleApiClient();
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        CreateLocationRequest();
                    } else {
                        checkLocationPermission();
                    }
                }
            }
        }
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                Dialog dialog = googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
                if (dialog != null) {
                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            finishAffinity();
                        }
                    });

                    return false;
                }
            }

            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            handleLocationPermissions();
        }
        else {
            if (mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
                CreateLocationRequest();
            }
        }
    }

    private void handleLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            createAndShowLocationPermissionsDialog();
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION_ACTION);
        }
    }

    private void createAndShowLocationPermissionsDialog() {

        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.location_permission_title))
                .setMessage(getString(R.string.location_permission_message))
                .setPositiveButton(getString(R.string.location_permission_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(LocationUtilsActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_LOCATION_PERMISSION_ACTION);
                    }
                }).create();
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(LocationUtilsActivity.this, getResources().getString(R.string.permission_denied_massage), Toast.LENGTH_LONG).show();
                finishAffinity();
            }
        });
        alertDialog.show();


    }


    //---------implement google api interfaces--------------//


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connection success");
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            CreateLocationRequest();
        }
        else {
            handleLocationPermissions();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        Toast.makeText(this, getResources().getString(R.string.toast_something_went_wrong), Toast.LENGTH_SHORT).show();
        finishAffinity();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLAY_SERVICES_RESOLUTION_REQUEST:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Play Services must be installed.",
                            Toast.LENGTH_SHORT).show();
                }
                finishAffinity();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION_ACTION: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient.isConnected()) {
                            CreateLocationRequest();
                        }
                    }
                    else {
                        finishAffinity();
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, getResources().getString(R.string.permission_denied_massage), Toast.LENGTH_LONG).show();
                    finishAffinity();
                }

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void returnCurrentLocation(Location location){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(CURRENT_LOCATION_RESULT, location);
        if(location != null) {
            setResult(Activity.RESULT_OK, returnIntent);
        }
        else {
            setResult(Activity.RESULT_CANCELED, returnIntent);

        }
//        spinner.setVisibility(View.GONE);
        progressDialog.dismiss();
        finish();
    }
    private void getCurrentLocation(){
        Location currentLocation = null;
        try {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            mGoogleApiClient.disconnect();

            Intent returnIntent = new Intent();
            returnIntent.putExtra(CURRENT_LOCATION_RESULT, currentLocation);
            if(currentLocation != null) {
                setResult(Activity.RESULT_OK, returnIntent);
            }
            else {
                setResult(Activity.RESULT_CANCELED, returnIntent);

            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        finish();
    }

    private void CreateLocationRequest(){
//        Toast.makeText(this, getResources().getString(R.string.toast_widget_location_open_massage), Toast.LENGTH_LONG).show();
//        spinner.setVisibility(View.VISIBLE);
        progressDialog.show();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // if called from Create Destination and it's the first time
                    // if location permission is enabled
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if (mLastLocation == null){
                            mLastLocation = location;
                        }
                        returnCurrentLocation(location);
                }
            }
        };
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}



//package com.tripper.tripper.utils;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.location.LocationManager;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v13.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationServices;
//import com.tripper.tripper.R;
//
//public class LocationUtilsActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
//    // Defines
//    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
//    public static final int REQUEST_LOCATION_PERMISSION_ACTION = 2;
//    private GoogleApiClient mGoogleApiClient;
//
//    public static String CURRENT_LOCATION_RESULT = "CURRENT_LOCATION_RESULT";
//    public static final String TAG = com.tripper.tripper.utils.LocationUtilsActivity.class.getSimpleName();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_location_utils);
//
//        handleCurrentLocation();
//    }
//
//    private void handleCurrentLocation(){
////        locationUtilsInstance = new LocationUtils();
//
//        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            Toast.makeText(this, getResources().getString(R.string.toast_location_is_off_massage), Toast.LENGTH_LONG).show();
//            finishAffinity();
//        }
//            // check if supporting google api at the moment
//        if (checkPlayServices()) {
//            buildGoogleApiClient();
//            if (mGoogleApiClient != null) {
//                    checkLocationPermission();
//            }
//        }
//    }
//
//    /**
//     * Method to verify google play services on the device
//     */
//    private boolean checkPlayServices() {
//        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
//        int result = googleAPI.isGooglePlayServicesAvailable(this);
//        if (result != ConnectionResult.SUCCESS) {
//            if (googleAPI.isUserResolvableError(result)) {
//                Dialog dialog = googleAPI.getErrorDialog(this, result,
//                        PLAY_SERVICES_RESOLUTION_REQUEST);
//                        if (dialog != null) {
//                            dialog.show();
//                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                public void onDismiss(DialogInterface dialog) {
//                                   finishAffinity();
//                                }
//                            });
//
//                            return false;
//                        }
//            }
//
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * Creating google api client object
//     */
//    protected synchronized void buildGoogleApiClient() {
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//        }
//        mGoogleApiClient.connect();
//    }
//
//    private void checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//                handleLocationPermissions();
//        }
////        else {
////            getCurrentLocation();
////        }
//    }
//
//    private void handleLocationPermissions() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//            createAndShowLocationPermissionsDialog();
//        } else {
//            // No explanation needed, we can request the permission.
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_LOCATION_PERMISSION_ACTION);
//        }
//    }
//
//    private void createAndShowLocationPermissionsDialog() {
//
//        // Show an explanation to the user *asynchronously* -- don't block
//        // this thread waiting for the user's response! After the user
//        // sees the explanation, try again to request the permission.
//        new AlertDialog.Builder(this)
//                .setTitle(getString(R.string.location_permission_title))
//                .setMessage(getString(R.string.location_permission_message))
//                .setPositiveButton(getString(R.string.location_permission_ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        //Prompt the user once explanation has been shown
//                        ActivityCompat.requestPermissions(LocationUtilsActivity.this,
//                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                                REQUEST_LOCATION_PERMISSION_ACTION);
//                    }
//                })
//                .create()
//                .show();
//
//    }
//
//
//    //---------implement google api interfaces--------------//
//
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        Log.i(TAG, "Connection success");
//        getCurrentLocation();
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
//                + connectionResult.getErrorCode());
//
//        Toast.makeText(this, getResources().getString(R.string.toast_something_went_wrong), Toast.LENGTH_SHORT).show();
//        finishAffinity();
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case PLAY_SERVICES_RESOLUTION_REQUEST:
//                if (resultCode == RESULT_CANCELED) {
//                    Toast.makeText(this, "Google Play Services must be installed.",
//                            Toast.LENGTH_SHORT).show();
//                }
//                finishAffinity();
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode){
//            case REQUEST_LOCATION_PERMISSION_ACTION: {
//
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // location-related task you need to do.
//                    if (ContextCompat.checkSelfPermission(this,
//                            android.Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED) {
////                        if (mGoogleApiClient != null) {
////                             getCurrentLocation();
////                        }
//                    }
//                    else {
//                        finishAffinity();
//                    }
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    Toast.makeText(this, getResources().getString(R.string.permission_denied_massage), Toast.LENGTH_LONG).show();
//                    finishAffinity();
//                }
//
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//
//    private void getCurrentLocation(){
//        Location currentLocation = null;
//        try {
//            currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//            mGoogleApiClient.disconnect();
//
//            Intent returnIntent = new Intent();
//            returnIntent.putExtra(CURRENT_LOCATION_RESULT, currentLocation);
//            if(currentLocation != null) {
//                setResult(Activity.RESULT_OK, returnIntent);
//            }
//            else {
//                setResult(Activity.RESULT_CANCELED, returnIntent);
//
//            }
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//        finish();
//    }
//}
//
