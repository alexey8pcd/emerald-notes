package ru.alexey_ovcharov.greenguide.mobile.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

/**
 * Created by Admin on 28.05.2017.
 */

class NotificationsHelper {

    private static final int NOTIFY_ID_PUBLIC = 101;
    private static final int NOTIFY_ID_UPDATE = 102;

    public static void sendPublicNotify(InteractStatus networkStatus, Context context, String title) {
        Log.d(APP_NAME, "Создаю уведомление об отправке");
        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        String textRes = "?";
        int icon = android.R.drawable.ic_dialog_alert;
        switch (networkStatus) {
            case SUCCESS:
                textRes = "Успешная отправка";
                icon = android.R.drawable.ic_dialog_info;
                break;
            case DB_ERROR:
            case CLIENT_ERROR:
                textRes = "Ошибка: неверный запрос";
                break;
            case SERVER_ERROR:
                textRes = "Ошибка сервера";
                break;
            case UNKNOWN:
                textRes = "Ошибка сети";
                break;
        }
        builder.setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(textRes);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID_PUBLIC, notification);
    }

    public static void sendUpdateNotify(InteractStatus networkStatus, Context context, String title) {
        Log.d(APP_NAME, "Создаю уведомление об обновлении");
        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        String textRes = "?";
        int icon = android.R.drawable.ic_dialog_alert;
        switch (networkStatus) {
            case SUCCESS:
                textRes = "Успешное обновление";
                icon = android.R.drawable.ic_dialog_info;
                break;
            case CLIENT_ERROR:
                textRes = "Ошибка: неверный запрос";
                break;
            case SERVER_ERROR:
                textRes = "Ошибка сервера";
                break;
            case UNKNOWN:
                textRes = "Ошибка сети";
                break;
            case DB_ERROR:
                textRes = "Ошибка обновления базы";
                break;
        }
        builder.setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(textRes);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID_PUBLIC, notification);
    }

}
