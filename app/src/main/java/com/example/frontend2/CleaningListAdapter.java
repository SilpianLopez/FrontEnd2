package com.example.frontend2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.CleaningList;
import com.example.frontend2.CleaningList;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class CleaningListAdapter extends RecyclerView.Adapter<CleaningListAdapter.ViewHolder> {

    private List<CleaningList> items;
    private Context context;
    private OnRoutineActionListener actionListener;

    public CleaningListAdapter(Context context, ArrayList<CleaningList> items) {
        this.context = context;
        this.items = items;
    }

    // ✅ 콜백 인터페이스 정의
    public interface OnRoutineActionListener {
        void onEditRequested(int position, CleaningList item);
        void onDeleteRequested(int position, CleaningList item);
    }

    public void setOnRoutineActionListener(OnRoutineActionListener listener) {
        this.actionListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, cycle, comment;
        ImageView triangle;
        RadioGroup detail;

        public ViewHolder(View itemView) {
            super(itemView);
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cleaning_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CleaningList item = items.get(position);
        holder.name.setText(item.getName());
        holder.cycle.setText("청소 주기: " + item.getCycle());
        holder.comment.setText(item.getComment());

        // ✅ 롱클릭 리스너 추가 (바텀시트 띄우기)
        holder.itemView.setOnLongClickListener(v -> {
            BottomSheetDialog sheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialog);
            View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_space, null); // layout 재사용 가능
            sheetView.setBackgroundResource(R.drawable.bottom_sheet_background);
            sheetDialog.setContentView(sheetView);

            sheetView.findViewById(R.id.btnEdit).setOnClickListener(view -> {
                if (actionListener != null) {
                    actionListener.onEditRequested(holder.getAdapterPosition(), item);
                }
                sheetDialog.dismiss();
            });

            sheetView.findViewById(R.id.btnDelete).setOnClickListener(view -> {
                if (actionListener != null) {
                    actionListener.onDeleteRequested(holder.getAdapterPosition(), item);
                }
                sheetDialog.dismiss();
            });

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

    public List<CleaningList> getItems() {
        return items;
    }


    // 외부에서 호출하는 삭제 메서드
    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }
}
