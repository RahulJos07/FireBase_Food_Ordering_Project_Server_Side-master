package com.gps.rahul.admin.firebase_mini_project_server_side.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.gps.rahul.admin.firebase_mini_project_server_side.Common.Common;
import com.gps.rahul.admin.firebase_mini_project_server_side.Model.RequestModel;
import com.gps.rahul.admin.firebase_mini_project_server_side.OrderStatus;
import com.gps.rahul.admin.firebase_mini_project_server_side.R;

import java.util.Random;

public class ListenOrder extends Service implements ChildEventListener{

    DatabaseReference databaseReference;
    public ListenOrder() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        databaseReference=FirebaseDatabase.getInstance().getReference("Requests");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        databaseReference.addChildEventListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        //Trigger Here
        RequestModel requestModel=dataSnapshot.getValue(RequestModel.class);
        if(requestModel.getStatus().equals("0")) {
            showNotification(dataSnapshot.getKey(), requestModel);
        }
    }

    private void showNotification(String key, RequestModel requestModel) {
        Intent intent=new Intent(getBaseContext(), OrderStatus.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(getBaseContext(),0,intent,0);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker("RJ")
                .setContentInfo("New Order")
                .setContentText("You have new order #"+key)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager=(NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        int randomInt=new Random().nextInt(9999-1)+1;
        notificationManager.notify(randomInt,builder.build());
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
