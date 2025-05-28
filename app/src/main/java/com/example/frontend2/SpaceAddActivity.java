package com.example.frontend2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SpaceAddActivity extends AppCompatActivity {

    private EditText etSpaceName, etFurniture, etCustomType;
    private Spinner spinnerSpaceType;
    private Button btnSave;
    private ImageView ivIcon;
    private int selectedIconResId = R.drawable.ic_default; // 기본 아이콘

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_add);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("공간 추가");
        }

        etSpaceName = findViewById(R.id.etSpaceName);
        etFurniture = findViewById(R.id.etFurniture);
        etCustomType = findViewById(R.id.etCustomType);
        spinnerSpaceType = findViewById(R.id.spinnerSpaceType);
        btnSave = findViewById(R.id.btnSave);
        ivIcon = findViewById(R.id.ivIcon);

        String[] types = {"거실", "침실", "화장실", "옷방", "기타"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpaceType.setAdapter(adapter);

        spinnerSpaceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if ("기타".equals(selected)) {
                    etCustomType.setVisibility(View.VISIBLE);
                } else {
                    etCustomType.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Intent intent = getIntent();
        if ("edit".equals(intent.getStringExtra("mode"))) {
            etSpaceName.setText(intent.getStringExtra("spaceName"));
            etFurniture.setText(intent.getStringExtra("furniture"));

            String type = intent.getStringExtra("spaceType");
            ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) spinnerSpaceType.getAdapter();
            int index = spinnerAdapter.getPosition(type);
            if (index >= 0) {
                spinnerSpaceType.setSelection(index);
            } else {
                spinnerSpaceType.setSelection(spinnerAdapter.getPosition("기타"));
                etCustomType.setVisibility(View.VISIBLE);
                etCustomType.setText(type);
            }
        }

        ivIcon.setOnClickListener(v -> showIconPicker());

        btnSave.setOnClickListener(v -> {
            String name = etSpaceName.getText().toString();
            String type = spinnerSpaceType.getSelectedItem().toString();
            String furniture = etFurniture.getText().toString();

            if ("기타".equals(type)) {
                type = etCustomType.getText().toString();
            }

            if (name.isEmpty() || furniture.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("spaceName", name);
            resultIntent.putExtra("spaceType", type);
            resultIntent.putExtra("furniture", furniture);
            resultIntent.putExtra("iconResId", selectedIconResId);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    private void showIconPicker() {
        int[] iconResIds = {
                R.drawable.ic_livingroom,
                R.drawable.ic_room,
                R.drawable.ic_toilet,
                R.drawable.ic_wardrobe
        };

        String[] iconLabels = {"거실", "침실", "화장실", "옷방"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("아이콘 선택");

        builder.setItems(iconLabels, (dialog, which) -> {
            selectedIconResId = iconResIds[which];
            ivIcon.setImageResource(selectedIconResId);
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
