package com.gps.rahul.admin.firebase_mini_project_server_side.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.gps.rahul.admin.firebase_mini_project_server_side.Model.LoginModel;
import com.gps.rahul.admin.firebase_mini_project_server_side.Model.RequestModel;
import com.gps.rahul.admin.firebase_mini_project_server_side.Remote.IGeoCoordinates;
import com.gps.rahul.admin.firebase_mini_project_server_side.Remote.RetrofitClient;

public class Common {
    public static LoginModel currentuser;
    public static RequestModel currentRequest;

    public static final String UPDATE="Update";
    public static final String DELETE="Delete";

    public static final int PICK_IMAGE_REQUEST=71;

    public static final String baseUrl = "https://maps.googleapis.com";

    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "On my way";
        else
            return "Shipped";
    }

    public static IGeoCoordinates getGeoCodeService()
    {
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap,int newWidth,int newHeight)
    {
        Bitmap scaledBitmap=Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);

        float scaleX=newWidth/(float)bitmap.getWidth();
        float scaleY=newHeight/(float)bitmap.getHeight();
        float pivotX=0,pivotY=0;

        Matrix scaleMatrix =new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas=new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }
}
