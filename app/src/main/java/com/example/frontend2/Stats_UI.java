package com.example.frontend2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningLogApi;
import com.example.frontend2.models.MonthlyLogStat;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Stats_UI extends AppCompatActivity {

    private BarChart barChart;
    private CleaningLogApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_ui);  // 👉 수아님 XML 파일명 맞춰서 그대로 유지

        barChart = findViewById(R.id.barchart);
        api = ApiClient.getClient().create(CleaningLogApi.class);

        // ✅ 로그인에서 저장한 SharedPreferences에서 userId 불러오기
        SharedPreferences prefs = getSharedPreferences(Login_UI.PREFS_NAME_FOR_APP, MODE_PRIVATE);
        int userId = prefs.getInt(Login_UI.KEY_USER_ID_FOR_APP, -1);

        if (userId != -1) {
            fetchMonthlyStats(userId);
        } else {
            Log.e("통계", "로그인 정보 없음");
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
                t.printStackTrace();
            }
        });
    }

    private void showBarChart(List<MonthlyLogStat> stats) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            MonthlyLogStat stat = stats.get(i);
            entries.add(new BarEntry(i, stat.getCount()));
            labels.add(stat.getMonth());
        }

        BarDataSet dataSet = new BarDataSet(entries, "월별 청소 횟수");
        dataSet.setColor(getResources().getColor(R.color.teal_200));  // 👉 색상도 조금 더 보기좋게 설정
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);  // 바 두께 약간 조절
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size(), true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChart.setFitBars(true);   // 바폭 자동 조정
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);  // 오른쪽 Y축 제거
        barChart.animateY(1000);  // 애니메이션 추가
        barChart.invalidate();
    }
}
