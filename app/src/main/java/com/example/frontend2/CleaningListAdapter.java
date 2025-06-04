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
    private final int HIGHLIGHT_COLOR = Color.parseColor("#bcbcbc");
    private final int DEFAULT_COLOR = Color.parseColor("#dadada");

    // 인터페이스 정의
    public interface OnCleaningEditListener {
        void onEditRequested(int position, CleaningList item);
    }

    private OnCleaningEditListener editListener;

    public void setOnCleaningEditListener(OnCleaningEditListener listener) {
        this.editListener = listener;
    }

    public CleaningListAdapter(List<CleaningList> items) {
        this.items = items;
    }

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

    //저장 기능
    public void addItem(CleaningList item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @NonNull
    @Override
    public CleaningListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cleaning_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CleaningListAdapter.ViewHolder holder, int position) {
        CleaningList item = items.get(position);
        holder.name.setText(item.name);
        holder.cycle.setText("청소 주기: " + item.cycle);
        holder.comment.setText(item.comment);

        holder.rootView.setBackgroundColor(DEFAULT_COLOR);

        holder.itemView.setOnLongClickListener(v -> {
            holder.rootView.setBackgroundColor(HIGHLIGHT_COLOR);

            BottomSheetDialog sheetDialog = new BottomSheetDialog(v.getContext(), R.style.CustomBottomSheetDialog);
            View sheetView = LayoutInflater.from(v.getContext()).inflate(R.layout.bottom_sheet_space, null);
            sheetView.setBackgroundResource(R.drawable.bottom_sheet_background);
            sheetDialog.setContentView(sheetView);

            sheetDialog.setOnDismissListener(dialog -> {
                holder.rootView.setBackgroundColor(DEFAULT_COLOR);
            });

            // 수정 버튼 클릭 시
            sheetView.findViewById(R.id.btnEdit).setOnClickListener(view -> {
                if (editListener != null) {
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        editListener.onEditRequested(pos, items.get(pos));
                    }
                }
                sheetDialog.dismiss();
            });

            // 삭제 버튼 클릭 시
            sheetView.findViewById(R.id.btnDelete).setOnClickListener(view -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    items.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, items.size());
                }
                sheetDialog.dismiss();
            });

            // 취소 버튼 클릭 시
            sheetView.findViewById(R.id.btnCancel).setOnClickListener(view -> {
                sheetDialog.dismiss();
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
