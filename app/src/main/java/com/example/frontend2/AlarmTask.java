package com.example.frontend2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class AlarmTask extends BroadcastReceiver {

    private static final String CHANNEL_ID = "alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");

        // ✅ 알림 채널 생성 (Android 8.0 이상 대응)
        createNotificationChannel(context);

        // ✅ 알림 빌더 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("청소 알림")
                .setContentText(message != null ? message : "청소할 시간이 다가왔습니다!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // ✅ 알림 권한 확인
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        } else {
            Log.w("AlarmTask", "알림 권한 없음 - 알림 생략됨");
        }
    }

    private void createNotificationChannel(Context context) {
        // Android 8.0 이상에서 채널 필요
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannelCompat channel = new NotificationChannelCompat.Builder(CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setName("청소 알림 채널")
                    .setDescription("다가오는 청소일 알림을 제공합니다.")
                    .build();

            NotificationManagerCompat.from(context).createNotificationChannel(channel);
        }
    }
}
