package com.selectmultipleimages_demo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by SONU on 31/10/15.
 */
public class GridView_Adaptersecond extends BaseAdapter {
    private Context context;
    private ArrayList<String> imageUrls;
    private SparseBooleanArray mSparseBooleanArray;//Variable to store selected Images
    private DisplayImageOptions options;
    private boolean isCustomGalleryActivity;//Variable to check if gridview is to setup for Custom Gallery or not
    private boolean isGray;
    private int numberOfFace = 5;
    private FaceDetector myFaceDetect, myFaceDetectCrop;
    private FaceDetector.Face[] myFace, myFaceCrop;
    float myEyesDistance;
    int numberOfFaceDetected, numberOfFaceDetectedCrop;
    int[] up = new int[5];
    int[] down = new int[5];
    int[] height = new int[5];
    int counter, counter1 = 0;
    Matrix m = new Matrix();
    Bitmap binarymap = null;
    Bitmap BmCrop,BmCrop1;
    private IntentFilter filter;
    public static final String MY_BROADCAST_TAG = "com.example.localbroadcasttest";
    private ArrayList<Integer> eye_h;
    private ArrayList<Integer> eye_h1;
    private ArrayList<Integer> eye_h2;
    private ArrayList<String> readyCrop;
    PointF myMidPointCrop;
    FaceDetector.Face faceCrop;
    float myEyesDistance1;
    int max_i = 0, max_i1;

