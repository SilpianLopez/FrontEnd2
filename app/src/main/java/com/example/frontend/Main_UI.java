package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

public class Main_UI extends AppCompatActivity {

    private GridLayout spaceGrid;
    private LinearLayout todoListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_ui); // main_ui.xml 이름과 일치해야 함

        spaceGrid = findViewById(R.id.spaceGrid);
        todoListLayout = findViewById(R.id.todoListLayout);

        addSpaceCard("거실", R.drawable.ic_livingroom);
        addSpaceCard("철수의 방", R.drawable.ic_room);
        addSpaceCard("화장실", R.drawable.ic_toilet);
        addSpaceCard("옷방", R.drawable.ic_wardrobe);

        addTodoItem("청소 항목1");
        addTodoItem("청소 항목2");
        addTodoItem("청소 항목3");
        addTodoItem("청소 항목4");

        findViewById(R.id.btnAddSpace).setOnClickListener(v -> {
            Intent intent = new Intent(Main_UI.this, SpaceListActivity.class);
            startActivity(intent);
        });


    }

    private void addSpaceCard(String name, int imageResId) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setBackgroundColor(0xFFDADADA);
        container.setPadding(16, 24, 16, 24);

        ImageView icon = new ImageView(this);
        icon.setImageResource(imageResId);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(96, 96); // 크기 조정 가능
        icon.setLayoutParams(iconParams);

        TextView tv = new TextView(this);
        tv.setText(name);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 8, 0, 0);

        container.addView(icon);
        container.addView(tv);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);
        container.setLayoutParams(params);

        spaceGrid.addView(container);
    }


    private void addTodoItem(String content) {
        TextView tv = new TextView(this);
        tv.setText("· " + content);
        tv.setTextSize(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        tv.setLayoutParams(params);
        todoListLayout.addView(tv);
    }
}

