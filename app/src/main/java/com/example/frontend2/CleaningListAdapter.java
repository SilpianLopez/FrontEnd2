package com.example.frontend2; // ★ 실제 프로젝트의 패키지 경로로 수정하세요.

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color; // 기본 색상용 (선택 사항)
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
// import android.widget.RadioGroup; // cleaning_list.xml에 RadioGroup이 없다면 제거, 또는 다른 상세 정보 레이아웃 ID
import android.widget.LinearLayout; // 상세 정보 레이아웃으로 LinearLayout 사용 시
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.R; // R 클래스 import
import com.example.frontend2.api.ApiClient; // API 호출용
import com.example.frontend2.api.RoutineApi; // 루틴 삭제 API용
import com.example.frontend2.models.CleaningRoutine; // ❗️ CleaningRoutine 모델 사용

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningListAdapter extends RecyclerView.Adapter<CleaningListAdapter.ViewHolder> {
    private static final String TAG = "CleaningListAdapter";
    private List<CleaningRoutine> routineItems; // ❗️ 데이터 타입을 CleaningRoutine으로 변경
    private Context context; // API 호출 및 Toast 등에 사용
    private final int HIGHLIGHT_COLOR = Color.parseColor("#E0E0E0"); // 예시 하이라이트 색상
    private final int DEFAULT_COLOR = Color.parseColor("#FFFFFF");   // 예시 기본 배경 색상

    // 수정 버튼 클릭 시 Activity로 이벤트를 전달하기 위한 리스너 인터페이스
    public interface OnCleaningEditListener {
        void onEditRequested(CleaningRoutine routineToEdit); // ❗️ 파라미터 타입을 CleaningRoutine으로 변경
    }

    private OnCleaningEditListener editListener;

    // 생성자 수정
    public CleaningListAdapter(Context context, List<CleaningRoutine> items, OnCleaningEditListener listener) {
        this.context = context;
        this.routineItems = items;
        this.editListener = listener;
    }

    // 데이터 목록을 새로 설정하는 메소드
    public void setItems(List<CleaningRoutine> newItems) {
        this.routineItems.clear();
        if (newItems != null) {
            this.routineItems.addAll(newItems);
        }
        notifyDataSetChanged(); // 전체 데이터셋 변경 알림
    }

    // 단일 아이템 추가 메소드 (CleaningAdd_UI에서 사용 가능)
    public void addItem(CleaningRoutine item) {
        this.routineItems.add(item);
        notifyItemInserted(this.routineItems.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.cleaning_list, parent, false); // R.layout.cleaning_list는 각 루틴 항목의 레이아웃 파일
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CleaningRoutine currentRoutine = routineItems.get(position);

        holder.tvRoutineTitle.setText(currentRoutine.getTitle() != null ? currentRoutine.getTitle() : "제목 없음");

        // 청소 주기 표시 로직
        String cycleText = "반복 없음";
        if (currentRoutine.getRepeat_unit() != null && !currentRoutine.getRepeat_unit().equals("NONE") &&
                currentRoutine.getRepeat_interval() != null && currentRoutine.getRepeat_interval() > 0) {
            String unitDisplay = "";
            switch (currentRoutine.getRepeat_unit()) {
                case "DAY": unitDisplay = "일"; break;
                case "WEEK": unitDisplay = "주"; break;
                case "MONTH": unitDisplay = "개월"; break;
                case "YEAR": unitDisplay = "년"; break;
                default: unitDisplay = currentRoutine.getRepeat_unit();
            }
            cycleText = currentRoutine.getRepeat_interval() + unitDisplay + "마다";
        }
        holder.tvRoutineCycle.setText("청소 주기: " + cycleText);

        // 설명 표시 (cleaning_list.xml에 해당 ID의 TextView가 있다면)
        if (holder.tvRoutineDescription != null) {
            holder.tvRoutineDescription.setText(currentRoutine.getDescription() != null ? currentRoutine.getDescription() : "");
            // 기본적으로 숨겨져 있고, 클릭 시 보이도록 할 수 있음 (triangle 로직과 연동)
        }


        holder.itemView.setBackgroundColor(DEFAULT_COLOR); // 기본 배경색

        // 아이템 롱클릭 시 바텀 시트 메뉴 표시
        holder.itemView.setOnLongClickListener(v -> {
            holder.itemView.setBackgroundColor(HIGHLIGHT_COLOR); // 롱클릭 시 배경색 변경

            BottomSheetDialog sheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialog);
            View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_space, null); // bottom_sheet_menu.xml 사용
            // sheetView.setBackgroundResource(R.drawable.bottom_sheet_background); // 필요시 배경 설정

            sheetDialog.setContentView(sheetView);
            sheetDialog.setOnDismissListener(dialog -> holder.itemView.setBackgroundColor(DEFAULT_COLOR)); // 다이얼로그 닫힐 때 배경색 원래대로

            // "수정" 버튼 클릭 리스너
            sheetView.findViewById(R.id.btnEdit).setOnClickListener(view -> {
                if (editListener != null) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        editListener.onEditRequested(routineItems.get(currentPosition)); // 선택된 CleaningRoutine 객체 전달
                    }
                }
                sheetDialog.dismiss();
            });

            // "삭제" 버튼 클릭 리스너
            sheetView.findViewById(R.id.btnDelete).setOnClickListener(view -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    showDeleteConfirmationDialog(routineItems.get(currentPosition), currentPosition);
                }
                sheetDialog.dismiss();
            });

            // "닫기" 버튼 클릭 리스너
            sheetView.findViewById(R.id.btnCancel).setOnClickListener(view -> sheetDialog.dismiss());

            sheetDialog.show();
            return true; // 롱클릭 이벤트 소비
        });

        // (선택 사항) 상세 정보 토글 로직
        if (holder.ivToggleDetail != null && holder.layoutDetailInfo != null) {
            // 초기 상태 설정 (예: 상세 정보 숨김)
            holder.layoutDetailInfo.setVisibility(View.GONE);
            holder.ivToggleDetail.setRotation(0);

            holder.ivToggleDetail.setOnClickListener(v -> {
                if (holder.layoutDetailInfo.getVisibility() == View.GONE) {
                    holder.layoutDetailInfo.setVisibility(View.VISIBLE);
                    holder.ivToggleDetail.setRotation(180); // 아이콘 회전
                } else {
                    holder.layoutDetailInfo.setVisibility(View.GONE);
                    holder.ivToggleDetail.setRotation(0);
                }
            });
        }
    }

    /**
     * 루틴 삭제 확인 다이얼로그 표시
     */
    private void showDeleteConfirmationDialog(final CleaningRoutine routineToDelete, final int position) {
        new AlertDialog.Builder(context)
                .setTitle("루틴 삭제 확인")
                .setMessage("'" + routineToDelete.getTitle() + "' 루틴을 정말 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    deleteRoutineFromBackend(routineToDelete, position);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    /**
     * 백엔드에 루틴 삭제 API를 호출합니다.
     */
    private void deleteRoutineFromBackend(final CleaningRoutine routineToDelete, final int position) {
        if (ApiClient.getClient() == null) {
            Toast.makeText(context, "네트워크 서비스 오류", Toast.LENGTH_SHORT).show();
            return;
        }
        RoutineApi routineApiService = ApiClient.getClient().create(RoutineApi.class);
        Call<Void> call = routineApiService.deleteRoutine(routineToDelete.getRoutine_id());

        Log.d(TAG, "루틴 삭제 요청: ID " + routineToDelete.getRoutine_id());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "루틴 삭제 성공 (서버): ID " + routineToDelete.getRoutine_id());
                    Toast.makeText(context, "'" + routineToDelete.getTitle() + "' 루틴이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    // UI에서 아이템 제거
                    routineItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, routineItems.size());
                } else {
                    // API 호출은 성공했으나 서버에서 에러 응답
                    String errorMsg = "루틴 삭제 실패 (서버 응답: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try { errorMsg += " - " + response.errorBody().string(); }
                        catch (IOException e) { Log.e(TAG, "Error body parsing error", e); }
                    }
                    Log.e(TAG, errorMsg);
                    Toast.makeText(context, "루틴 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "루틴 삭제 API 호출 오류", t);
                Toast.makeText(context, "네트워크 오류로 루틴을 삭제하지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return routineItems != null ? routineItems.size() : 0;
    }

    // ViewHolder 클래스
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoutineTitle, tvRoutineCycle, tvRoutineDescription; // 필드명 변경 또는 cleaning_list.xml ID와 일치
        ImageView ivToggleDetail; // 예시: 상세 정보 토글용 이미지뷰 (cleaning_list.xml에 ID: im_triangle 가정)
        LinearLayout layoutDetailInfo; // 예시: 상세 정보를 담는 레이아웃 (cleaning_list.xml에 ID: cdetail 가정, RadioGroup 대신 LinearLayout 사용 가능)
        View itemView; // 루트 뷰 참조

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView; // itemView 저장
            tvRoutineTitle = itemView.findViewById(R.id.tv_cname);     // ❗️ cleaning_list.xml의 ID와 일치 필요
            tvRoutineCycle = itemView.findViewById(R.id.tv_cycle);      // ❗️ cleaning_list.xml의 ID와 일치 필요
            tvRoutineDescription = itemView.findViewById(R.id.tv_comment); // ❗️ cleaning_list.xml의 ID와 일치 필요 (설명 표시용)
            ivToggleDetail = itemView.findViewById(R.id.im_triangle);      // ❗️ cleaning_list.xml에 이 ID가 있어야 함
            layoutDetailInfo = itemView.findViewById(R.id.cdetail);        // ❗️ cleaning_list.xml에 이 ID가 있어야 함 (LinearLayout 등으로 변경 가능)
        }
    }
}