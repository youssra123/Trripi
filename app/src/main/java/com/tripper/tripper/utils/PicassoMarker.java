package com.tripper.tripper.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public class PicassoMarker implements Target {
    Marker mMarker;
    ImageView mImageView;

    public PicassoMarker(Marker marker, ImageView imageView) {
        mMarker = marker;
        mImageView = imageView;
    }

    @Override
    public int hashCode() {
        return mMarker.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof PicassoMarker) {
            Marker marker = ((PicassoMarker) o).mMarker;
            return mMarker.equals(marker);
        } else {
            return false;
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        mImageView.setImageBitmap(bitmap);
        if (mMarker != null && mMarker.isInfoWindowShown()){
            mMarker.hideInfoWindow();
            mMarker.showInfoWindow();
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}