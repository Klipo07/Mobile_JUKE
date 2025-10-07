package com.example.myapplication;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class RecordVH extends RecyclerView.ViewHolder {
    TextView name;
    TextView score;
    TextView diff;
    TextView date;
    public RecordVH(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.textName);
        score = itemView.findViewById(R.id.textScore);
        diff = itemView.findViewById(R.id.textDiff);
        date = itemView.findViewById(R.id.textDate);
    }
}


