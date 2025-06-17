package com.example.frontend2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningLogApi;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.models.CleaningLogRecommendation;
import com.example.frontend2.models.MonthlyLogStat;
import com.example.frontend2.models.RecommendationRoutineRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Stats_UI extends AppCompatActivity {

    private BarChart barChart;
    private TextView encouragementTextView;
    private LinearLayout recommendationContainer;

    private CleaningLogApi api;
    private CleaningRoutineApi routineApi;

    private int userId;

    public static final String PREFS_NAME = "CleanItAppPrefs";
    public static final String KEY_USER_ID = "logged_in_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_ui);

        Toolbar toolbar = findViewById(R.id.toolbar_stats);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        barChart = findViewById(R.id.barchart);
        encouragementTextView = findViewById(R.id.encouragementTextView);
        recommendationContainer = findViewById(R.id.recommendationContainer);

        api = ApiClient.getClient().create(CleaningLogApi.class);
        routineApi = ApiClient.getClient().create(CleaningRoutineApi.class);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getInt(KEY_USER_ID, -1);

        Log.d("Stats_UI", "현재 userId = " + userId);

        if (userId != -1) {
            fetchMonthlyStats(userId);
            fetchSpaceStats(userId);
            fetchRecommendations(userId);
        } else {
            Log.e("통계", "로그인 정보 없음");
            Toast.makeText(this, "로그인 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchMonthlyStats(int userId) {
        api.getMonthlyLogs(userId).enqueue(new Callback<List<MonthlyLogStat>>() {
            @Override
            public void onResponse(Call<List<MonthlyLogStat>> call, Response<List<MonthlyLogStat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showBarChart(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<MonthlyLogStat>> call, Throwable t) {
                Log.e("통계", "월별 통계 호출 실패", t);
            }
        });
    }

    private void showBarChart(List<MonthlyLogStat> stats) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        float maxCount = 0;

        for (int i = 0; i < stats.size(); i++) {
            MonthlyLogStat stat = stats.get(i);
            float count = stat.getCount();
            entries.add(new BarEntry(i, count));
            labels.add(stat.getMonth());
            if (count > maxCount) maxCount = count;
        }

        BarDataSet dataSet = new BarDataSet(entries, "월별 청소 횟수");
        dataSet.setColor(getResources().getColor(R.color.teal_200));
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);
        barChart.setData(barData);

        // X축 설정
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size(), true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        // Y축 스케일 안정화
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setGranularity(1f);
        yAxis.setGranularityEnabled(true);
        yAxis.setAxisMaximum(maxCount + 1);  // 자동 최대값 조정

        barChart.getAxisRight().setEnabled(false);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void fetchSpaceStats(int userId) {
        api.getSpaceLogs(userId).enqueue(new Callback<Map<String, Integer>>() {
            @Override
            public void onResponse(Call<Map<String, Integer>> call, Response<Map<String, Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    generateEncouragement(response.body());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Integer>> call, Throwable t) {
                Log.e("통계", "공간별 통계 호출 실패", t);
            }
        });
    }

    private void generateEncouragement(Map<String, Integer> spaceStats) {
        String leastCleanedSpace = null;
        int minCount = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : spaceStats.entrySet()) {
            if (entry.getValue() < minCount) {
                minCount = entry.getValue();
                leastCleanedSpace = entry.getKey();
            }
        }

        if (leastCleanedSpace != null) {
            String message = String.format("이번달에는 '%s' 청소가 가장 적어요! 한 번 정리해보는 건 어떨까요? 🧹", leastCleanedSpace);
            encouragementTextView.setText(message);
        }
    }

    private void fetchRecommendations(int userId) {
        api.getRecommendations(userId).enqueue(new Callback<List<CleaningLogRecommendation>>() {
            @Override
            public void onResponse(Call<List<CleaningLogRecommendation>> call, Response<List<CleaningLogRecommendation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("RECOMMEND_API", "추천 응답: " + response.body().size() + "개");
                    showRecommendations(response.body());
                } else {
                    Log.e("RECOMMEND_API", "추천 결과가 없습니다.");
                }
            }

            @Override
            public void onFailure(Call<List<CleaningLogRecommendation>> call, Throwable t) {
                Log.e("RECOMMEND_API", "추천 호출 실패", t);
            }
        });
    }

    private void showRecommendations(List<CleaningLogRecommendation> list) {
        recommendationContainer.removeAllViews();

        for (CleaningLogRecommendation rec : list) {
            TextView tv = new TextView(this);
            tv.setText(rec.getSpace_name() + " 청소를 다시 해보는 건 어떨까요?");
            tv.setPadding(8, 16, 8, 16);
            tv.setTextSize(16f);
            tv.setOnClickListener(v -> createRoutineFromRecommendation(rec));
            recommendationContainer.addView(tv);
        }
    }

    private void createRoutineFromRecommendation(CleaningLogRecommendation rec) {
        RecommendationRoutineRequest request = new RecommendationRoutineRequest();
        request.setUser_id(userId);
        request.setSpace_id(rec.getSpace_id());
        request.setTitle(rec.getSpace_name() + " 청소");
        request.setRepeat_unit("WEEK");
        request.setRepeat_interval(1);

        routineApi.createRecommendationRoutine(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(Stats_UI.this, "루틴에 추가되었습니다!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Stats_UI.this, "루틴 추가 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