    public GridView_Adaptersecond(Context context, ArrayList<String> imageUrls, boolean isCustomGalleryActivity, boolean isGray) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.isCustomGalleryActivity = isCustomGalleryActivity;
        mSparseBooleanArray = new SparseBooleanArray();
        this.isGray = isGray;


        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .resetViewBeforeLoading(true).cacheOnDisk(true)
                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        eye_h = new ArrayList<Integer>();
        eye_h1 = new ArrayList<Integer>();
        eye_h2 = new ArrayList<Integer>();
    }

    //Method to return selected Images
    public ArrayList<String> getCheckedItems() {
        ArrayList<String> mTempArry = new ArrayList<String>();

        for (int i = 0; i < imageUrls.size(); i++) {
            if (mSparseBooleanArray.get(i)) {
                mTempArry.add(imageUrls.get(i));
            }
        }

        return mTempArry;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public Object getItem(int i) {
        return imageUrls.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.customgridview_item, viewGroup, false);//Inflate layout

            CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.selectCheckBox);
            final ImageView imageView = (ImageView) view.findViewById(R.id.galleryImageView);
            Bitmap bm = null;

            File imgFile = new File(imageUrls.get(position));
            if (imgFile.exists()) {
                BitmapFactory.Options bitmapLoadingOptions = new BitmapFactory.Options();
                bitmapLoadingOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bitmapLoadingOptions);

            }
            int imageWidth = bm.getWidth();
            int imageHeight = bm.getHeight();


            m.setRotate(-90);
            bm = Bitmap.createBitmap(bm, 0, 0, imageWidth, imageHeight, m, true);

            Bitmap tempbitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.RGB_565);
            myFace = new FaceDetector.Face[numberOfFace];
            myFaceDetect = new FaceDetector(bm.getWidth(), bm.getHeight(), numberOfFace);
            numberOfFaceDetected = myFaceDetect.findFaces(bm, myFace);
            System.out.println("numberOfFaceDetected" + numberOfFaceDetected);
            binarymap = bm.copy(Bitmap.Config.ARGB_8888, true);
            for (int i = 0; i < bm.getWidth(); i++) {
                for (int j = 0; j < bm.getHeight(); j++) {
                    int col = binarymap.getPixel(i, j);
                    int alpha = col & 0xFF000000;
                    int red = (col & 0x00FF0000) >> 16;
                    int green = (col & 0x0000FF00) >> 8;
                    int blue = (col & 0x000000FF);
                    int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                    if (gray <= 100) {
                        gray = 0;
                    } else {
                        gray = 255;
                    }
                    int newColor = alpha | (gray << 16) | (gray << 8) | gray;
                    binarymap.setPixel(i, j, newColor);
                }
            }

            Canvas canvas = new Canvas(tempbitmap);
            canvas.drawBitmap(bm, 0, 0, null);

            Paint myPaint = new Paint();
            myPaint.setColor(Color.GREEN);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(3);
            Paint myPaint1 = new Paint();
            myPaint1.setColor(Color.BLUE);
            myPaint1.setStyle(Paint.Style.STROKE);
            myPaint1.setStrokeWidth(3);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6);
            for (int k = 0; k < numberOfFaceDetected; k++) {
                FaceDetector.Face face = myFace[k];
                PointF myMidPoint = new PointF();
                face.getMidPoint(myMidPoint);
                myEyesDistance = face.eyesDistance();
                float x = myMidPoint.x;
                float y = myMidPoint.y;
                canvas.drawRect((int) (x - myEyesDistance * 2),
                        (int) (y - myEyesDistance * 2),
                        (int) (x + myEyesDistance * 2),
                        (int) (y + myEyesDistance * 2), myPaint);
                for (int a = (int) y; a < (int) (y + (myEyesDistance / 2.0)); a++) {
                    int col = binarymap.getPixel((int) (x - myEyesDistance / 2), a);
                    int red = (col & 0x00FF0000) >> 16;
                    int green = (col & 0x0000FF00) >> 8;
                    int blue = (col & 0x000000FF);
                    int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                    if (gray == 255) {
                        counter1++;
                    }
                    if (counter1 > 2) {
                        up[k] = a;
                        canvas.drawLine(x - myEyesDistance / 2, up[k], x + myEyesDistance / 2, up[k], myPaint1);
                        counter1 = 0;
                        a = (int) (y + (myEyesDistance / 2));
                    }
                }
                for (int a = (int) y; a > (int) (y - (myEyesDistance / 2)); a--) {
                    int col = binarymap.getPixel((int) (x - myEyesDistance / 2), a);
                    int red = (col & 0x00FF0000) >> 16;
                    int green = (col & 0x0000FF00) >> 8;
                    int blue = (col & 0x000000FF);
                    int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                    if (gray == 255) {
                        counter++;
                    }
                    if (counter > 2) {
                        down[k] = a;
                        canvas.drawLine(x - myEyesDistance / 2, down[k], x + myEyesDistance / 2, down[k], myPaint1);
                        counter = 0;
                        a = (int) (y - (myEyesDistance / 2));
                    }
                }
                height[k] = up[k] - down[k];
            }
            if (isCustomGalleryActivity) {
                System.out.println("isCustomGalleryActivity==true");
                imageView.setImageBitmap(tempbitmap);
                eye_h.add(height[0]);
                eye_h1.add(height[1]);
                eye_h2.add(height[2]);
                System.out.println("eye_h=" + eye_h);
                System.out.println("eye_h1=" + eye_h1);


            if (eye_h.size() == 2) {
                System.out.println("imageUrls.size()=" + imageUrls.size());

                int max = 0;
                for (int i = 0; i < eye_h.size(); i++) {
                    if (max < eye_h.get(i)) {
                        max_i = i;
                        max = eye_h.get(i);
                        System.out.println("max=" + max);
                    }
                }
                int max1 = 0;
                for (int i = 0; i < eye_h1.size(); i++) {
                    if (max1 < eye_h1.get(i)) {
                        max_i1 = i;
                        max1 = eye_h1.get(i);
                        System.out.println("max1=" + max1);
                    }
                }

                max_i1 = 1;
                System.out.println(" max_i=" + max_i);
                System.out.println(" max_i1=" + max_i1);

                ArrayList<String> uris= new ArrayList<>();
                uris.add(imageUrls.get(max_i));
                uris.add(imageUrls.get(max_i1));
                Intent intent = new Intent();
                intent.setAction(MY_BROADCAST_TAG);
                intent.putExtra("message", uris);

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


//
//                myFaceCrop = new FaceDetector.Face[numberOfFace];
//                myFaceDetectCrop = new FaceDetector(BmCrop1.getWidth(), BmCrop1.getHeight(), numberOfFace);
//                numberOfFaceDetectedCrop = myFaceDetectCrop.findFaces(BmCrop1, myFaceCrop);
//                System.out.println("numberOfFaceDetectedCrop=" + numberOfFaceDetectedCrop);
//
//                faceCrop = myFace[1];
//                myMidPointCrop = new PointF();
//                faceCrop.getMidPoint(myMidPointCrop);
//                myEyesDistance1 = faceCrop.eyesDistance();
//                float x = myMidPointCrop.x;
//                float y = myMidPointCrop.y;
//
//                System.out.println("x=" + x);
//                System.out.println("y=" + y);
//
//
//                 int[] pixels = new int[BmCrop1.getWidth()* BmCrop1.getHeight()];
//
//                        BmCrop.getPixels(pixels, 0, 1152, 700, 500, 200, 200);
//                        //String text = String.valueOf(pixels[10]);
//                        BmCrop1 = BmCrop1.copy(Bitmap.Config.ARGB_8888, true);
//                        BmCrop1.setPixels(pixels, 0, 1152, 700, 500, 200, 200);


//                        for (int i = (int) (x - myEyesDistance1 * 2); i < x + myEyesDistance1 * 2; i++) {
//                            for (int j = (int) (y - myEyesDistance1 * 2); j < y + myEyesDistance1 * 2; j++) {
//                                int color = BmCrop.getPixel(i, j);
//                                System.out.println("color="+color);
//                                BmCrop1 = BmCrop1.copy(Bitmap.Config.ARGB_8888, true);
//                                BmCrop1.setPixel(i, j, color);
//                            }
//                        }
            }}

            if (!isCustomGalleryActivity) {
                File img = new File(imageUrls.get(max_i));
                if (img.exists()) {
                    BitmapFactory.Options bitmapLoadingOptions = new BitmapFactory.Options();
                    bitmapLoadingOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                    BmCrop = BitmapFactory.decodeFile(img.getAbsolutePath(), bitmapLoadingOptions);
                    BmCrop = Bitmap.createBitmap(BmCrop, 0, 0, BmCrop.getWidth(), BmCrop.getHeight(), m, true);
                    System.out.println("BmCrop="+BmCrop);
                }

                File img1 = new File(imageUrls.get(max_i1));
                if (img1.exists()) {
                    BitmapFactory.Options bitmapLoadingOptions = new BitmapFactory.Options();
                    bitmapLoadingOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                    BmCrop1 = BitmapFactory.decodeFile(img1.getAbsolutePath(), bitmapLoadingOptions);
                    BmCrop1 = Bitmap.createBitmap(BmCrop1, 0, 0, BmCrop1.getWidth(), BmCrop1.getHeight(), m, true);
                    System.out.println("BmCrop1="+BmCrop1);
                }
               // BmCrop1.setPixel(i,j,0);
                System.out.println("isCustomGalleryActivity==false=="+BmCrop1);
                imageView.setImageBitmap(BmCrop);
            }


            // if (!isCustomGalleryActivity)
            mCheckBox.setVisibility(View.GONE);
            mCheckBox.setTag(position);//Set Tag for CheckBox
            mCheckBox.setChecked(mSparseBooleanArray.get(position));
            mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);
        }
        return view;
    }

    CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);//Insert selected checkbox value inside boolean array
            ((CustomGallery_Activity) context).showSelectButton();//call custom gallery activity method
        }
    };
}



