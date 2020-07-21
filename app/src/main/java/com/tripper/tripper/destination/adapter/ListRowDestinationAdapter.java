package com.tripper.tripper.destination.adapter;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tripper.tripper.R;
import com.tripper.tripper.models.Destination;
import com.tripper.tripper.services.MyContentProvider;
import com.tripper.tripper.utils.DateUtils;
import com.tripper.tripper.utils.StartActivityUtils;
import com.tripper.tripper.views.HighlightTextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ListRowDestinationAdapter extends RecyclerView.Adapter<ListRowDestinationAdapter.LandmarkViewHolder> implements Filterable {

    public static final String TAG = ListRowDestinationAdapter.class.getSimpleName();

    private LandmarkCursorAdapter landmarkCursorAdapter;
    private OnOpenLandmarkDetailsForUpdate mCallbackSetCurLandmark;
    private Context context;
    private String filter;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LANDMARK = 1;

    private ActionMode mActionMode = null;
    private OnActionItemPress mCallbackActionItemPress;
    private OnGetSelectedLandmarkMap mCallbackMultipleSelectHandle;
    private OnFilterPublishResults mCallbackFilterPublishResults;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.multi_select_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MenuItem editItem = menu.findItem(R.id.multiple_select_action_edit);
            MenuItem viewItem = menu.findItem(R.id.multiple_select_action_view);
            MenuItem deleteItem = menu.findItem(R.id.multiple_select_action_delete);

            if(mCallbackMultipleSelectHandle.onGetSelectedLandmarkMap().size() > 0){
                deleteItem.setVisible(true);
            }

            else {
                deleteItem.setVisible(false);
            }

            if (mCallbackMultipleSelectHandle.onGetSelectedLandmarkMap().size() == 1) {
                editItem.setVisible(true);
                viewItem.setVisible(true);
                return true;
            } else {
                editItem.setVisible(false);
                viewItem.setVisible(false);
                return true;
            }
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            mCallbackActionItemPress.OnActionItemPress(item);

            return true;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            ListRowDestinationAdapter.this.notifyDataSetChanged();
            mActionMode = null;
            mCallbackMultipleSelectHandle.setIsMultipleSelect(false);
            mCallbackMultipleSelectHandle.onClearSelectedLandmarkMap();

        }
    };



    // ------------------------ Interfaces ----------------------------- //
    public interface OnFilterPublishResults {
        void onFilterPublishResults(int resultsCount);
    }

    public interface OnOpenLandmarkDetailsForUpdate {
        void onOpenLandmarkDetailsForView(Destination destination);
    }

    public interface OnActionItemPress {
        void OnActionItemPress(MenuItem item);
    }

    public interface OnGetSelectedLandmarkMap {
        HashMap<Integer, Destination> onGetSelectedLandmarkMap();
        void onClearSelectedLandmarkMap();
        boolean getIsMultipleSelect();
        void setIsMultipleSelect(boolean isMultipleSelect);
    }
    // ------------------------ Constructor ----------------------------- //
    public ListRowDestinationAdapter(Context context, Fragment fragment, Cursor cursor, String filter) {
        mCallbackSetCurLandmark = StartActivityUtils.onAttachCheckInterface(fragment, OnOpenLandmarkDetailsForUpdate.class);
        mCallbackActionItemPress = StartActivityUtils.onAttachCheckInterface(fragment, OnActionItemPress.class);
        mCallbackMultipleSelectHandle = StartActivityUtils.onAttachCheckInterface(fragment, OnGetSelectedLandmarkMap.class);
        mCallbackFilterPublishResults = StartActivityUtils.onAttachCheckInterface(fragment, OnFilterPublishResults.class);

        this.filter = filter;
        this.context = context;
        Cursor cursorWrapper = createCursorWrapper(cursor);

        this.landmarkCursorAdapter = new LandmarkCursorAdapter(context, cursorWrapper, 0);
    }

    // ------------------------ ViewHolder Class ----------------------------- //
    public class LandmarkViewHolder extends RecyclerView.ViewHolder {
        private View v;

        public LandmarkViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            v = itemLayoutView;
        }
    }

    // ------------------------ RecyclerView.Adapter methods ----------------------------- //
    @Override
    public ListRowDestinationAdapter.LandmarkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = landmarkCursorAdapter.newView(context, landmarkCursorAdapter.getCursor(), parent);
        return new LandmarkViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ListRowDestinationAdapter.LandmarkViewHolder holder, int position) {
        landmarkCursorAdapter.getCursor().moveToPosition(position);
        landmarkCursorAdapter.bindView(holder.itemView, context, landmarkCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        if(landmarkCursorAdapter == null) return 0;

        return landmarkCursorAdapter.getCount();
    }

    private CursorWrapper createCursorWrapper(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        if (TextUtils.isEmpty(this.filter)) {
            return new CursorWrapper(cursor);
        } else {
            String[] columnsToSearch = new String[] {
                    MyContentProvider.Destinations.TITLE_COLUMN,
                    MyContentProvider.Destinations.AUTOMATIC_LOCATION_COLUMN,
                    MyContentProvider.Destinations.LOCATION_DESCRIPTION_COLUMN,
                    MyContentProvider.Destinations.DESCRIPTION_COLUMN,
            };

            return new FilterCursorWrapper(cursor, this.filter, columnsToSearch);
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        Cursor oldCursor = landmarkCursorAdapter.swapCursor(createCursorWrapper(newCursor));
        this.notifyDataSetChanged();
        mCallbackFilterPublishResults.onFilterPublishResults(landmarkCursorAdapter.getCount());
        return oldCursor;
    }

    // ------------------------ CursorAdapter class ----------------------------- //
    private class LandmarkCursorAdapter extends CursorAdapter {
        public TextView title, date;

        public LandmarkCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.data_card_destination_timeline_layout, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final Destination destination = new Destination(cursor);
            int itemViewType = getItemViewType(cursor.getPosition());
            View viewHeader = view.findViewById(R.id.landmark_card_header);
            viewHeader.setVisibility(View.GONE);

            switch (itemViewType) {
                case TYPE_HEADER:
                    viewHeader.setVisibility(View.VISIBLE);
                    TextView dateHeaderTextView = view.findViewById(R.id.landmark_header_date_text_view);
                    Date date = destination.getDate();
                    SimpleDateFormat sdfHeader = DateUtils.getLandmarkHeaderDateFormat();
                    dateHeaderTextView.setText(sdfHeader.format(date));

                case TYPE_LANDMARK:
                    final HighlightTextView title = view.findViewById(R.id.landmark_card_timeline_title_text_view);
                    final TextView dateDataTextView = view.findViewById(R.id.landmark_card_date_text_view);
                    final ImageView landmarkImage = view.findViewById(R.id.landmark_card_photo_image_view);
                    final CardView landmarkCard = view.findViewById(R.id.landmark_card_view_widget);
                    final CheckBox selectLandmarkCheckbox = view.findViewById(R.id.select_landmark_checkbox);
                    final HighlightTextView locationTextBox = view.findViewById(R.id.landmark_card_timeline_location_text_view);
                    final HighlightTextView locationDescriptionTextBox = view.findViewById(R.id.landmark_card_timeline_location_description_text_view);
                    final View textLayout = view.findViewById(R.id.landmark_card_timeline_text_layout);

                    if(isMultiSelect()){
                        selectLandmarkCheckbox.setVisibility(View.VISIBLE);
                        selectLandmarkCheckbox.setChecked(mCallbackMultipleSelectHandle.onGetSelectedLandmarkMap().containsKey(destination.getId()));
                        if (mActionMode == null) {
                            mActionMode = ((AppCompatActivity)view.getContext()).startActionMode(mActionModeCallback);
                            updateActionModeTitle();
                        }

                    }
                    else {
                        selectLandmarkCheckbox.setVisibility(View.GONE);
                        selectLandmarkCheckbox.setChecked(false);
                    }
                    landmarkCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isMultiSelect()) {
                                selectLandmarkCheckbox.setChecked(multi_select(destination.getId(), destination));
                            }
                            else {
                                mCallbackSetCurLandmark.onOpenLandmarkDetailsForView(destination);
                                AppCompatActivity hostActivity = (AppCompatActivity) view.getContext();
                            }
                        }
                    });
                    landmarkCard.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if (!isMultiSelect()) {
                                mCallbackMultipleSelectHandle.setIsMultipleSelect(true);

                                if (mActionMode == null) {
                                    mActionMode = view.startActionMode(mActionModeCallback);
                                }

                                multi_select(destination.getId(), destination);
                                ListRowDestinationAdapter.this.notifyDataSetChanged();
                            }
                            else {
                                selectLandmarkCheckbox.setChecked(multi_select(destination.getId(), destination));
                            }

                            return true;
                        }
                    });

                    // set title
                    title.setHighlightTextOrGone(destination.getTitle(), filter);

                    // set location
                    locationTextBox.setHighlightTextOrGone(destination.getAutomaticLocation(), filter);
                    locationDescriptionTextBox.setHighlightTextOrGone(destination.getLocationDescription(), filter);

                    textLayout.setVisibility(title.isGone() && title.isGone() && locationTextBox.isGone()
                                            ? View.GONE :
                                            View.VISIBLE);

                    // set image
                    String imagePath = destination.getPhotoPath();
                    if (TextUtils.isEmpty(imagePath)) {
                        Picasso.with(context).cancelRequest(landmarkImage);
                        landmarkImage.setImageDrawable(null);
                        landmarkImage.setVisibility(View.GONE);
                    } else {
                        landmarkImage.setVisibility(View.VISIBLE);
                        Picasso.with(context).load(new File(imagePath)).error(R.drawable.error_no_image).fit().centerCrop().into(landmarkImage);
                    }

                    // set date
                    SimpleDateFormat sdfData = DateUtils.getLandmarkTimeDateFormat();
                    dateDataTextView.setText(sdfData.format(destination.getDate()));

                    // start trip row
                    View viewStart = view.findViewById(R.id.landmark_card_start);
                    viewStart.setVisibility(cursor.isLast() ? View.VISIBLE : View.GONE);

                    break;
            }

        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Cursor cursor = (Cursor) landmarkCursorAdapter.getItem(position);
            if(position == -1) {
                return TYPE_HEADER;
            }

            // date of current item
            Date dateCurrent =  DateUtils.databaseStringToDate(cursor.getString(cursor.getColumnIndex(MyContentProvider.Destinations.DATE_COLUMN)));

            if (!cursor.moveToPrevious()){
                cursor.moveToNext();
                return TYPE_HEADER;
            }

            // date of item that temporary comes after
            Date datePrev = DateUtils.databaseStringToDate(cursor.getString(cursor.getColumnIndex(MyContentProvider.Destinations.DATE_COLUMN)));

            cursor.moveToNext();
            return DateUtils.isSameDay(dateCurrent, datePrev) ? TYPE_LANDMARK : TYPE_HEADER;
        }
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ListRowDestinationAdapter.this.filter = constraint.toString();
                FilterResults res = new FilterResults();

                if (landmarkCursorAdapter.getCursor() == null) {
                    res.values = null;
                    return res;
                }

                Cursor origCursor = getOrigCursor();
                Cursor filteredCursor = createCursorWrapper(origCursor);
                res.values = filteredCursor;
                res.count = filteredCursor.getCount();

                return res;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values == null) {
                    return;
                }

                landmarkCursorAdapter.swapCursor((Cursor)(results.values));
                ListRowDestinationAdapter.this.notifyDataSetChanged();
                mCallbackFilterPublishResults.onFilterPublishResults(landmarkCursorAdapter.getCount());
            }
        };

        return filter;
    }

    private class FilterCursorWrapper extends CursorWrapper {
        private String filter;
        private int[] index;
        private int count = 0;
        private int pos = 0;

        private FilterCursorWrapper(Cursor cursor, String filter, String[] columns) {
            super(cursor);
            this.filter = filter.toLowerCase();

            if (!TextUtils.isEmpty(this.filter)) {
                this.count = super.getCount();
                this.index = new int[this.count];

                for (int i=0;i<this.count;i++) {
                    super.moveToPosition(i);
                    for (String column: columns) {
                        if (checkEqualStringToColumn(this.getColumnIndexOrThrow(column), this.filter)) {
                            this.index[this.pos++] = i;
                            break;
                        }
                    }
                }

                this.count = this.pos;
                this.pos = 0;
                super.moveToFirst();
            } else {
                this.count = super.getCount();
                this.index = new int[this.count];
                for (int i=0;i<this.count;i++) {
                    this.index[i] = i;
                }
            }
        }

        @Override
        public boolean move(int offset) {
            return this.moveToPosition(this.pos + offset);
        }

        @Override
        public boolean moveToNext() {
            return this.moveToPosition(this.pos + 1);
        }

        @Override
        public boolean moveToPrevious() {
            return this.moveToPosition(this.pos - 1);
        }

        @Override
        public boolean moveToFirst() {
            return this.moveToPosition(-1);
        }

        @Override
        public boolean moveToLast() {
            return this.moveToPosition(this.count-1);
        }

        @Override
        public boolean moveToPosition(int position) {
            if (position >= this.count || position < -1)
                return false;
            this.pos = position;
            if (position == -1) {
                return false;
            }
            return super.moveToPosition(this.index[position]);
        }

        @Override
        public int getCount() {
            return this.count;
        }

        @Override
        public int getPosition() {
            return this.pos;
        }

        @Override
        public boolean isLast() {
            return this.pos + 1 == this.count;
        }


        private boolean checkEqualStringToColumn(int column, String filter) {
            String value = this.getString(column);
            if (TextUtils.isEmpty(value)) {
                return false;
            }

            return this.getString(column).toLowerCase().contains(filter);
        }
    }

    //----------multiple select---------------
    // Add/Remove the item from/to the list

    public boolean multi_select(int landmarkId, Destination destination) {
        boolean res = false;
        if (mActionMode != null) {
            if (mCallbackMultipleSelectHandle.onGetSelectedLandmarkMap().containsKey(landmarkId)) {
                mCallbackMultipleSelectHandle.onGetSelectedLandmarkMap().remove(Integer.valueOf(landmarkId));
            }
            else {
                mCallbackMultipleSelectHandle.onGetSelectedLandmarkMap().put(landmarkId, destination);
                res = true;
            }
            updateActionModeTitle();
            mActionMode.invalidate();
        }
        return res;
    }

    private void updateActionModeTitle(){
        if (mCallbackMultipleSelectHandle.onGetSelectedLandmarkMap().size() > 0) {
            mActionMode.setTitle("" + mCallbackMultipleSelectHandle.onGetSelectedLandmarkMap().size());
        }
        else {
            mActionMode.setTitle("");
        }
    }

    private boolean isMultiSelect(){
        return mCallbackMultipleSelectHandle.getIsMultipleSelect();
    }

    public void handleFinishActionMode(){
        if(mActionMode != null){
            mActionMode.finish();
        }
    }

    public Cursor getOrigCursor() {
        return ((CursorWrapper)(landmarkCursorAdapter.getCursor())).getWrappedCursor();
    }
}

