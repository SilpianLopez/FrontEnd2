package com.example.frontend2;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast; // Toast import 추가 (에러 메시지 사용자에게 표시용)

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.models.CleaningRoutine;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper"; // 로그 태그 추가

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
                    String nextDateStr = routine.getNext_due_date(); // 변수명 nextDateStr로 변경

                    // 🌟🌟🌟 수정된 부분: nextDateStr가 null이거나 비어있는지 먼저 확인 🌟🌟🌟
                    if (nextDateStr == null || nextDateStr.trim().isEmpty()) {
                        Log.d(TAG, "다음 알람을 위한 next_due_date가 null이거나 비어있습니다. 알람을 스케줄링하지 않습니다.");
                        // Toast.makeText(context, "다음 알람 날짜 정보가 없어 알림을 설정할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return; // 알람 스케줄링 없이 종료
                    }

                    // calculateDDay도 nextDateStr이 null일 경우를 대비해 early exit 로직을 넣는 것이 좋습니다.
                    long dDay = calculateDDay(nextDateStr);

                    String message;
                    if (dDay > 0) {
                        message = String.format("'%s' 청소가 D-%d 남았습니다! 미리 준비해보세요 🧹", title, dDay);
                    } else if (dDay == 0) {
                        message = String.format("오늘 '%s' 청소하는 날입니다! ⏰", title);
                    } else { // dDay < 0 (이미 지난 날짜)
                        message = String.format("'%s' 청소가 %d일 지났습니다. 청소할 시간이에요! 🚨", title, Math.abs(dDay));
                    }


                    scheduleAlarm(context, nextDateStr, message);
                } else {
                    Log.d(TAG, "다가오는 루틴 없음 또는 서버 응답 실패. 코드: " + response.code());
                    // 오류 응답 바디 로그 추가 (디버깅용)
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<CleaningRoutine> call, Throwable t) {
                Log.e(TAG, "알람 루틴 API 호출 실패", t); // 태그 사용
                // Toast.makeText(context, "알림 설정 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void scheduleAlarm(Context context, String nextDateStr, String message) {
        // SimpleDateFormat의 예외 처리를 강화합니다.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            // nextDateStr은 이미 scheduleNextAlarm에서 null/empty 체크를 거쳤지만,
            // 다른 경로로 호출될 가능성을 대비해 한 번 더 체크하는 것도 좋습니다.
            if (nextDateStr == null || nextDateStr.trim().isEmpty()) {
                Log.e(TAG, "scheduleAlarm: nextDateStr이 null이거나 비어있어 알람을 스케줄링할 수 없습니다.");
                Toast.makeText(context, "알림 설정 오류: 날짜 정보 부족", Toast.LENGTH_SHORT).show();
                return;
            }

            Date nextDate = sdf.parse(nextDateStr); // 🌟 이 라인이 이전 72번 라인일 가능성이 높습니다. 🌟

            // 알람 시간을 좀 더 현실적으로 설정:
            // nextDate는 'yyyy-MM-dd' 형식으로, 시간이 00:00:00으로 설정됩니다.
            // 알람이 당일 자정에 울리도록 하거나, 특정 시간을 더하여 설정할 수 있습니다.
            // 여기서는 일단 받은 날짜의 자정으로 설정하겠습니다.

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmTask.class); // AlarmTask 대신 AlarmReceiver 사용 권장
            intent.putExtra("notification_message", message);
            intent.putExtra("notification_title", "청소 알림"); // 알림 제목 추가 (필요시)
            // 알람 고유 ID를 루틴 ID 등으로 설정하면 좋습니다. (현재는 0 고정)
            // intent.putExtra("routine_id", routine.getRoutine_id());

            // PendingIntent 생성 시 FLAG_IMMUTABLE 또는 FLAG_UPDATE_CURRENT 사용
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, nextDateStr.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                try {
                    // 정확한 알람 설정 (API Level S 이상)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextDate.getTime(), pendingIntent); // Doze 모드에서도 동작
                            Log.d(TAG, "정확한 알람 예약 성공: " + nextDateStr + " - " + message);
                        } else {
                            Log.w(TAG, "정확한 알람 권한 없음. 일반 setExact 사용.");
                            Toast.makeText(context, "정확한 알림 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                            // 권한이 없으면 setExact (정확도 낮음) 또는 set (가장 부정확) 사용
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextDate.getTime(), pendingIntent);
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // API 23 (Marshmallow) 이상에서는 Doze 모드 고려
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextDate.getTime(), pendingIntent);
                        Log.d(TAG, "정확한 알람 (Doze 허용) 예약 성공: " + nextDateStr + " - " + message);
                    } else {
                        // 그 외 버전
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextDate.getTime(), pendingIntent);
                        Log.d(TAG, "정확한 알람 예약 성공 (이전 버전): " + nextDateStr + " - " + message);
                    }
                } catch (SecurityException e) {
                    Log.e(TAG, "정확한 알람 권한 부족으로 예약 실패. Manifest 확인: " + Manifest.permission.SCHEDULE_EXACT_ALARM, e);
                    Toast.makeText(context, "알림 권한이 필요합니다. 설정에서 정확한 알림을 허용해주세요.", Toast.LENGTH_LONG).show();
                } catch (Exception e) { // 기타 예상치 못한 예외 처리
                    Log.e(TAG, "알람 스케줄링 중 알 수 없는 오류 발생: " + e.getMessage(), e);
                    Toast.makeText(context, "알림 설정 중 알 수 없는 오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "AlarmManager를 가져올 수 없습니다.");
                Toast.makeText(context, "알림 서비스 초기화 오류", Toast.LENGTH_SHORT).show();
            }

        } catch (ParseException e) {
            Log.e(TAG, "날짜 파싱 실패: " + nextDateStr + ", 오류: " + e.getMessage(), e);
            Toast.makeText(context, "알림 날짜 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private static long calculateDDay(String dueDateStr) {
        // 🌟🌟🌟 수정된 부분: Null 또는 빈 문자열 체크 추가 🌟🌟🌟
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            Log.w(TAG, "calculateDDay: Due date string is null or empty. Returning 0.");
            return 0; // Null 또는 빈 문자열이면 0 반환
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dueDate = sdf.parse(dueDateStr);
            // 현재 시간을 자정으로 맞춤 (D-Day 계산의 기준)
            SimpleDateFormat todaySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date today = todaySdf.parse(todaySdf.format(new Date()));

            long diffMillis = dueDate.getTime() - today.getTime(); // 오늘 날짜와의 차이 계산
            return TimeUnit.MILLISECONDS.toDays(diffMillis);
        } catch (ParseException e) {
            Log.e(TAG, "D-Day 날짜 파싱 실패: " + dueDateStr, e);
            return 0;
        } catch (Exception e) { // 기타 예상치 못한 예외 처리
            Log.e(TAG, "D-Day 계산 중 오류 발생: " + e.getMessage(), e);
            return 0;
        }
    }
}
