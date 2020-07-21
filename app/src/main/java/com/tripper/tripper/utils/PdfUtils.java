package com.tripper.tripper.utils;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

public class PdfUtils {

//    private static Document pdfDocument = null;
    private static File pdfFile = null;
    private static File pdfDir = null;
    private static String pdfFileName = "KeepTripPdfFile1.pdf";


    public static void saveToPdf2(View view, Context context){
        //First Check if the external storage is writable
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
        }

        //Create a directory for your PDF
        File pdfDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "KeepTrip");
        if (!pdfDir.exists()){
            pdfDir.mkdir();
        }

        //Then take the screen shot
//        Bitmap screen = loadBitmapFromView(view);
        Bitmap screen;
        view.setDrawingCacheEnabled(true);
        screen = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        //Now create the name of your PDF file that you will generate
        File pdfFile = new File(pdfDir, "myKeepTripPdfFile.pdf");


        try {
            Document  document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            screen.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            addImage(document,byteArray);
            document.close();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(pdfFile);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            context.startActivity(intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    public static void saveToPdf(View view, Context context){

//        if(pdfDocument == null){
//            init();
//        }

        init();
        try {
            addContent(view);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(new File(pdfDir, pdfFileName));
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            context.startActivity(intent);
        }
        catch (Exception e){

        }
    }

    private static void init() {
        //First Check if the external storage is writable
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            boolean result = false;
        }

        //Create a directory for your PDF
         pdfDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "KeepTrip");
        if (!pdfDir.exists()){
            pdfDir.mkdir();
        }

        //Now create the name of your PDF file that you will generate
         pdfFile = new File(pdfDir, pdfFileName);

//        try {
//            pdfDocument = new Document();
////            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
////            document.open();
////            document.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    //Adding the content to the document
    private static void addContent(View view) {
        try
        {
//            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
//            document.open();

            //Then take the screen shot
            Bitmap screen;
            view.setDrawingCacheEnabled(true);
            screen = Bitmap.createBitmap(view.getDrawingCache());

            view.setDrawingCacheEnabled(false);

            view.buildDrawingCache();
            Bitmap bmp = view.getDrawingCache();
            try {
                Document  document = new Document();

                PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                document.open();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                screen.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                addImage(document,byteArray);
                document.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        catch (Exception ex)
        {
            Log.e("TAG-ORDER PRINT ERROR", ex.getMessage());
        }
    }
    private static void addImage(Document document,byte[] byteArray)
    {
        Image image = null;
        try
        {
            image = Image.getInstance(byteArray);
        }
        catch (BadElementException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // image.scaleAbsolute(150f, 150f);
        try
        {
            document.add(image);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }

    //    public void saveImageToPDF(View title, Bitmap bitmap, String filename) {
//
//    File mFile = new File(new File(), filename + ".pdf");
//    if (!mFile.exists()) {
//        int height = title.getHeight() + bitmap.getHeight();
//        PdfDocument document = new PdfDocument();
//        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), height, 1).create();
//        PdfDocument.Page page = document.startPage(pageInfo);
//        Canvas canvas = page.getCanvas();
//        title.draw(canvas);
//
//        canvas.drawBitmap(bitmap, null, new Rect(0, title.getHeight(), bitmap.getWidth(),bitmap.getHeight()), null);
//
//        document.finishPage(page);
//
//        try {
//            mFile.createNewFile();
//            OutputStream out = new FileOutputStream(mFile);
//            document.writeTo(out);
//            document.close();
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
}
