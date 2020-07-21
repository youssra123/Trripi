package com.tripper.tripper.utils;

import android.text.Html;
import android.text.Spanned;

public class FormatHtmlText {
    final private String bold = "b";
    final private String italic = "i";
    final private String underline = "u";

    String htmlString;

    public FormatHtmlText(String s) {
        htmlString = s;
    }

    public FormatHtmlText setBold() {
        addStringOnBothSides(bold);
        return this;
    }

    public FormatHtmlText setItalic() {
        addStringOnBothSides(italic);
        return this;
    }

    public FormatHtmlText setUnderline() {
        addStringOnBothSides(underline);
        return this;
    }


    public Spanned Format() {
        return Html.fromHtml(htmlString);
    }

    private void addStringOnBothSides(String whatToAdd) {
        htmlString = "<" + whatToAdd + ">" + htmlString + "</" + whatToAdd + ">" ;
    }

    public static FormatHtmlText Start(String s) {
        return new FormatHtmlText(s);
    }

    public static Spanned setBold (String s) {
        return Start(s).setBold().Format();
    }

    public static Spanned setItalic (String s) {
        return Start(s).setItalic().Format();
    }

    public static Spanned setUnderline (String s) {
        return Start(s).setUnderline().Format();
    }
}
