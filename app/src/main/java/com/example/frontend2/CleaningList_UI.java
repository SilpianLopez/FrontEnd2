package com.example.frontend2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.CleaningListAdapter;
import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.RoutineApi;
import com.example.frontend2.models.CleaningRoutine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningList_UI extends AppCompatActivity implements CleaningListAdapter.OnCleaningEditListener {

    private static final String TAG = "CleaningList_UI";
    private Toolbar toolbar;
    private String spaceNameFromIntent;
    private int spaceIdFromIntent = -1;
    private int currentUserId = -1;

    private RecyclerView recyclerView;
    private CleaningListAdapter adapter;
    private List<CleaningRoutine> routineList = new ArrayList<>();
    private ImageView btnAddRoutine;

    private RoutineApi routineApiService;

    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_USER_ID = "user_id";

    private ActivityResultLauncher<Intent> addOrEditRoutineLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_list_ui);

        // Intent에서 space_name, space_id, userId 가져오기
        Intent intent = getIntent();
        if (intent != null) {
            spaceNameFromIntent = intent.getStringExtra("space_name");
            spaceIdFromIntent = intent.getIntExtra("space_id", -1);
            currentUserId = intent.getIntExtra("userId", -1);
        }
        // userId가 없으면 SharedPreferences에서 시도
        if (currentUserId == -1) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentUserId = prefs.getInt(KEY_USER_ID, -1);
        }
        if (currentUserId == -1) {
            Toast.makeText(this, "사용자 정보가 없습니다. 로그인 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "User ID not available in CleaningList_UI.");
            finish();
            return;
        }
        if (spaceIdFromIntent == -1) {
            Toast.makeText(this, "공간 정보가 없어 루틴을 표시할 수 없습니다.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Space ID not passed or invalid: " + spaceIdFromIntent);
            finish();
            return;
        }
        Log.d(TAG, "Displaying routines for spaceId=" + spaceIdFromIntent + ", spaceName=" + spaceNameFromIntent + ", userId=" + currentUserId);

        // Toolbar 설정
        toolbar = findViewById(R.id.toolbar_clist);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    spaceNameFromIntent != null ? spaceNameFromIntent + " 루틴 목록" : "청소 루틴"
            );
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // RecyclerView + Adapter 초기화
        recyclerView = findViewById(R.id.rv_clist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CleaningListAdapter(this, routineList, this);
        recyclerView.setAdapter(adapter);

        // Retrofit 서비스 초기화
        if (ApiClient.getClient() != null) {
            routineApiService = ApiClient.getClient().create(RoutineApi.class);
        } else {
            Log.e(TAG, "ApiClient.getClient() is null. Check Retrofit setup.");
            Toast.makeText(this, "네트워크 초기화 오류입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 액티비티 결과 처리 런처 등록
        addOrEditRoutineLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Log.d(TAG, "Returned from CleaningAdd_UI with RESULT_OK. Refreshing.");
                        fetchRoutinesForSpace(currentUserId, spaceIdFromIntent);
                    } else {
                        Log.d(TAG, "Returned with result code: " + result.getResultCode());
                    }
                }
        );

        // 추가 버튼 클릭 리스너
        btnAddRoutine = findViewById(R.id.im_cadd);
        btnAddRoutine.setOnClickListener(v -> {
            Intent addIntent = new Intent(CleaningList_UI.this, CleaningAdd_UI.class);
            addIntent.putExtra("userId", currentUserId);
            addIntent.putExtra("spaceId", spaceIdFromIntent);
            addOrEditRoutineLauncher.launch(addIntent);
        });

        // 초기 데이터 로드
        fetchRoutinesForSpace(currentUserId, spaceIdFromIntent);
    }

    private void fetchRoutinesForSpace(int userId, int spaceId) {
        Log.d(TAG, "API 호출 getRoutinesByUserAndSpace(userId=" + userId + ", spaceId=" + spaceId + ")");
        Call<List<CleaningRoutine>> call = routineApiService.getRoutinesByUserAndSpace(userId, spaceId);
        call.enqueue(new Callback<List<CleaningRoutine>>() {
            @Override
            public void onResponse(@NonNull Call<List<CleaningRoutine>> call, @NonNull Response<List<CleaningRoutine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                    Log.d(TAG, "루틴 목록 로드 성공: " + response.body().size());
                    if (response.body().isEmpty()) {
                        Toast.makeText(CleaningList_UI.this, "등록된 루틴이 없습니다. '+' 버튼으로 추가하세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    handleApiError(response, "루틴 목록 불러오기 실패");
                    adapter.setItems(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CleaningRoutine>> call, @NonNull Throwable t) {
                handleApiFailure(t, "루틴 목록 서버 연결 오류");
                adapter.setItems(new ArrayList<>());
            }
        });
    }

    @Override
    public void onEditRequested(CleaningRoutine routineToEdit) {
        Log.d(TAG, "수정 요청: " + routineToEdit.getTitle());
        Intent editIntent = new Intent(this, CleaningAdd_UI.class);
        editIntent.putExtra("mode", "edit");
        editIntent.putExtra("userId", currentUserId);
        editIntent.putExtra("spaceId", routineToEdit.getSpace_id());
        editIntent.putExtra("routineIdToEdit", routineToEdit.getRoutine_id());
        editIntent.putExtra("currentTitle", routineToEdit.getTitle());
        editIntent.putExtra("currentDescription", routineToEdit.getDescription());
        editIntent.putExtra("currentRepeatUnit", routineToEdit.getRepeat_unit());
        if (routineToEdit.getRepeat_interval() != null) {
            editIntent.putExtra("currentRepeatInterval", routineToEdit.getRepeat_interval());
        }
        addOrEditRoutineLauncher.launch(editIntent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void handleApiError(Response<?> response, String defaultMessage) {
        String errorMessage = defaultMessage + " (코드: " + response.code() + ")";
        if (response.errorBody() != null) {
            try {
                errorMessage += "\n내용: " + response.errorBody().string();
            } catch (IOException e) { Log.e(TAG, "Error body parsing error", e); }
        }
        Log.e(TAG, errorMessage);
        Toast.makeText(this, defaultMessage, Toast.LENGTH_LONG).show();
    }

    private void handleApiFailure(Throwable t, String defaultMessage) {
        String failMessage = defaultMessage + ": " + t.getMessage();
        Log.e(TAG, failMessage, t);
        Toast.makeText(this, defaultMessage, Toast.LENGTH_LONG).show();
    }
}
