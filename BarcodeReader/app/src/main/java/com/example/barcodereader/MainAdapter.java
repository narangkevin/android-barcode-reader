package com.example.barcodereader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    ArrayList<String> mCodeList;

    public MainAdapter(ArrayList<String> mCodeList) {
        this.mCodeList = mCodeList;
    }

    @NonNull
    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.ViewHolder holder, int position) {
        holder.mCode.setText(mCodeList.get(position));
    }

    @Override
    public int getItemCount() {
        return mCodeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mCode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mCode = itemView.findViewById(R.id.mCode);
        }
    }
}
