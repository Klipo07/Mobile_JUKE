package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.AppDao;
import com.example.myapplication.db.RecordItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecordsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_records, container, false);
        recyclerView = v.findViewById(R.id.recyclerRecords);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecordsAdapter();
        recyclerView.setAdapter(adapter);
        loadData();
        return v;
    }

    private void loadData() {
        new Thread(() -> {
            AppDao dao = AppDatabase.get(requireContext()).dao();
            List<RecordItem> items = dao.getTopRecords();
            requireActivity().runOnUiThread(() -> adapter.setItems(items));
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    static class RecordsAdapter extends RecyclerView.Adapter<RecordVH> {
        private final List<RecordItem> data = new ArrayList<>();
        void setItems(List<RecordItem> items) { data.clear(); if (items != null) data.addAll(items); notifyDataSetChanged(); }
        @Override public RecordVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
            return new RecordVH(v);
        }
        @Override public void onBindViewHolder(RecordVH h, int position) {
            RecordItem it = data.get(position);
            h.name.setText(it.name);
            h.score.setText("Очки: " + it.score);
            h.diff.setText("Сложность: " + it.difficulty);
            h.date.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date(it.createdAt)));
        }
        @Override public int getItemCount() { return data.size(); }
    }
}


