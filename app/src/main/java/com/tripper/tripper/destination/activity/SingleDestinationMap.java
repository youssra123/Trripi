package com.tripper.tripper.destination.activity;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tripper.tripper.R;
import com.tripper.tripper.models.Destination;
import com.tripper.tripper.utils.ImageUtils;
import com.tripper.tripper.utils.LocationUtils;

import java.util.Locale;

public class SingleDestinationMap extends DestinationMap {

    public static final String TAG = SingleDestinationMap.class.getSimpleName();
    private static final String SAVE_LANDMARK_LOCATION = "SAVE_LANDMARK_LOCATION";
    private static final String SAVE_LANDMARK_AUTOMATIC_LOCATION = "SAVE_LANDMARK_AUTOMATIC_LOCATION";

    private Destination lmCurrent;
    private int lmSpinnerPosition;
    private Location landmarkLocation;
    private String landmarkAutomaticLocation;
    private Intent resultIntent;
    private Geocoder gcd;
    private AsyncTask<Void, Void, String> updateLocationTask;
    private Button doneButton;
    private Button cancelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lmCurrent = lmArrayList.get(0);
        lmSpinnerPosition = lmCurrent.getTypePosition();
        resultIntent = new Intent();
        landmarkLocation = lmCurrent.getGPSLocation();
        gcd = new Geocoder(SingleDestinationMap.this, Locale.getDefault());

        if(savedInstanceState!= null){
            landmarkLocation = savedInstanceState.getParcelable(SAVE_LANDMARK_LOCATION);
            landmarkAutomaticLocation = savedInstanceState.getString(SAVE_LANDMARK_AUTOMATIC_LOCATION);
            resultIntent.putExtra(DestinationMainActivity.LandmarkNewGPSLocation, landmarkLocation);
            resultIntent.putExtra(DestinationMainActivity.LandmarkNewLocation, landmarkAutomaticLocation);
        }
        else {
        resultIntent.putExtra(DestinationMainActivity.LandmarkNewGPSLocation, lmCurrent.getGPSLocation());
        resultIntent.putExtra(DestinationMainActivity.LandmarkNewLocation, lmCurrent.getAutomaticLocation());
        }

       findViewsByIdAndSetListeners();

        setResult(RESULT_CANCELED, resultIntent);

//        resultIntent.putExtra(DestinationMainActivity.LandmarkNewGPSLocation, lmCurrent.getGPSLocation());
//        resultIntent.putExtra(DestinationMainActivity.LandmarkNewLocation, lmCurrent.getAutomaticLocation());
    }

    private void findViewsByIdAndSetListeners(){
        doneButton = findViewById(R.id.map_done_button);
        doneButton.setVisibility(View.VISIBLE);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        cancelButton = findViewById(R.id.map_cancel_button);
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, resultIntent);
                finish();
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        setListeners();

        if (landmarkLocation != null){
            // Create new LatLng
            LatLng landmarkLatLng = new LatLng(
                    landmarkLocation.getLatitude(),
                    landmarkLocation.getLongitude()
            );

            // add marker and update the marker/index dictionary
            addMarkerAndUpdateDict(landmarkLatLng);

            // Move Camera
            if (isFirstLoad) {
                mMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(landmarkLatLng, 15), 2000, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        isFirstLoad = false;
                    }

                    @Override
                    public void onCancel() {
                        isFirstLoad = false;
                    }
                });
            }
        }else{
            Toast.makeText(this, R.string.no_previous_location_mark_map, Toast.LENGTH_LONG).show();
        }


    }

    private void setListeners(){
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                setMarker(point, true);
            }
        });
    }

    private void setMarker(LatLng point, boolean isUpdateAutomaticLocation) {
        mMap.clear();

        // add marker and update the marker/index dictionary
        addMarkerAndUpdateDict(point);

        updateAddressLocation(point);

        if (isUpdateAutomaticLocation) {
            // save the new Destination GPS location and string location
            updateAutomaticLocation();
        }
    }

    private void updateAutomaticLocation() {
        createUpdateLocationTask();
    }


    public void setLandmarkAutomaticLocation(String landmarkAutomaticLocation) {
        resultIntent.putExtra(DestinationMainActivity.LandmarkNewLocation, landmarkAutomaticLocation);
        this.landmarkAutomaticLocation = landmarkAutomaticLocation;
    }

    private Marker addMarkerAndUpdateDict(LatLng point){
        Marker marker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getBitmap(this, iconTypeArray.getResourceId(lmSpinnerPosition, -1))))
                .title(lmCurrent.getTitle())
                .position(point));

        // Add 0 index to receive is from array
        markerToLmIndex.put(marker, 0);
        return marker;
    }

    private void updateAddressLocation(LatLng point){
        if(point != null){
            if(landmarkLocation == null){
                landmarkLocation = new Location("");
            }
            landmarkLocation.setLatitude(point.latitude);
            landmarkLocation.setLongitude(point.longitude);
        }
        resultIntent.putExtra(DestinationMainActivity.LandmarkNewGPSLocation, landmarkLocation);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable(SAVE_LANDMARK_LOCATION, landmarkLocation);
        state.putString(SAVE_LANDMARK_AUTOMATIC_LOCATION, landmarkAutomaticLocation);
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
        updateLocationTask = new AsyncTask<Void, Void, String>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setLandmarkAutomaticLocation(null);
            }

            @Override
            protected void onPostExecute(String stringResult) {
                super.onPostExecute(stringResult);
                setLandmarkAutomaticLocation(stringResult);
            }

            @Override
            protected String doInBackground(Void... params) {
                return LocationUtils.updateLmLocationString(SingleDestinationMap.this, landmarkLocation);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void cancelTask(){
        if(updateLocationTask != null){
            if(!updateLocationTask.isCancelled()) {
                updateLocationTask.cancel(true);
            }
            updateLocationTask = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelTask();
    }

    @Override
    public void onPlaceSelected(Place place) {
        super.onPlaceSelected(place);

        setMarker(place.getLatLng(), false);
        setLandmarkAutomaticLocation(place.getName().toString());
    }
}
