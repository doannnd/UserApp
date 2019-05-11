package com.nguyendinhdoan.userapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nguyendinhdoan.userapp.R;
import com.nguyendinhdoan.userapp.utils.NotificationUtils;

/**
 * this user app, we don't need code here
 */
public class MyFirebaseMessaging extends FirebaseMessagingService {

    public static final String MESSAGE_KEY = "MESSAGE_KEY";
    public static final String MESSAGE_DRIVER_KEY = "MESSAGE_DRIVER_KEY";
    public static final String BODY_KEY = "BODY_KEY";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            switch (title) {
                case "cancel":
               /* Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // show message on user interface
                        Toast.makeText(MyFirebaseMessaging.this, message, Toast.LENGTH_SHORT).show();
                    }
                });*/

                    sendMessageCancelToUserActivity("cancel", remoteMessage.getNotification().getBody());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showArrivedNotificationAPI26(remoteMessage.getNotification().getBody());
                    } else {
                        showArrivedNotification(remoteMessage.getNotification().getBody());
                    }
                    break;
                case "accept":
                    sendMessageToUserActivity("accept");
                    break;
                case "Arrived":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showArrivedNotificationAPI26(remoteMessage.getNotification().getBody());
                    } else {
                        showArrivedNotification(remoteMessage.getNotification().getBody());
                    }
                    break;
                case "DropOff":
                    //openRateDriverActivity(remoteMessage.getNotification().getBody());
                    sendMessageToUserActivity("DropOff");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showArrivedNotificationAPI26(remoteMessage.getNotification().getBody());
                    } else {
                        showArrivedNotification(remoteMessage.getNotification().getBody());
                    }
                    break;
                case "cancelTrip":
                    String message1 = "The driver has canceled the trip for some reason, please find another driver";
                    sendMessageCancelToUserActivity("cancelTrip", remoteMessage.getNotification().getBody());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showArrivedNotificationAPI26(message1);
                    } else {
                        showArrivedNotification(message1);
                    }
                    break;
            }
        }

    }

    private void sendMessageCancelToUserActivity(String message, String body) {
        Intent intent = new Intent(MESSAGE_DRIVER_KEY);
        intent.putExtra(MESSAGE_KEY, message);
        intent.putExtra(BODY_KEY, body);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageToUserActivity(String message) {
        Intent intent = new Intent(MESSAGE_DRIVER_KEY);
        intent.putExtra(MESSAGE_KEY, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /*private void openRateDriverActivity(String body) {
        Intent intent = new Intent(this, RateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }*/

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0,
                new Intent(), PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationUtils notificationUtils = new NotificationUtils(getBaseContext());
        Notification.Builder builder = notificationUtils.getUberNotification(
                "Arrived", body, contentIntent, defaultSound
        );

        notificationUtils.getManager().notify(1, builder.build());
    }

    private void showArrivedNotification(String body) {
        // this code only work for android API 25 and below
        // from android API 26 or higher, you need create Notification chanel
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0,
                new Intent(), PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
}
