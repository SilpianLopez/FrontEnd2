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
import java.util.Calendar;
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

    // ✅ 예정된 루틴 예약 (기존 방식 개선)
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


                    // 👉 여기 추가
                    Log.d("Alarm", "다음 예약일: " + nextDate);

                    if (nextDate == null || nextDate.isEmpty()) {
                        Log.e("Alarm", "다음 예정일이 없습니다. 알람 예약 생략");
                        return;
                    }

                    long dDay = calculateDDay(nextDate);
                    String message;

                    if (dDay > 0) {
                        message = String.format("'%s' 청소가 D-%d 남았습니다! 미리 준비해보세요 🧹", title, dDay);
                    } else if (dDay == 0) {
                        message = String.format("'%s' 청소 예정일입니다! 오늘 청소를 해보는 건 어떨까요? 🧹", title);
                    } else {
                        message = String.format("'%s' 청소 예정일이 지났습니다. 다시 스케줄을 확인해보세요.", title);
                    }

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

    // ✅ 완료된 루틴 다음 주기 예약 (30일 뒤 알림)
    public static void scheduleCompletedRoutineAlarm(Context context, String title, String completedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date completed = sdf.parse(completedDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(completed);
            calendar.add(Calendar.DATE, 30);

            String nextDateStr = sdf.format(calendar.getTime());
            String message = String.format("'%s' 청소를 완료하셨습니다! 다음 청소까지 30일 남았습니다 ✅", title);

            scheduleAlarm(context, nextDateStr, message);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // ✅ 알람 실제 스케줄 예약 (null 방어 포함)
    public static void scheduleAlarm(Context context, String dateStr, String message) {
        if (dateStr == null || dateStr.isEmpty()) {
            Log.e("scheduleAlarm", "알람 예약 실패: 날짜 문자열이 null 또는 빈 문자열입니다.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date targetDate = sdf.parse(dateStr);
            long triggerTime = targetDate.getTime();

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmTask.class);
            intent.putExtra("message", message);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // ✅ D-Day 계산 (null 방어 포함)
    private static long calculateDDay(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.isEmpty()) {
            return 0;
        }
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
