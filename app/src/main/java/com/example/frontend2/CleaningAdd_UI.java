package com.example.frontend2;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CleaningAdd_UI extends AppCompatActivity {
    Toolbar toolbar;
    Spinner spunit, spvalue;
    Button btnsave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_add_ui);
        // 청소 항목 추가 툴바
        toolbar = findViewById(R.id.toolbar_cadd);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("청소 항목 추가");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // <- 버튼 표시
        }
        // 청소 주기 스피너
        spunit = findViewById(R.id.sp_unit);
        spvalue = findViewById(R.id.sp_value);
        // 단위: 매일, 매주, 매달
        String[] unitItems = {"매일", "매주", "매달"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_white, unitItems);
        unitAdapter.setDropDownViewResource(R.layout.spinner_item_white);
        spunit.setAdapter(unitAdapter);
        // 숫자: 1 ~ 10일
        String[] valueitems = new String[10];
        for (int i = 0; i < 10; i++) {
            valueitems[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_white, valueitems);
        unitAdapter.setDropDownViewResource(R.layout.spinner_item_white);
        spvalue.setAdapter(valueAdapter);

        // 저장 버튼('저장 완료' 메시지만 뜨게 함)
        btnsave = findViewById(R.id.btn_save);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CleaningAdd_UI.this, "저장 완료", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
