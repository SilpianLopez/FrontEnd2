package com.example.frontend2;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.frontend2.models.Space;  // ✅ 이게 꼭 있어야 함!

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class SpaceAdapter extends RecyclerView.Adapter<SpaceAdapter.ViewHolder> {

    private List<Space> spaceList;
    private Context context;
    private OnSpaceEditListener editListener;

    // 강조 색상 및 기본 색상 정의
    private final int HIGHLIGHT_COLOR = Color.parseColor("#bcbcbc");
    private final int DEFAULT_COLOR = Color.parseColor("#dadada");

    // ✅ 인터페이스: 수정 + 삭제 콜백
    public interface OnSpaceEditListener {
        void onEditRequested(int position, Space space);
        void onDeleteRequested(int position, Space space);
    }

    public void setOnSpaceEditListener(OnSpaceEditListener listener) {
        this.editListener = listener;
    }

    public SpaceAdapter(Context context, List<Space> spaceList) {
        this.context = context;
        this.spaceList = spaceList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpaceName, tvSpaceType, tvFurniture;
        View rootView;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            tvSpaceName = itemView.findViewById(R.id.tvSpaceName);
            tvSpaceType = itemView.findViewById(R.id.tvSpaceType);
            tvFurniture = itemView.findViewById(R.id.tvFurniture);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_space, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Space space = spaceList.get(position);
        holder.tvSpaceName.setText(space.getName());
        holder.tvSpaceType.setText("종류: " + space.getType());
        holder.tvFurniture.setText("가구: " + space.getFurniture());
        holder.rootView.setBackgroundColor(DEFAULT_COLOR);

        holder.itemView.setOnLongClickListener(v -> {
            BottomSheetDialog sheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialog);
            View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_space, null);
            sheetDialog.setContentView(sheetView);

            sheetView.findViewById(R.id.btnEdit).setOnClickListener(view -> {
                if (editListener != null) {
                    editListener.onEditRequested(holder.getAdapterPosition(), space);
                }
                sheetDialog.dismiss();
            });

            sheetView.findViewById(R.id.btnDelete).setOnClickListener(view -> {
                if (editListener != null) {
                    editListener.onDeleteRequested(holder.getAdapterPosition(), space);
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
        return spaceList.size();
    }

    // ✅ 외부에서 삭제 후 리스트 갱신 시 사용할 메서드
    public void removeItem(int position) {
        spaceList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, spaceList.size());
    }
}
