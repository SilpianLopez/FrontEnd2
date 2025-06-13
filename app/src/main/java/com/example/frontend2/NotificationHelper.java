package com.example.frontend2;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.models.CleaningRoutine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationHelper {

    public static void requestNotificationPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    public static void scheduleNextAlarm(Context context, int userId) {
        CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
        Call<CleaningRoutine> call = api.getNextAlarmRoutine(userId);

        call.enqueue(new Callback<CleaningRoutine>() {
            @Override
            public void onResponse(Call<CleaningRoutine> call, Response<CleaningRoutine> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CleaningRoutine routine = response.body();
                    String title = routine.getTitle();
                    String nextDate = routine.getNext_due_date();

                    long dDay = calculateDDay(nextDate);
                    String message = String.format("'%s' 청소가 D-%d 남았습니다! 미리 준비해보세요 🧹", title, dDay);

                    scheduleAlarm(context, nextDate, message);
                } else {
                    Log.d("Alarm", "다가오는 루틴 없음.");
                }
            }

            @Override
            public void onFailure(Call<CleaningRoutine> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private static void scheduleAlarm(Context context, String nextDateStr, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date nextDate = sdf.parse(nextDateStr);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmTask.class);
            intent.putExtra("message", message);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextDate.getTime(), pendingIntent);
                        } else {
                            Log.w("Alarm", "정확한 알람 권한 없음");
                        }
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextDate.getTime(), pendingIntent);
                    }
                } catch (SecurityException e) {
                    Log.e("Alarm", "정확한 알람 권한 부족으로 예약 실패", e);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static long calculateDDay(String dueDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dueDate = sdf.parse(dueDateStr);
            long diffMillis = dueDate.getTime() - System.currentTimeMillis();
            return TimeUnit.MILLISECONDS.toDays(diffMillis);
        } catch (Exception e) {
            return 0;
        }
    }
}
