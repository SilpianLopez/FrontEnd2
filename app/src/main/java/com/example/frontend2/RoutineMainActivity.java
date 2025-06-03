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

    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_USER_ID = "current_user_id";

    private Button btnAllRoutine;
    private LinearLayout routineButtonContainer;
    private TextView tvNoSpacesMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_main);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "로그인이 필요합니다. 로그인 화면으로 이동합니다.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "User not logged in. currentUserId: " + currentUserId);
            Intent intent = new Intent(this, Login_UI.class);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "Current User ID: " + currentUserId);

        btnAllRoutine = findViewById(R.id.btnAllRoutine);
        routineButtonContainer = findViewById(R.id.routineButtonContainer);
        tvNoSpacesMessage = findViewById(R.id.tvNoSpacesMessage);

        if (ApiClient.getClient() != null) {
            spaceApiService = ApiClient.getClient().create(SpaceApi.class);
        } else {
            Log.e(TAG, "ApiClient.getClient() is null. Check Retrofit setup.");
            Toast.makeText(this, "네트워크 초기화 오류입니다. 앱을 재시작해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        setupNavigationAndGlobalButtons();

        if (currentUserId != -1) {
            fetchUserSpacesAndCreateDynamicButtons(currentUserId);
        }
    }

    private void setupNavigationAndGlobalButtons() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navAi = findViewById(R.id.navAi);

        navHome.setOnClickListener(v -> navigateTo(Main_UI.class, true));
        navCalendar.setOnClickListener(v -> navigateTo(CalendarActivity.class, true));
        navAi.setOnClickListener(v -> Toast.makeText(this, "AI 추천 기능 선택 화면입니다.", Toast.LENGTH_SHORT).show());

        btnAllRoutine.setOnClickListener(v -> {
            if (currentUserId == -1) {
                Toast.makeText(this, "로그인 정보가 없어 전체 추천을 받을 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            startRoutineDetailActivity("전체 공간", SPACE_ID_FOR_ALL_RECOMMENDATIONS, currentUserId, RoutineAllActivity.class);
        });
    }

    private void fetchUserSpacesAndCreateDynamicButtons(int userId) {
        Log.d(TAG, "사용자 공간 목록 요청 시작, userId: " + userId);
        if (tvNoSpacesMessage != null) tvNoSpacesMessage.setVisibility(View.GONE);
        routineButtonContainer.removeAllViews();

        Call<List<Space>> call = spaceApiService.getSpacesByUserId(userId);
        call.enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(@NonNull Call<List<Space>> call, @NonNull Response<List<Space>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userSpaceList = response.body();
                    Log.d(TAG, "사용자 공간 목록 로드 성공: " + userSpaceList.size() + "개");
                    createDynamicSpaceButtons();
                } else {
                    Log.e(TAG, "공간 목록 로드 실패: " + response.code());
                    if (tvNoSpacesMessage != null) {
                        tvNoSpacesMessage.setText("등록된 공간 정보를 가져오는 데 실패했습니다.");
                        tvNoSpacesMessage.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Space>> call, @NonNull Throwable t) {
                Log.e(TAG, "공간 목록 API 호출 실패", t);
                if (tvNoSpacesMessage != null) {
                    tvNoSpacesMessage.setText("네트워크 오류로 공간 목록을 가져올 수 없습니다.");
                    tvNoSpacesMessage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void createDynamicSpaceButtons() {
        routineButtonContainer.removeAllViews();

        if (userSpaceList == null || userSpaceList.isEmpty()) {
            Log.w(TAG, "동적으로 생성할 사용자 공간 정보가 없습니다.");
            if (tvNoSpacesMessage != null) {
                tvNoSpacesMessage.setText("등록된 공간이 없습니다. '+' 버튼을 눌러 공간을 추가해주세요.");
                tvNoSpacesMessage.setVisibility(View.VISIBLE);
            }
            return;
        }

        tvNoSpacesMessage.setVisibility(View.GONE);

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        int topMarginInDp = 16;
        int bottomMarginInDp = 8;
        float scale = getResources().getDisplayMetrics().density;
        buttonLayoutParams.setMargins(0,
                (int) (topMarginInDp * scale + 0.5f),
                0,
                (int) (bottomMarginInDp * scale + 0.5f));

        for (final Space space : userSpaceList) {
            Button spaceButton = new Button(this);
            spaceButton.setText(space.getName() + " AI 추천");
            spaceButton.setLayoutParams(buttonLayoutParams);
            spaceButton.setBackgroundResource(R.drawable.btn_outline_black);
            spaceButton.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            spaceButton.setTag(space);

            spaceButton.setOnClickListener(view -> {
                Space selectedSpace = (Space) view.getTag();
                if (currentUserId != -1) {
                    Log.d(TAG, "동적 공간 버튼 클릭: " + selectedSpace.getName());
                    startRoutineDetailActivity(selectedSpace.getName(), selectedSpace.getSpace_id(), currentUserId, RoutineDetailActivity.class);
                } else {
                    Toast.makeText(this, "사용자 정보가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            });

            routineButtonContainer.addView(spaceButton);
        }
    }

    private void startRoutineDetailActivity(String roomName, int spaceId, int userId, Class<?> destinationActivityClass) {
        Intent intent = new Intent(this, destinationActivityClass);
        intent.putExtra("roomName", roomName);
        intent.putExtra("spaceId", spaceId);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void navigateTo(Class<?> destinationClass, boolean finishCurrent) {
        Intent intent = new Intent(this, destinationClass);
        startActivity(intent);
        if (finishCurrent) finish();
    }
}