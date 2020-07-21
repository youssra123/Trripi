package com.tripper.tripper.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.tripper.tripper.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    public static File updatePhotoImageViewByPath(Context context, String imagePath, ImageView imageView){
        return updatePhotoImageViewByPath(context, imagePath, imageView, true);
    }

    public static File updatePhotoImageViewByPath(Context context, String imagePath, ImageView imageView, boolean isCenterCrop){
        File file = getFile(imagePath);
        updatePhotoImageViewByPath(context, file, imageView, isCenterCrop);
        return file;
    }

    public static File updatePhotoImageViewByPath(Context context, String imagePath, Target target, int width, int height){
        File file = getFile(imagePath);
        updatePhotoImageViewByPath(context, file, target, width, height);
        return file;
    }

    private static void updatePhotoImageViewByPath(Context context, File imageFile, ImageView imageView, boolean isCenterCrop){
        if (imageFile == null) {
            Picasso.with(context).cancelRequest(imageView);
        }

        RequestCreator creator = getRequestCreator(context, imageFile);
        if (isCenterCrop) {
            creator.centerCrop().fit().into(imageView);
        } else {
            creator.centerInside().fit().into(imageView);
        }
    }

    private static void updatePhotoImageViewByPath(Context context, File imageFile, Target target, int width, int height){
        if (imageFile == null) {
            Picasso.with(context).cancelRequest(target);
        }

        RequestCreator creator = getRequestCreator(context, imageFile);
        creator.resize(width, height).centerCrop().into(target);
    }

    public static boolean isPhotoExist(String imagePath) {
        if (imagePath == null) {
            return false;
        }

        File file = new File(imagePath);
        return file.exists();
    }

    private static File getFile(String imagePath) {
        File file = null;

        if(imagePath != null && !imagePath.trim().equals("")) {

            try {
                file = new File(imagePath);
            } catch (Exception e) {
                // ignore
            }
        }

        return file;
    }

    private static RequestCreator getRequestCreator(Context context, File imageFile) {
        RequestCreator creator;

        if (imageFile == null) {
            creator = Picasso.with(context).load(R.drawable.default_no_image);
        } else {
            if (imageFile.exists()) {
                creator = Picasso.with(context).load(imageFile).error(R.drawable.error_no_image);
            }
            else {
                creator = Picasso.with(context).load(R.drawable.error_no_image);
            }
        }

        return creator;
    }

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static Bitmap getBitmap(Context context, int drawableId){
        Drawable drawable = context.getResources().getDrawable(drawableId);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static ExifInterface getImageExif(String imagePath){
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(imagePath);
        } catch (Exception e) {
          }
        return  exifInterface;
    }

    public static Date getImageDateFromExif(ExifInterface exifInterface){
       Date imageDate = null;
        try {
            exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
            String imageDateStr = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            if(imageDateStr != null){
                imageDate = sdf.parse(imageDateStr);
            }
            else {
                String imageGpsDateStr = exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
                String imageGpsTimeStr = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
                if(imageGpsDateStr != null && imageGpsTimeStr != null){
                    imageDateStr = imageGpsDateStr + " " + imageGpsTimeStr;
                }
                imageDate = (imageDateStr != null) ? sdf.parse(imageDateStr) : null;
            }

        } catch (Exception e) {
            imageDate = null;
        }
        return imageDate;
    }

    public static Location getImageLocationFromExif(ExifInterface exifInterface) {
        Location imageLocation = null;
        try {
            float[] latLong = new float[2];
            boolean hasLatLong = exifInterface.getLatLong(latLong);
            if (hasLatLong) {
                imageLocation = new Location("");
                imageLocation.setLatitude(latLong[0]);
                imageLocation.setLongitude(latLong[1]);
            }
        } catch (Exception e) {
        }
        return imageLocation;
    }

    public static File createImageFile() throws IOException{
        String timeStamp = DateUtils.getImageTimeStampDateFormat().format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File directory = new File(Environment.getExternalStorageDirectory(), "KeepTrip");
        if (!directory.mkdirs()) {
            Log.e(ImageUtils.class.getSimpleName(), "Directory not created");
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                directory
        );

        return image;
    }

    public static void insertImageToGallery(Context context, String imagePath, Location currentLocation){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"KeepTrip");
        values.put(MediaStore.Images.Media.DESCRIPTION, "KeepTrip description");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        if(currentLocation != null){
            values.put(MediaStore.Images.Media.LATITUDE, currentLocation.getLatitude());
            values.put(MediaStore.Images.Media.LONGITUDE, currentLocation.getLongitude());
        }
        values.put("_data", imagePath);
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
