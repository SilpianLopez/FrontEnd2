package com.example.frontend2;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class Stats_UI extends AppCompatActivity {
    Toolbar toolbar;
    BarChart barchart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_ui);
        // 청소 통계 툴바
        toolbar = findViewById(R.id.toolbar_stats);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("청소 통계");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // <- 버튼 표시
        }
        // 막대그래프
        barchart = findViewById(R.id.barchart);
        // 데이터 준비
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 5)); // 거실 5회
        entries.add(new BarEntry(1f, 3)); // 철수의 방 3회
        entries.add(new BarEntry(2f, 6)); // 화장실 6회
        entries.add(new BarEntry(3f, 2)); // 옷방 2회

        BarDataSet dataSet = new BarDataSet(entries, "청소 횟수");
        dataSet.setColor(Color.parseColor("#4A90E2"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        barchart.setData(barData);
        barchart.invalidate();
        // BarChart 설정
        barchart.setData(barData);
        barchart.getDescription().setEnabled(false);
        barchart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"거실", "철수의 방", "화장실", "옷방"}));
        barchart.getLegend().setEnabled(false);
        barchart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"거실", "철수의 방", "화장실", "옷방"}));// TODO: 더미 라벨 - 공간 이름도 동적으로 받아오도록 수정
        barchart.getXAxis().setGranularity(1f);
        barchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barchart.getXAxis().setDrawGridLines(false);
        barchart.getAxisLeft().setAxisMinimum(0f);
        barchart.getAxisLeft().setAxisMaximum(10f);
        barchart.getAxisLeft().setGranularity(2f);
        barchart.getAxisLeft().setLabelCount(6, true);
        barchart.getAxisLeft().setDrawGridLines(true);
        barchart.getAxisRight().setEnabled(false);
        barchart.invalidate(); // 새로고침
    }
    // 툴바 <- 버튼 기능 구현
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return  true;
    }
}
