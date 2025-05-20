package com.example.frontend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CleaningListAdapter extends RecyclerView.Adapter<CleaningListAdapter.ViewHolder> {
    private List<CleaningList> items;

    public CleaningListAdapter(List<CleaningList> items) {
        this.items = items;
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
        holder.name.setText(item.name);
        holder.cycle.setText("청소 주기: " + item.cycle);
        holder.comment.setText(item.comment);
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
}
