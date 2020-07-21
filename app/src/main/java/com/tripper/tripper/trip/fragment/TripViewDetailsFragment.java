package com.tripper.tripper.trip.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.tripper.tripper.R;
import com.tripper.tripper.models.Trip;
import com.tripper.tripper.trip.interfaces.OnGetCurrentTrip;
import com.tripper.tripper.utils.DateUtils;
import com.tripper.tripper.utils.ImageUtils;

import java.text.SimpleDateFormat;


public class TripViewDetailsFragment extends Fragment {

    // tag
    public static final String TAG = TripViewDetailsFragment.class.getSimpleName();

    // Destination View Details Views
    private TextView tripTitleTextView;
    private ImageView tripPhotoImageView;
    private View tripPhotoFrameLayout;
    private TextView tripDatesTextView;
    private TextView tripPlaceTextView;
    private TextView tripDescriptionTextView;


    private View parentView;
    private OnGetCurrentTrip mCallbackGetCurrentTrip;
    private Trip currentTrip;

    private boolean fromTripsList;
    public static final String FROM_TRIPS_LIST = "FROM_TRIPS_LIST";

    private SimpleDateFormat dateFormatter;

    private Animator mCurrentAnimator;

    private int mShortAnimationDuration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_trip_view_details, container, false);

        dateFormatter = DateUtils.getTripListDateFormat();

        findViewsById(parentView);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.show();
        actionBar.setTitle(getResources().getString(R.string.trip_view_details_toolbar_title));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);


        fromTripsList = getArguments().getBoolean(FROM_TRIPS_LIST);

        currentTrip = mCallbackGetCurrentTrip.onGetCurrentTrip();

        updateTripParameters();

        setHasOptionsMenu(true);

        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        return parentView;
    }

    private void findViewsById(View parentView) {
        tripTitleTextView = parentView.findViewById(R.id.trip_view_details_title);
        tripPhotoImageView = parentView.findViewById(R.id.trip_view_details_photo);
        tripPhotoFrameLayout = parentView.findViewById(R.id.trip_frame_layout_details_photo);
        tripPlaceTextView = parentView.findViewById(R.id.trip_view_details_place);
        tripDatesTextView = parentView.findViewById(R.id.trip_view_details_dates);
        tripDescriptionTextView = parentView.findViewById(R.id.trip_view_details_description);
    }


    private void updateTripParameters() {

        setViewStringOrGone(tripTitleTextView,
                parentView.findViewById(R.id.trip_view_underline_title),
                currentTrip.getTitle());
        setViewStringOrGone(tripDatesTextView,
                null,
                dateFormatter.format(currentTrip.getStartDate()) + " - " + dateFormatter.format(currentTrip.getEndDate()));
        setViewStringOrGone(tripPlaceTextView,
                parentView.findViewById(R.id.trip_view_uperline_place),
                currentTrip.getPlace());
        setViewStringOrGone(tripDescriptionTextView,
                parentView.findViewById(R.id.trip_view_uperline_description),
                currentTrip.getDescription());

        if(currentTrip.getPicture() == null || currentTrip.getPicture().trim().equals("")){
            tripPhotoFrameLayout.setVisibility(View.GONE);
        }
        else{
            tripPhotoFrameLayout.setVisibility(View.VISIBLE);
            ImageUtils.updatePhotoImageViewByPath(getActivity(), currentTrip.getPicture(), tripPhotoImageView);
            tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(getActivity() ,v , currentTrip.getPicture());
                }
            });
        }
    }

    private void setViewStringOrGone(TextView currentView, View view, String string) {
        if (string == null || string.trim().isEmpty()) {
            currentView.setVisibility(View.GONE);
            if(view != null){
                view.setVisibility(View.GONE);
            }
        } else {
            currentView.setText(string);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details_menusitem_trip_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.edit_item:
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                //TODO: MAKE SURE IT'S O.K
                if(fromTripsList) {
                    transaction.replace(R.id.trip_main_fragment_container, new TripUpdateFragment(), TripUpdateFragment.TAG);
                }
                else{
                    transaction.replace(R.id.landmark_main_fragment_container, new TripUpdateFragment(), TripUpdateFragment.TAG);
                }
                transaction.addToBackStack(null);
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbackGetCurrentTrip = (OnGetCurrentTrip) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GetCurrentTrip");
        }

    }

    private void zoomImageFromThumb(final Activity activity, final View thumbView, String filePath) {

        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        ((AppCompatActivity)activity).getSupportActionBar().hide();

        final ImageView expandedImageView = activity.findViewById(
                R.id.expanded_image);

        final View expandedLayoutView = activity.findViewById(R.id.expanded_image_layout);

        ImageUtils.updatePhotoImageViewByPath(activity, filePath, expandedImageView, false);


        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();


        thumbView.getGlobalVisibleRect(startBounds);
        activity.findViewById(R.id.fragment_trip_view_details_layout)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);


        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        thumbView.setAlpha(0f);
        expandedLayoutView.setVisibility(View.VISIBLE);


        expandedLayoutView.setPivotX(0f);
        expandedLayoutView.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedLayoutView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedLayoutView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedLayoutView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedLayoutView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;


        final float startScaleFinal = startScale;
        expandedLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                ((AppCompatActivity)activity).getSupportActionBar().show();


                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedLayoutView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedLayoutView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedLayoutView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedLayoutView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedLayoutView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedLayoutView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
