package com.example.frontend2;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.RoutineApi;
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningListAdapter extends RecyclerView.Adapter<CleaningListAdapter.ViewHolder> {

    private static final String TAG = "CleaningListAdapter";
    private List<CleaningRoutine> routineItems;
    private Context context;
    private final int DEFAULT_COLOR = Color.WHITE;
    private final int HIGHLIGHT_COLOR = Color.parseColor("#E0E0E0");

    public interface OnCleaningEditListener {
        void onEditRequested(CleaningRoutine routineToEdit);
    }
    private OnCleaningEditListener editListener;

    public CleaningListAdapter(Context context, List<CleaningRoutine> items, OnCleaningEditListener listener) {
        this.context = context;
        this.routineItems = items;
        this.editListener = listener;
    }

    public void setItems(List<CleaningRoutine> newItems) {
        routineItems.clear();
        if (newItems != null) routineItems.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItem(CleaningRoutine item) {
        routineItems.add(item);
        notifyItemInserted(routineItems.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.cleaning_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CleaningRoutine current = routineItems.get(position);
        holder.tvRoutineTitle.setText(current.getTitle() != null ? current.getTitle() : "제목 없음");

        // 주기 설정
        String cycleText = "반복 없음";
        if (current.getRepeat_unit() != null) {
            String unit;
            switch (current.getRepeat_unit()) {
                case "DAY":
                    unit = (current.getRepeat_interval() != null ? current.getRepeat_interval() + "일마다" : "매일");
                    break;
                case "WEEK":
                    unit = (current.getRepeat_interval() != null ? current.getRepeat_interval() + "주마다" : "매주");
                    break;
                case "MONTH":
                    unit = (current.getRepeat_interval() != null ? current.getRepeat_interval() + "개월마다" : "매월");
                    break;
                case "YEAR":
                    unit = (current.getRepeat_interval() != null ? current.getRepeat_interval() + "년마다" : "매년");
                    break;
                case "OTHER":
                    unit = (current.getRepeat_interval() != null ? current.getRepeat_interval() + "일마다" : "사용자 지정");
                    break;
                case "NONE":
                    unit = "반복 없음";
                    break;
                default:
                    unit = "반복 정보 없음";
                    break;
            }
            cycleText = unit;
        }
        holder.tvRoutineCycle.setText(cycleText);

        // 설명
        holder.tvRoutineDescription.setText(current.getDescription() != null ? current.getDescription() : "");

        // 다음 예정 날짜 포맷 적용
        String nextDate = current.getNext_due_date();
        if (nextDate != null && !nextDate.isEmpty()) {
            holder.tvNextDate.setText(formatDate(nextDate));
        } else {
            holder.tvNextDate.setText("예정일 없음");
        }

        holder.itemView.setBackgroundColor(DEFAULT_COLOR);

        holder.itemView.setOnLongClickListener(v -> {
            holder.itemView.setBackgroundColor(HIGHLIGHT_COLOR);
            BottomSheetDialog sheet = new BottomSheetDialog(context, R.style.CustomBottomSheetDialog);
            View sheetView = LayoutInflater.from(context)
                    .inflate(R.layout.bottom_sheet_space, null);
            sheet.setContentView(sheetView);

            sheetView.findViewById(R.id.btnEdit).setOnClickListener(view -> {
                if (editListener != null) editListener.onEditRequested(current);
                sheet.dismiss();
            });
            sheetView.findViewById(R.id.btnDelete).setOnClickListener(view -> {
                sheet.dismiss();
                showDeleteConfirmation(current, position);
            });
            sheetView.findViewById(R.id.btnCancel).setOnClickListener(view -> sheet.dismiss());

            sheet.setOnDismissListener(dialog -> holder.itemView.setBackgroundColor(DEFAULT_COLOR));
            sheet.show();
            return true;
        });

        holder.ivToggleDetail.setOnClickListener(v -> {
            if (holder.layoutDetailInfo.getVisibility() == View.GONE) {
                holder.layoutDetailInfo.setVisibility(View.VISIBLE);
                holder.ivToggleDetail.setRotation(180);
            } else {
                holder.layoutDetailInfo.setVisibility(View.GONE);
                holder.ivToggleDetail.setRotation(0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return routineItems != null ? routineItems.size() : 0;
    }

    private void showDeleteConfirmation(CleaningRoutine routine, int pos) {
        new AlertDialog.Builder(context)
                .setTitle("루틴 삭제 확인")
                .setMessage("'" + routine.getTitle() + "' 을(를) 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteRoutine(routine, pos))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteRoutine(CleaningRoutine routine, int pos) {
        RoutineApi service = ApiClient.getClient().create(RoutineApi.class);
        Call<Void> call = service.deleteRoutine(routine.getRoutine_id());
        Log.d(TAG, "Deleting routine id=" + routine.getRoutine_id());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    routineItems.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, routineItems.size());
                } else {
                    Toast.makeText(context, "삭제 실패 (코드: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Delete API error", t);
                Toast.makeText(context, "네트워크 오류: 삭제 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 날짜 포맷터: yyyy-MM-dd → yyyy년 M월 d일 */
    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = originalFormat.parse(rawDate);
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault());
            return newFormat.format(date);
        } catch (ParseException e) {
            return rawDate; // 포맷 실패 시 원본 출력
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoutineTitle, tvRoutineCycle, tvRoutineDescription, tvNextDate;
        ImageView ivToggleDetail;
        LinearLayout layoutDetailInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            tvRoutineTitle = itemView.findViewById(R.id.tv_cname);
            tvRoutineCycle = itemView.findViewById(R.id.tv_cycle);
            tvRoutineDescription = itemView.findViewById(R.id.tv_comment);
            tvNextDate = itemView.findViewById(R.id.tv_next_date); // 🔸 추가됨
            ivToggleDetail = itemView.findViewById(R.id.im_triangle);
            layoutDetailInfo = itemView.findViewById(R.id.cdetail);
        }
    }
}
