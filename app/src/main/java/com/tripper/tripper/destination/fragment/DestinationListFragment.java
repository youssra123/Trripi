package com.tripper.tripper.destination.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tripper.tripper.R;
import com.tripper.tripper.destination.activity.DestinationMainActivity;
import com.tripper.tripper.destination.activity.DestinationMultiMap;
import com.tripper.tripper.destination.adapter.ListRowDestinationAdapter;
import com.tripper.tripper.destination.interfaces.OnGetCurrentTripID;
import com.tripper.tripper.models.Destination;
import com.tripper.tripper.models.Trip;
import com.tripper.tripper.services.MyContentProvider;
import com.tripper.tripper.trip.fragment.TripViewDetailsFragment;
import com.tripper.tripper.utils.AnimationUtils;
import com.tripper.tripper.utils.DatabaseUtils;
import com.tripper.tripper.utils.LocationUtils;
import com.tripper.tripper.utils.NotificationUtils;
import com.tripper.tripper.utils.SharedPreferencesUtils;
import com.tripper.tripper.utils.StartActivityUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class DestinationListFragment extends Fragment implements ListRowDestinationAdapter.OnFilterPublishResults,
        ListRowDestinationAdapter.OnOpenLandmarkDetailsForUpdate, ListRowDestinationAdapter.OnActionItemPress,
        ListRowDestinationAdapter.OnGetSelectedLandmarkMap{

    // tag
    public static final String TAG = DestinationListFragment.class.getSimpleName();

    private OnGetCurrentTripID mCallbackGetCurrentTripId;
    static final int DESTINATION_DIALOG = 0;
    static final String DESTINATION_DIALOG_OPTION = "DESTINATION_DIALOG_OPTION";
    private GetCurrentTripTitle mCallbackGetCurrentTripTitle;
    static final int DESTINATION_LOADER_ID = 0;
    AlertDialog destinationDeleteDialogConfirm;
    AlertDialog multipleDestinationDeleteDialogConfirm;
    private OnSetCurrentDestination mSetCurrentDestinationCallback;

    private Destination currentDestination;
    private OnGetIsDestinationAdded mCallbackGetIsDestinationAdded;
    private OnGetMoveToDestinationID mCallbackGetMoveToDestinationId;
    LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks;
    ListRowDestinationAdapter listRowDestinationAdapter;
    private String currentSearchQuery;
    private OnQueryDestinationTextListener onQueryDestinationTextListener;
    private Parcelable recyclerViewScrollPosition;

    private ProgressBar loadingSpinner;
    private ImageView arrowWhenNoLandmarksImageView;
    private TextView messageWhenNoLandmarksTextView;
    private SearchView searchView;
    private TextView searchViewNoResultsMessage;
    private RecyclerView landmarksRecyclerView;

    private String saveCurrentLandmark = "saveCurrentLandmark";
    private String saveCurrentSearchQuery = "saveCurrentSearchQuery";
    private String saveSelectedLandmarks = "saveSelectedLandmarks";
    private String saveIsMultipleSelected = "saveIsMultipleSelected";
    private String saveRecyclerViewScrollPosition = "saveRecyclerViewScrollPosition";

    private int currentTripId;

    private HashMap<Integer, Destination> multiSelectedLandmarksMap = new HashMap<Integer, Destination>();
    private boolean isMultipleSelect = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_destination_list, container, false);
        currentTripId = mCallbackGetCurrentTripId.onGetCurrentTripID();

        loadingSpinner = parentView.findViewById(R.id.landmarks_main_progress_bar_loading_spinner);
        loadingSpinner.setVisibility(View.VISIBLE);
        arrowWhenNoLandmarksImageView = parentView.findViewById(R.id.landmarks_add_trips_when_empty_arrow_image_view);
        messageWhenNoLandmarksTextView = parentView.findViewById(R.id.landmarks_add_trips_when_empty_text_view);
        searchViewNoResultsMessage = parentView.findViewById(R.id.landmarks_no_results_found_text_view);

        //toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mCallbackGetCurrentTripTitle.getCurrentTripTitle());
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setHasOptionsMenu(true);

        if(savedInstanceState != null){
            ((AppCompatActivity) getActivity()).supportInvalidateOptionsMenu();
        }

        // init the the RecyclerView
        landmarksRecyclerView = parentView.findViewById(R.id.landmarks_recycler_view);
        landmarksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        landmarksRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if (listRowDestinationAdapter == null ) {
            listRowDestinationAdapter = new ListRowDestinationAdapter(getActivity(), DestinationListFragment.this, null, currentSearchQuery);
        }
        // set map if needed

        landmarksRecyclerView.setAdapter(listRowDestinationAdapter);

        // init the cursorLoader
        cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                return new CursorLoader(getActivity(),
                        MyContentProvider.CONTENT_LANDMARKS_URI,
                        null,
                        MyContentProvider.Destinations.TRIP_ID_COLUMN + " =? ",
                        new String[] { Integer.toString(currentTripId) },
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                loadingSpinner.setVisibility(View.GONE);
                onCursorChange(cursor);

                // Swap the new cursor in. (The framework will take care of closing the
                // old cursor once we return.)
                listRowDestinationAdapter.swapCursor(cursor);

                setRecyclerViewPosition(cursor);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                // This is called when the last Cursor provided to onLoadFinished()
                // above is about to be closed.  We need to make sure we are no
                // longer using it.
                listRowDestinationAdapter.swapCursor(null);
            }
        };

        if (getLoaderManager().getLoader(DESTINATION_LOADER_ID) == null) {
            getLoaderManager().initLoader(DESTINATION_LOADER_ID, null, cursorLoaderCallbacks);
        }
        else {
            getLoaderManager().restartLoader(DESTINATION_LOADER_ID, null, cursorLoaderCallbacks);
        }

        // init the FloatingActionButton
        FloatingActionButton AddFab = parentView.findViewById(R.id.landmarks_main_floating_action_button);
        AddFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((DestinationMainActivity) getActivity()).currentDestination = null;
                DestinationDetailsFragment newFragment = new DestinationDetailsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.landmark_main_fragment_container, newFragment, DestinationDetailsFragment.TAG);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        initDialogs();
        return parentView;
    }

    public interface GetCurrentTripTitle {
        String getCurrentTripTitle();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        mCallbackGetCurrentTripId = StartActivityUtils.onAttachCheckInterface(activity, OnGetCurrentTripID.class);
        mSetCurrentDestinationCallback = StartActivityUtils.onAttachCheckInterface(activity, OnSetCurrentDestination.class);
        mCallbackGetCurrentTripTitle = StartActivityUtils.onAttachCheckInterface(activity, GetCurrentTripTitle.class);
        mCallbackGetIsDestinationAdded = StartActivityUtils.onAttachCheckInterface(activity, OnGetIsDestinationAdded.class);
        mCallbackGetMoveToDestinationId = StartActivityUtils.onAttachCheckInterface(activity, OnGetMoveToDestinationID.class);
    }

    @Override
    public void onOpenLandmarkDetailsForView(Destination destination) {
        currentDestination = destination;
        mSetCurrentDestinationCallback.onSetCurrentLandmark(destination);
        DestinationViewDetailsFragment newFragment = new DestinationViewDetailsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.landmark_main_fragment_container, newFragment, DestinationViewDetailsFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            currentDestination = savedInstanceState.getParcelable(saveCurrentLandmark);
            currentSearchQuery = savedInstanceState.getString(saveCurrentSearchQuery);
            multiSelectedLandmarksMap = ((HashMap<Integer, Destination>)savedInstanceState.getSerializable(saveSelectedLandmarks));
            isMultipleSelect = savedInstanceState.getBoolean(saveIsMultipleSelected);
            recyclerViewScrollPosition = savedInstanceState.getParcelable(saveRecyclerViewScrollPosition);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).supportInvalidateOptionsMenu();

        if (searchView != null) {
            searchView.setOnQueryTextListener(getOnQueryDestinationTextListener());
        }
    }

    //------------On Activity Result--------------//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == DESTINATION_DIALOG) {
            if (resultCode == Activity.RESULT_OK) {
                DestinationOptionDialogFragment.DialogOptions whichOptionEnum = (DestinationOptionDialogFragment.DialogOptions) data.getSerializableExtra(DESTINATION_DIALOG_OPTION);
                switch (whichOptionEnum) {
                    case EDIT:
                        onOpenLandmarkDetailsForUpdate();
                        break;
                    case DELETE:
                        destinationDeleteDialogConfirm.setMessage(getResources().getString(R.string.destination_delete_warning_dialog_message));
                        destinationDeleteDialogConfirm.show();
                        break;
                    case VIEW:
                        onOpenLandmarkDetailsForView(currentDestination);
                        break;
                }
            }
        }
    }

    private void initDialogs() {
        // Use the Builder class for convenient dialog construction
        destinationDeleteDialogConfirm = new AlertDialog.Builder(getActivity())
                //set message, title, and icon
                .setTitle(getResources().getString(R.string.destination_delete_warning_dialog_title))
                .setPositiveButton(getResources().getString(R.string.destination_delete_warning_dialog_delete_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onDeleteLandmarkDialog();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.destination_delete_warning_dialog_cancel_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        multipleDestinationDeleteDialogConfirm = new AlertDialog.Builder(getActivity())
                //set message, title, and icon
                .setTitle(getResources().getString(R.string.destination_multiple_delete_warning_dialog_title))
                .setPositiveButton(getResources().getString(R.string.destination_delete_warning_dialog_delete_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onDeleteMultipleLandmarks();
                        dialog.dismiss();
                        listRowDestinationAdapter.handleFinishActionMode();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.destination_delete_warning_dialog_cancel_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    @Override
    public void onPause() {
        super.onPause();

        // in order to save the current search query, we need to deactivate the callbacks.
        if (searchView != null) {
            searchView.setOnQueryTextListener(null);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    ////////////////////////////////
    //Toolbar functions
    ////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timeline_menusitem_destination_fragment, menu);

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(getOnQueryDestinationTextListener());

        EditText searchEditText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        if(searchEditText != null) {
            searchEditText.setTextColor(Color.WHITE);
        }
    }

    public OnQueryDestinationTextListener getOnQueryDestinationTextListener() {
        if (onQueryDestinationTextListener == null) {
            onQueryDestinationTextListener = new OnQueryDestinationTextListener();
        }
        return onQueryDestinationTextListener;
    }

    public void onOpenLandmarkDetailsForUpdate() {
        DestinationDetailsFragment updateFragment = new DestinationDetailsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.landmark_main_fragment_container, updateFragment, DestinationDetailsFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onDeleteLandmarkDialog() {
        // delete current Destination
        getActivity().getContentResolver().delete(
                ContentUris.withAppendedId(MyContentProvider.CONTENT_LANDMARK_ID_URI_BASE, currentDestination.getId()),
                null,
                null);
    }

    public void onDeleteMultipleLandmarks() {
        for (Destination destination : multiSelectedLandmarksMap.values()) {
            getActivity().getContentResolver().delete(
                    ContentUris.withAppendedId(MyContentProvider.CONTENT_LANDMARK_ID_URI_BASE, destination.getId()),
                    null,
                    null);
        }
    }

    @Override
        public void onPrepareOptionsMenu(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        if (!TextUtils.isEmpty(currentSearchQuery)) {
            String searchQuery = currentSearchQuery;

            // set to null in order to avoid text change when expandActionView
            searchView.setOnQueryTextListener(null);
            MenuItemCompat.expandActionView(menu.findItem(R.id.search));

            searchView.setOnQueryTextListener(getOnQueryDestinationTextListener());
            searchView.setQuery(searchQuery, true);
        }

        //check if i'm the last trip and the quick Destination window is closed
        MenuItem showQuickLandmarks =  menu.findItem(R.id.show_quick_landmarks_option_item);
        MenuItem hideQuickLandmarks =  menu.findItem(R.id.hide_quick_landmarks_option_item);
        Trip lastTrip = DatabaseUtils.getLastTrip(getActivity());
        if(lastTrip != null && lastTrip.getId() == currentTripId){
           if(!SharedPreferencesUtils.getIsNotificationsWindowOpen(getActivity())){
               showQuickLandmarks.setVisible(true);
               hideQuickLandmarks.setVisible(false);
           }
           else {
               showQuickLandmarks.setVisible(false);
               hideQuickLandmarks.setVisible(true);
           }
        }
        super.onPrepareOptionsMenu(menu);
    }


    //---------------------save-------------------//

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(saveCurrentLandmark, currentDestination);
        outState.putString(saveCurrentSearchQuery, currentSearchQuery);
        outState.putSerializable(saveSelectedLandmarks, multiSelectedLandmarksMap);
        outState.putBoolean(saveIsMultipleSelected, isMultipleSelect);

        if (landmarksRecyclerView != null) {
            outState.putParcelable(saveRecyclerViewScrollPosition, landmarksRecyclerView.getLayoutManager().onSaveInstanceState());
        } else {
            outState.putParcelable(saveRecyclerViewScrollPosition, recyclerViewScrollPosition);
        }


    }

    private void onCursorChange(Cursor cursor) {
        if (mCallbackGetIsDestinationAdded.getIsLandmarkAdded()) {
            return;
        }

        if (cursor.getCount() == 0) {
                arrowWhenNoLandmarksImageView.setVisibility(View.VISIBLE);
                arrowWhenNoLandmarksImageView.setAnimation(AnimationUtils.getArrowListEmptyAnimation());
                messageWhenNoLandmarksTextView.setVisibility(View.VISIBLE);
        } else {
            arrowWhenNoLandmarksImageView.setAnimation(null);
            arrowWhenNoLandmarksImageView.setVisibility(View.GONE);
            messageWhenNoLandmarksTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void OnActionItemPress(MenuItem item) {
        int id = item.getItemId();
        if(multiSelectedLandmarksMap.values().size() == 1){
            currentDestination = multiSelectedLandmarksMap.values().iterator().next();
            mSetCurrentDestinationCallback.onSetCurrentLandmark(currentDestination);
        }
        switch (id) {
            case R.id.multiple_select_action_delete:
                multipleDestinationDeleteDialogConfirm.setMessage(getResources().getString(R.string.destination_multiple_delete_warning_dialog_message));
                multipleDestinationDeleteDialogConfirm.show();
                break;
            case R.id.multiple_select_action_edit:
                onOpenLandmarkDetailsForUpdate();
                listRowDestinationAdapter.handleFinishActionMode();
                break;
            case R.id.multiple_select_action_view:
                onOpenLandmarkDetailsForView(currentDestination);
                listRowDestinationAdapter.handleFinishActionMode();
                break;

        }
    }

    private void setRecyclerViewPosition(Cursor cursor) {
        int gotoLandmarkId = mCallbackGetMoveToDestinationId.onGetMoveToLandmarkId();
        if (gotoLandmarkId != StartActivityUtils.NOT_JUMP_TO_LANDMARK_ID) {
            while (cursor.moveToNext()) {
                int landmarkId = cursor.getInt(cursor.getColumnIndexOrThrow(MyContentProvider.Destinations.ID_COLUMN));
                if (gotoLandmarkId == landmarkId) {
                    landmarksRecyclerView.getLayoutManager().scrollToPosition(cursor.getPosition()); // make it smooth
                }
            }
        }

        if (recyclerViewScrollPosition != null) {
            landmarksRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewScrollPosition);
            recyclerViewScrollPosition = null;
        }
    }

    public interface OnSetCurrentDestination {
        void onSetCurrentLandmark(Destination destination);
    }

    private void updateSearchQuery(String query) {
        if (TextUtils.equals(query, currentSearchQuery)) {
            return;
        }

        currentSearchQuery = query;
        if (listRowDestinationAdapter != null) {
            listRowDestinationAdapter.getFilter().filter(query);
        }
    }


    @Override
    public void onFilterPublishResults(int resultsCount) {
        if (resultsCount == 0 && (messageWhenNoLandmarksTextView.getVisibility() == View.GONE)) {
            searchViewNoResultsMessage.setVisibility(View.VISIBLE);
        } else {
            searchViewNoResultsMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.view_trip_details_item:
                //move to trip view details fragment
                TripViewDetailsFragment tripViewFragment = new TripViewDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean(TripViewDetailsFragment.FROM_TRIPS_LIST, false);
                tripViewFragment.setArguments(bundle);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.landmark_main_fragment_container, tripViewFragment, TripViewDetailsFragment.TAG);
                transaction.addToBackStack(null);
                transaction.commit();
                return true;

            case R.id.view_map_item:
                if(LocationUtils.checkPlayServices(getActivity(), true)) {
                    Intent mapIntent = new Intent(getActivity(), DestinationMultiMap.class);
                    Bundle gpsLocationBundle = new Bundle();
                    ArrayList<Destination> destinationArray = new ArrayList();

                    Cursor cursor = listRowDestinationAdapter.getOrigCursor();
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                Destination currentDestination = new Destination(cursor);
                                destinationArray.add(currentDestination);
                            } while (cursor.moveToNext());
                        }

                        gpsLocationBundle.putParcelableArrayList(DestinationMainActivity.LandmarkArrayList, destinationArray);
                        mapIntent.putExtras(gpsLocationBundle);
                        startActivity(mapIntent);
                    }
                }
                break;
            case R.id.show_quick_landmarks_option_item:
                if(NotificationUtils.areNotificationsEnabled(getActivity())) {
                    NotificationUtils.initNotification(getActivity(), DatabaseUtils.getLastTrip(getActivity()).getTitle());
                }
                else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.notification_disabled_message), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.hide_quick_landmarks_option_item:
                NotificationUtils.cancelNotification(getActivity());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public interface OnGetIsDestinationAdded {
        boolean getIsLandmarkAdded();
    }

    public interface OnGetMoveToDestinationID {
        int onGetMoveToLandmarkId();
    }

    @Override
    public HashMap<Integer, Destination> onGetSelectedLandmarkMap() {
        return multiSelectedLandmarksMap;
    }

    @Override
    public boolean getIsMultipleSelect() {
        return isMultipleSelect;
    }

    @Override
    public void setIsMultipleSelect(boolean isMultipleSelect) {
        this.isMultipleSelect = isMultipleSelect;
    }

    @Override
    public void onClearSelectedLandmarkMap() {
        multiSelectedLandmarksMap.clear();
    }

    private class OnQueryDestinationTextListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            searchView.clearFocus();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            updateSearchQuery(newText);
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(landmarksRecyclerView != null){
            landmarksRecyclerView.setAdapter(null);
        }
    }

}