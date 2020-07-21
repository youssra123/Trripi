package com.tripper.tripper.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.tripper.tripper.models.Trip;
import com.tripper.tripper.services.MyContentProvider;

public class DatabaseUtils {

    public static int addNewTrip(Activity activity, Trip newTrip){
        ContentValues contentValues = newTrip.tripToContentValues();
        Uri uri = activity.getContentResolver().insert(MyContentProvider.CONTENT_TRIPS_URI, contentValues);
        return Integer.parseInt(uri.getPathSegments().get(MyContentProvider.TRIPS_ID_PATH_POSITION));
    }

    public static Trip getLastTrip(Context context){
        Trip lastTrip = null;
        Cursor cursor = context.getContentResolver().query(MyContentProvider.CONTENT_TRIPS_URI, null, null,
                null, " LIMIT 1");
        if(cursor != null && cursor.moveToFirst()) {
            lastTrip = new Trip(cursor);
            cursor.close();
        }
        return lastTrip;
    }

    public static String getWhereClause(String[] columns) {
        String whereClause = "";

        for (String col : columns) {
            if (!whereClause.isEmpty()) {
                whereClause += " OR ";
            }

            whereClause += col + " like ? ";
        }

        return whereClause;
    }
}
