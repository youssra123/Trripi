package com.tripper.tripper.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class HighlightTextView extends TextView {

    public HighlightTextView(Context context) {
        super(context);
    }

    public HighlightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HighlightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setHighlightText(String itemValue, String filter) {
        if (TextUtils.isEmpty(filter) || TextUtils.isEmpty(itemValue)) {
            this.setText(itemValue);
            return;
        }

        int startPos = itemValue.toLowerCase(Locale.US).indexOf(filter.toLowerCase(Locale.US));
        int endPos = startPos + filter.length();

        if (startPos != -1)
        {
            Spannable spannable = new SpannableString(itemValue);
            ColorStateList blueColor = new ColorStateList(new int[][] { new int[] {}}, new int[] { Color.BLUE });
            TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.NORMAL, -1, blueColor, null);

            spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.setText(spannable);
        }
        else {
            this.setText(itemValue);
        }
    }

    public void setHighlightTextOrGone(String itemValue, String filter) {
        if (TextUtils.isEmpty(itemValue)) {
            this.setVisibility(View.GONE);
        } else {
            this.setVisibility(View.VISIBLE);
            this.setHighlightText(itemValue, filter);
        }
    }

    public boolean isGone() {
        return (this.getVisibility() == View.GONE);
    }
}
