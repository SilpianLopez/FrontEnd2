// SpaceAdapter.java (정리된 코드 - 강조색, 기본색 적용 포함)

package com.example.frontend;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class SpaceAdapter extends RecyclerView.Adapter<SpaceAdapter.ViewHolder> {

    private List<Space> spaceList;
    private Context context;
    private OnSpaceEditListener editListener;

    // 강조 색상 및 기본 색상 정의
    private final int HIGHLIGHT_COLOR = Color.parseColor("#bcbcbc"); // 더 어두운 회색
    private final int DEFAULT_COLOR = Color.parseColor("#dadada");   // 기존 회색

    public interface OnSpaceEditListener {
        void onEditRequested(int position, Space space);
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

        // 기본 색상 적용
        holder.rootView.setBackgroundColor(DEFAULT_COLOR);

        // 롱클릭 시 BottomSheet로 강조 및 메뉴 표시
        holder.itemView.setOnLongClickListener(v -> {
            holder.rootView.setBackgroundColor(HIGHLIGHT_COLOR);

            BottomSheetDialog sheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialog);
            View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_space, null);
            sheetView.setBackgroundResource(R.drawable.bottom_sheet_background); // 배경 적용
            sheetDialog.setContentView(sheetView);


            sheetDialog.setOnDismissListener(dialog -> {
                holder.rootView.setBackgroundColor(DEFAULT_COLOR);
            });

            sheetView.findViewById(R.id.btnEdit).setOnClickListener(view -> {
                if (editListener != null) {
                    editListener.onEditRequested(holder.getAdapterPosition(), space);
                }
                sheetDialog.dismiss();
            });

            sheetView.findViewById(R.id.btnDelete).setOnClickListener(view -> {
                int pos = holder.getAdapterPosition();
                spaceList.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, spaceList.size());
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
}
