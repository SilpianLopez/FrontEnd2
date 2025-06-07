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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningListAdapter extends RecyclerView.Adapter<CleaningListAdapter.ViewHolder> {

    private static final String TAG = "CleaningListAdapter";
    private List<CleaningRoutine> routineItems;
    private Context context;
    private final int DEFAULT_COLOR = Color.WHITE;
    private final int HIGHLIGHT_COLOR = Color.parseColor("#E0E0E0");

    /** 수정 요청을 화면(Activity)에 전달하기 위한 리스너 */
    public interface OnCleaningEditListener {
        void onEditRequested(CleaningRoutine routineToEdit);
    }
    private OnCleaningEditListener editListener;

    /** 생성자 */
    public CleaningListAdapter(Context context, List<CleaningRoutine> items, OnCleaningEditListener listener) {
        this.context = context;
        this.routineItems = items;
        this.editListener = listener;
    }

    /** 전체 데이터 갱신 */
    public void setItems(List<CleaningRoutine> newItems) {
        routineItems.clear();
        if (newItems != null) routineItems.addAll(newItems);
        notifyDataSetChanged();
    }

    /** 단일 아이템 추가 */
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

        String cycleText = "반복 없음";
        if (current.getRepeat_unit() != null && current.getRepeat_interval() != null) {
            String unit;
            switch (current.getRepeat_unit()) {
                case "DAY": unit = "일"; break;
                case "WEEK": unit = "주"; break;
                case "MONTH": unit = "개월"; break;
                default: unit = current.getRepeat_unit();
            }
            cycleText = current.getRepeat_interval() + unit + "마다";
        }
        holder.tvRoutineCycle.setText(cycleText);

        holder.tvRoutineDescription.setText(
                current.getDescription() != null ? current.getDescription() : "");

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

    /** 삭제 확인 다이얼로그 표시 */
    private void showDeleteConfirmation(CleaningRoutine routine, int pos) {
        new AlertDialog.Builder(context)
                .setTitle("루틴 삭제 확인")
                .setMessage("'" + routine.getTitle() + "' 을(를) 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteRoutine(routine, pos))
                .setNegativeButton("취소", null)
                .show();
    }

    /** 백엔드 삭제 API 호출 */
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

    /** ViewHolder */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoutineTitle, tvRoutineCycle, tvRoutineDescription;
        ImageView ivToggleDetail;
        LinearLayout layoutDetailInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            tvRoutineTitle = itemView.findViewById(R.id.tv_cname);
            tvRoutineCycle = itemView.findViewById(R.id.tv_cycle);
            tvRoutineDescription = itemView.findViewById(R.id.tv_comment);
            ivToggleDetail = itemView.findViewById(R.id.im_triangle);
            layoutDetailInfo = itemView.findViewById(R.id.cdetail);
        }
    }
}
