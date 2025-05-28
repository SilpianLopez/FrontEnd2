package com.example.frontend2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class CleaningListAdapter extends RecyclerView.Adapter<CleaningListAdapter.ViewHolder> {
    private List<CleaningList> items;
    // 강조 색상 및 기본 색상 정의
    private final int HIGHLIGHT_COLOR = Color.parseColor("#bcbcbc"); // 더 어두운 회색
    private final int DEFAULT_COLOR = Color.parseColor("#dadada");   // 기존 회색

    public CleaningListAdapter(List<CleaningList> items) {
        this.items = items;
    }
    // ViewHolder 클래스(리스트 항목을 표현)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, cycle, comment;
        ImageView triangle;
        RadioGroup detail;
        View rootView;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            name = itemView.findViewById(R.id.tv_cname);
            cycle = itemView.findViewById(R.id.tv_cycle);
            comment = itemView.findViewById(R.id.tv_comment);
            triangle = itemView.findViewById(R.id.im_triangle);
            detail = itemView.findViewById(R.id.cdetail);

            triangle.setOnClickListener(v -> {
                if (detail.getVisibility() == View.GONE) {
                    detail.setVisibility(View.VISIBLE);
                    triangle.setRotation(180);
                } else {
                    detail.setVisibility(View.GONE);
                    triangle.setRotation(0);
                }
            });
        }
    }
    // 아이템 레이아웃을 생성할 때 호출됨
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cleaning_list, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CleaningList item = items.get(position);
        holder.name.setText(item.name);
        holder.cycle.setText("청소 주기: " + item.cycle);
        holder.comment.setText(item.comment);

        // 롱클릭 시 BottomSheet로 강조 및 메뉴 표시(ui만 구현함, 기능은 아직)
        holder.itemView.setOnLongClickListener(v -> {
            holder.rootView.setBackgroundColor(HIGHLIGHT_COLOR);

            BottomSheetDialog sheetDialog = new BottomSheetDialog(v.getContext(), R.style.CustomBottomSheetDialog);
            View sheetView = LayoutInflater.from(v.getContext()).inflate(R.layout.bottom_sheet_space, null);
            sheetView.setBackgroundResource(R.drawable.bottom_sheet_background);
            sheetDialog.setContentView(sheetView);


            sheetDialog.setOnDismissListener(dialog -> {
                holder.rootView.setBackgroundColor(DEFAULT_COLOR);
            });
            sheetDialog.show();
            return true;
        });
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
}
