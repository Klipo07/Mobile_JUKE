package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class AuthorsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_authors, container, false);
        
        ListView listView = view.findViewById(R.id.listViewAuthors);
        
        // Создаем список авторов
        List<Author> authors = new ArrayList<>();
        // Огарков Кирилл Алексеевич - единственный автор с реальной фотографией
        authors.add(new Author("Огарков Кирилл Алексеевич", R.drawable.author1));
        
        // Создаем адаптер
        AuthorAdapter adapter = new AuthorAdapter(getContext(), authors);
        listView.setAdapter(adapter);
        
        return view;
    }
}
