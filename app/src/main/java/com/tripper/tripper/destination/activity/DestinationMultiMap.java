package com.tripper.tripper.destination.activity;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tripper.tripper.R;
import com.tripper.tripper.utils.ImageUtils;

import java.util.ArrayList;

public class DestinationMultiMap extends DestinationMap {

    private ArrayList<Marker> markers;
    private ArrayList<LatLng> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markers = new ArrayList<>();
        points = new ArrayList<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        int index = 0;
        LatLng landmarkLatLng = null;

        if (lmArrayList.isEmpty()){
            Toast.makeText(this, R.string.no_destinations_to_view, Toast.LENGTH_LONG).show();
            Toast.makeText(this, R.string.make_sure_gps_enabled_or_map, Toast.LENGTH_LONG).show();
            return;
        }

        Location currentLocation;
        do{
            currentLocation = lmArrayList.get(index).getGPSLocation();
            if(currentLocation == null){
                index ++;
                continue;
            }

            landmarkLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            // Add a marker in Destination
            int spinnerPosition = lmArrayList.get(index).getTypePosition();
            Marker marker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getBitmap(this, iconTypeArray.getResourceId(spinnerPosition, -1))))
                            .title(lmArrayList.get(index).getTitle())
                            .position(landmarkLatLng));

            markerToLmIndex.put(marker, index);

            markers.add(marker);
            points.add(landmarkLatLng);

            index ++;
        } while (index < lmArrayList.size());

        if(landmarkLatLng == null){
            Toast.makeText(this, R.string.no_destinations_with_location_found, Toast.LENGTH_LONG).show();
            Toast.makeText(this, R.string.make_sure_gps_enabled_or_map, Toast.LENGTH_LONG).show();
        }else {

            final CameraUpdate cu;
            if (markers.size() == 1) {
                cu = CameraUpdateFactory.newLatLngZoom(landmarkLatLng, 15);
            } else {
                mMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .width(5)
                        .color(R.color.accent));

                LatLngBounds bounds = getMarkersBound(markers);
                cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);

            }


            if (isFirstLoad) {
                mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        mMap.animateCamera(cu, 2000, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                isFirstLoad = false;
                            }

                            @Override
                            public void onCancel() {
                                isFirstLoad = false;
                            }
                        });
                        mMap.setOnCameraIdleListener(null);
                    }
                });
            }
        }
    }

    LatLngBounds getMarkersBound(ArrayList<Marker> markers){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        return builder.build();
    }
}
