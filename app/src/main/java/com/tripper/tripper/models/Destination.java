package com.tripper.tripper.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.tripper.tripper.services.MyContentProvider;
import com.tripper.tripper.utils.DateUtils;

import java.util.Date;

public class Destination implements Parcelable {

    private String dateFormatString = "YYYY-MM-DDTHH:MM:SS.SSS";

    private static final int DEFAULT_ID = -1;

    private int id;
    private int tripId;
    private String title;
    private String photoPath; // TODO: check what is the image type
    private Date date;
    private String automaticLocation;
    private Location GPSLocation;
    private String locationDescription;
    private String description;
    private int typePosition; //TODO: change it to enum? where to define?

    public Destination(Cursor cursor){
        final int COLUMN_ID = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.ID_COLUMN);
        final int COLUMN_TRIP_ID = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.TRIP_ID_COLUMN);
        final int COLUMN_TITLE = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.TITLE_COLUMN);
        final int COLUMN_PHOTO_PATH = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.PHOTO_PATH_COLUMN);
        final int COLUMN_DATE = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.DATE_COLUMN);
        final int COLUMN_AUTOMATIC_LOCATION = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.AUTOMATIC_LOCATION_COLUMN);
        final int COLUMN_LOCATION_LATITUDE = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.LOCATION_LATITUDE_COLUMN);
        final int COLUMN_LOCATION_LONGITUDE = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.LOCATION_LONGITUDE_COLUMN);
        final int COLUMN_LOCATION_DESCRIPTION = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.LOCATION_DESCRIPTION_COLUMN);
        final int COLUMN_DESCRIPTION = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.DESCRIPTION_COLUMN);
        final int COLUMN_TYPE_POSITION = cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.TYPE_POSITION_COLUMN);

        id = cursor.getInt(COLUMN_ID);
        tripId = cursor.getInt(COLUMN_TRIP_ID);
        title = cursor.getString(COLUMN_TITLE);
        photoPath = cursor.getString(COLUMN_PHOTO_PATH);
        date = DateUtils.databaseStringToDate(cursor.getString(COLUMN_DATE));

        automaticLocation = cursor.getString(COLUMN_AUTOMATIC_LOCATION);

        if (!cursor.isNull(COLUMN_LOCATION_LATITUDE)){
            GPSLocation = new Location("");
            double latitude = cursor.getDouble(COLUMN_LOCATION_LATITUDE);
            GPSLocation.setLatitude(latitude);
        }
        if (!cursor.isNull(COLUMN_LOCATION_LONGITUDE)){
            double longitude = cursor.getDouble(COLUMN_LOCATION_LONGITUDE);
            GPSLocation.setLongitude(longitude);
        }

        locationDescription = cursor.getString(COLUMN_LOCATION_DESCRIPTION);

        description = cursor.getString(COLUMN_DESCRIPTION);

        typePosition = cursor.getInt(COLUMN_TYPE_POSITION);
    }

    public Destination(int tripId, String title, String photoPath, Date date, String automaticLocation, Location GPSLocation, String locationDescription, String description, int typePosition){
        this(DEFAULT_ID, tripId, title, photoPath, date, automaticLocation, GPSLocation, locationDescription, description, typePosition);
    }

    public Destination(int id, int tripId, String title, String photoPath, Date date, String automaticLocation, Location GPSLocation, String locationDescription, String description, int typePosition){
        this.id = id;
        this.tripId = tripId;
        this.title = title;
        this.photoPath = photoPath;
        this.date = date;
        this.automaticLocation = automaticLocation;
        this.GPSLocation = GPSLocation;
        this.locationDescription = locationDescription;
        this.description = description;
        this.typePosition = typePosition;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAutomaticLocation() {
        return automaticLocation;
    }

    public void setAutomaticLocation(String automaticLocation) {
        this.automaticLocation = automaticLocation;
    }

    public Location getGPSLocation() {
        return GPSLocation;
    }

    public void setGPSLocation(Location GPSLocation) {
        this.GPSLocation = GPSLocation;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTypePosition() {
        return typePosition;
    }

    public void setTypePosition(int typePosition) {
        this.typePosition = typePosition;
    }

    public ContentValues landmarkToContentValues(){

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyContentProvider.Destinations.TITLE_COLUMN, title);
        contentValues.put(MyContentProvider.Destinations.TRIP_ID_COLUMN, tripId);
        contentValues.put(MyContentProvider.Destinations.DATE_COLUMN, DateUtils.databaseDateToString(date));
        contentValues.put(MyContentProvider.Destinations.TITLE_COLUMN, title);
        contentValues.put(MyContentProvider.Destinations.AUTOMATIC_LOCATION_COLUMN, automaticLocation);
        if (GPSLocation != null){
            contentValues.put(MyContentProvider.Destinations.LOCATION_LATITUDE_COLUMN, GPSLocation.getLatitude());
            contentValues.put(MyContentProvider.Destinations.LOCATION_LONGITUDE_COLUMN, GPSLocation.getLongitude());
        }
        contentValues.put(MyContentProvider.Destinations.LOCATION_DESCRIPTION_COLUMN, locationDescription);
        contentValues.put(MyContentProvider.Destinations.PHOTO_PATH_COLUMN, photoPath);
        contentValues.put(MyContentProvider.Destinations.DESCRIPTION_COLUMN, description);
        contentValues.put(MyContentProvider.Destinations.TYPE_POSITION_COLUMN, typePosition);

        return contentValues;
    }

    protected Destination(Parcel in) {
        id = in.readInt();
        tripId = in.readInt();
        title = in.readString();
        photoPath = in.readString();
        long tmpDate = in.readLong();
        date = tmpDate != -1 ? new Date(tmpDate) : null;
        automaticLocation = in.readString();
        GPSLocation = (Location) in.readValue(Location.class.getClassLoader());
        locationDescription = in.readString();
        description = in.readString();
        typePosition = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(tripId);
        dest.writeString(title);
        dest.writeString(photoPath);
        dest.writeLong(date != null ? date.getTime() : -1L);
        dest.writeString(automaticLocation);
        dest.writeValue(GPSLocation);
        dest.writeString(locationDescription);
        dest.writeString(description);
        dest.writeInt(typePosition);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Destination> CREATOR = new Parcelable.Creator<Destination>() {
        @Override
        public Destination createFromParcel(Parcel in) {
            return new Destination(in);
        }

        @Override
        public Destination[] newArray(int size) {
            return new Destination[size];
        }
    };
}