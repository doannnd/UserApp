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

import java.util.Objects;

/**
 * this user app, we don't need code here
 */
public class MyFirebaseMessaging extends FirebaseMessagingService {

    public static final String MESSAGE_KEY = "MESSAGE_KEY";
    public static final String MESSAGE_DRIVER_KEY = "MESSAGE_DRIVER_KEY";
    public static final String BODY_KEY = "BODY_KEY";
    public static final String CANCEL_TITLE = "cancel";
    public static final String ACCEPT_TITLE = "accept";
    public static final String ARRIVED_TITLE = "Arrived";
    public static final String DROP_OFF_TITLE = "DropOff";
    public static final String CANCEL_TRIP_TITLE = "cancelTrip";
    private static final int PENDING_REQUEST_CODE = 0;
    private static final int NOTIFY_ID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            switch (Objects.requireNonNull(title)) {
                case CANCEL_TITLE:
                    sendMessage(title);
                    notification(title, body);
                    break;
                case ACCEPT_TITLE:
                    notification(title, body);
                    break;
                case ARRIVED_TITLE:
                    notification(title, body);
                    break;
                case DROP_OFF_TITLE:
                    notification(title, body);
                    break;
                case CANCEL_TRIP_TITLE:
                    notification(title, body);
                    break;
            }
        }

    }

    private void notification(String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showArrivedNotificationAPI26(title, body);
        } else {
            showArrivedNotification(title, body);
        }
    }

    private void sendMessageCancelToUserActivity(String message, String body) {
        Intent intent = new Intent(MESSAGE_DRIVER_KEY);
        intent.putExtra(MESSAGE_KEY, message);
        intent.putExtra(BODY_KEY, body);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessage(String message) {
        Intent intent = new Intent(MESSAGE_DRIVER_KEY);
        intent.putExtra(MESSAGE_KEY, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String title, String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(
                getBaseContext(), PENDING_REQUEST_CODE,
                new Intent(), PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationUtils notificationUtils = new NotificationUtils(getBaseContext());
        Notification.Builder builder = notificationUtils.getUserNotification(
                title, body, contentIntent, defaultSound
        );

        notificationUtils.getManager().notify(NOTIFY_ID, builder.build());
    }

    private void showArrivedNotification(String title, String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(
                getBaseContext(), PENDING_REQUEST_CODE,
                new Intent(), PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getBaseContext()
                .getSystemService(NOTIFICATION_SERVICE);
        Objects.requireNonNull(manager).notify(NOTIFY_ID, builder.build());
    }
}
