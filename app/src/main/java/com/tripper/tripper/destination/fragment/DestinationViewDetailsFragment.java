package com.tripper.tripper.destination.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.TypedArray;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tripper.tripper.R;
import com.tripper.tripper.destination.interfaces.OnGetCurrentDestination;
import com.tripper.tripper.models.Destination;
import com.tripper.tripper.utils.DateUtils;
import com.tripper.tripper.utils.ImageUtils;
import com.tripper.tripper.utils.LocationUtils;

import java.text.SimpleDateFormat;

public class DestinationViewDetailsFragment extends Fragment {

    public static final String TAG = DestinationViewDetailsFragment.class.getSimpleName();

    // Destination View Details Views
    private TextView lmTitleTextView;
    private ImageView lmPhotoImageView;
    private View lmPhotoFrameLayout;
    private TextView lmDateTextView;
    private TextView lmAutomaticLocationTextView;
    private TextView lmLocationDescriptionTextView;
    private LinearLayout lmTypeLayout;
    private TextView lmTypeTextView;
    private ImageView lmIconTypeImageView;
    private TextView lmDescriptionTextView;


    // Private parameters
    private Destination currentDestination;
    private View parentView;
    private OnGetCurrentDestination mCallback;
    private SimpleDateFormat dateFormatter;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_destination_view_details, container, false);

        // initialize Destination date parameters
        dateFormatter = DateUtils.getFormDateTimeFormat();

        findViewsById(parentView);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.show();
        actionBar.setTitle(getResources().getString(R.string.destination_view_details_toolbar_title));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        currentDestination = mCallback.onGetCurrentDestination();
        if (currentDestination != null) {
            // We were called from Update Destination need to update parameters
            updateLmParameters();
        }

        setHasOptionsMenu(true);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        return parentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnGetCurrentDestination) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnGetCurrentDestination");
        }
    }


    // find all needed views by id's
    private void findViewsById(View parentView) {
        lmTitleTextView = parentView.findViewById(R.id.landmark_view_details_title);
        lmPhotoImageView = parentView.findViewById(R.id.landmark_view_details_photo);
        lmPhotoFrameLayout = parentView.findViewById(R.id.landmark_frame_layout_details_photo);
        lmAutomaticLocationTextView = parentView.findViewById(R.id.landmark_view_details_automatic_location);
        lmDateTextView = parentView.findViewById(R.id.landmark_view_details_date);
        lmLocationDescriptionTextView = parentView.findViewById(R.id.landmark_view_details_location_description);
        lmTypeLayout = parentView.findViewById(R.id.landmark_view_type_layout);
        lmTypeTextView = parentView.findViewById(R.id.landmark_view_details_type);
        lmIconTypeImageView = parentView.findViewById(R.id.landmark_view_details_icon_type);
        lmDescriptionTextView = parentView.findViewById(R.id.landmark_view_details_description);
    }

    // Update Destination , need to update Destination Parameters
    private void updateLmParameters() {

        String[] type = getResources().getStringArray(R.array.destination_details_type_spinner_array);
        TypedArray iconType = getResources().obtainTypedArray(R.array.landmark_view_details_icon_type_array);
        String automaticLocation = currentDestination.getAutomaticLocation();
        if(automaticLocation == null){
            automaticLocation = LocationUtils.locationToLatLngString(getActivity(), currentDestination.getGPSLocation());
        }

        // for each view, if it's not empty set the text, otherwise, set the view as gone.
        setViewStringOrGone(lmTitleTextView,
                parentView.findViewById(R.id.landmark_view_underline_title),
                currentDestination.getTitle());
        setViewStringOrGone(lmDateTextView,
                null,
                dateFormatter.format(currentDestination.getDate()));
        setViewStringOrGone(lmAutomaticLocationTextView,
                parentView.findViewById(R.id.landmark_view_uperline_automatic_location),
                automaticLocation);
        setViewStringOrGone(lmLocationDescriptionTextView,
                parentView.findViewById(R.id.landmark_view_uperline_location_description),
                currentDestination.getLocationDescription());
        setViewStringOrGone(lmTypeTextView,
                parentView.findViewById(R.id.landmark_view_uperline_type),
                type[currentDestination.getTypePosition()]);
        setViewStringOrGone(lmDescriptionTextView,
                parentView.findViewById(R.id.landmark_view_uperline_description),
                currentDestination.getDescription());

        if(currentDestination.getTypePosition() > 0){
            lmTypeLayout.setVisibility(View.VISIBLE);
            lmIconTypeImageView.setVisibility(View.VISIBLE);
            lmIconTypeImageView.setImageResource(iconType.getResourceId(currentDestination.getTypePosition(), -1));
        }
        else{
            lmTypeLayout.setVisibility(View.GONE);
            lmIconTypeImageView.setVisibility(View.GONE);
        }

        if (currentDestination.getPhotoPath() == null || currentDestination.getPhotoPath().trim().equals("")) {
            lmPhotoFrameLayout.setVisibility(View.GONE);
        } else {
            lmPhotoFrameLayout.setVisibility(View.VISIBLE);
            ImageUtils.updatePhotoImageViewByPath(getActivity(), currentDestination.getPhotoPath(), lmPhotoImageView);
            lmPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(getActivity(), v, currentDestination.getPhotoPath());
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

    ////////////////////////////////
    //Toolbar functions
    ////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details_menusitem_destination_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.edit_item:
                //move to Destination update details fragment
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.landmark_main_fragment_container, new DestinationDetailsFragment(), DestinationDetailsFragment.TAG);
                transaction.addToBackStack(null);
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void zoomImageFromThumb(final Activity activity, final View thumbView, String filePath) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        ((AppCompatActivity)activity).getSupportActionBar().hide();

        final ImageView expandedImageView = activity.findViewById(
                R.id.expanded_image);

        final View expandedLayoutView = activity.findViewById(R.id.expanded_image_layout);

        ImageUtils.updatePhotoImageViewByPath(activity, filePath, expandedImageView, false);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        activity.findViewById(R.id.fragment_landmark_view_details_layout)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
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

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedLayoutView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedLayoutView.setPivotX(0f);
        expandedLayoutView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
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

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                ((AppCompatActivity)activity).getSupportActionBar().show();

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
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