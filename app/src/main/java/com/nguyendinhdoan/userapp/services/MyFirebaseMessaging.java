package com.nguyendinhdoan.userapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nguyendinhdoan.userapp.R;

/**
 * this user app, we don't need code here
 */
public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle().equals("cancel")) {
                final String message = remoteMessage.getNotification().getBody();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // show message on user interface
                        Toast.makeText(MyFirebaseMessaging.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (remoteMessage.getNotification().getTitle().equals("accept")) {
                showArrivedNotification(remoteMessage.getNotification().getBody());
            }
        }

    }

    private void showArrivedNotification(String body) {
        // this code only work for android API 25 and below
        // from android API 26 or higher, you need create Notification chanel
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0,
                new Intent(), PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
}