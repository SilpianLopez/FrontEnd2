package com.example.frontend2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.Space;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutineMainActivity extends AppCompatActivity {

    private static final String TAG = "RoutineMainActivity";
    public static final int SPACE_ID_FOR_ALL_RECOMMENDATIONS = 0;

    private List<Space> userSpaceList = new ArrayList<>();
    private SpaceApi spaceApiService;
    private int currentUserId = -1;

    public static final String PREFS_NAME = "CleanItAppPrefs";
    public static final String KEY_USER_ID = "logged_in_user_id";

    private Button btnAllRoutine;
    private LinearLayout routineButtonContainer;
    private TextView tvNoSpacesMessage;

    private ActivityResultLauncher<Intent> routineActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_main);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);
        Log.d(TAG, "최종 user_id (onCreate): " + currentUserId);

        if (currentUserId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, Login_UI.class));
            finish();
            return;
        }

        btnAllRoutine = findViewById(R.id.btnAllRoutine);
        routineButtonContainer = findViewById(R.id.routineButtonContainer);
        tvNoSpacesMessage = findViewById(R.id.tvNoSpacesMessage);

        routineActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Toast.makeText(this, "루틴이 성공적으로 반영되었습니다!", Toast.LENGTH_SHORT).show();
                        refreshSpaceList();
                    }
                }
        );

        spaceApiService = ApiClient.getSpaceApi();
        if (spaceApiService == null) {
            Log.e(TAG, "SpaceApi service is null. Check ApiClient setup.");
            Toast.makeText(this, "네트워크 서비스 초기화 오류", Toast.LENGTH_SHORT).show();
            displayNoSpacesMessage("서비스 오류로 공간을 불러올 수 없습니다.");
            return;
        }

        setupNavigationAndGlobalButtons();
        refreshSpaceList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);
        Log.d(TAG, "최종 user_id (onResume): " + currentUserId);

        if (currentUserId != -1) {
        }
    }

    // ✅ 항상 안전하게 초기화해서 새로 불러오는 함수 분리
    private void refreshSpaceList() {
        userSpaceList.clear();
        tvNoSpacesMessage.setVisibility(View.GONE);
        routineButtonContainer.removeAllViews();

        spaceApiService.getSpacesByUserId(currentUserId).enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(@NonNull Call<List<Space>> call, @NonNull Response<List<Space>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userSpaceList.addAll(response.body());
                    Log.d(TAG, "공간 로드 성공: " + userSpaceList.size() + "개");

                    if (userSpaceList.isEmpty()) {
                        displayNoSpacesMessage("등록된 공간이 없습니다.");
                    } else {
                        createDynamicSpaceButtons();
                    }
                } else {
                    displayNoSpacesMessage("공간 목록을 가져오는 데 실패했습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Space>> call, @NonNull Throwable t) {
                displayNoSpacesMessage("네트워크 오류로 공간을 가져올 수 없습니다.");
            }
        });
    }

    private void createDynamicSpaceButtons() {
        routineButtonContainer.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        for (Space space : userSpaceList) {
            Button btn = new Button(this);
            btn.setText(space.getName() + " AI 추천");
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> startNextAiActivity(space.getName(), space.getSpace_id(), currentUserId, RoutineDetailActivity.class));
            routineButtonContainer.addView(btn);
        }
    }

    private void displayNoSpacesMessage(String message) {
        routineButtonContainer.removeAllViews();
        tvNoSpacesMessage.setText(message);
        tvNoSpacesMessage.setVisibility(View.VISIBLE);
    }

    private void setupNavigationAndGlobalButtons() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navAi = findViewById(R.id.navAi);

        navHome.setOnClickListener(v -> navigateTo(Main_UI.class, true));
        navCalendar.setOnClickListener(v -> navigateTo(CalendarActivity.class, true));
        navAi.setOnClickListener(v -> { /* 현재 AI 화면 → 아무것도 안함 */ });

        btnAllRoutine.setOnClickListener(v -> {
            startNextAiActivity("전체 공간", SPACE_ID_FOR_ALL_RECOMMENDATIONS, currentUserId, RoutineAllActivity.class);
        });
    }

    private void startNextAiActivity(String roomName, int spaceId, int userId, Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(KEY_USER_ID, userId);
        intent.putExtra("roomName", roomName);
        intent.putExtra("spaceId", spaceId);
        routineActivityResultLauncher.launch(intent);
    }

    private void navigateTo(Class<?> cls, boolean finishCurrent) {
        startActivity(new Intent(this, cls));
        if (finishCurrent) finish();
    }
}
