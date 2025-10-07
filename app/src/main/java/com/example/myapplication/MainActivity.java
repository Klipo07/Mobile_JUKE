package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.app.AlertDialog;
import android.widget.ArrayAdapter;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.AppDao;
import com.example.myapplication.db.UserEntity;
import java.util.List;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Настройка TabLayout и ViewPager2
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        
        // Создаем адаптер для ViewPager2
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Связываем TabLayout с ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Игра");
                    break;
                case 1:
                    tab.setText("Регистрация");
                    break;
                case 2:
                    tab.setText("Правила");
                    break;
                case 3:
                    tab.setText("Рекорды");
                    break;
                case 4:
                    tab.setText("Авторы");
                    break;
                case 5:
                    tab.setText("Настройки");
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create_user) { showCreateUserDialog(); return true; }
        if (item.getItemId() == R.id.action_select_user) { showSelectUserDialog(); return true; }
        if (item.getItemId() == R.id.action_manage_users) { showManageUsersDialog(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void showCreateUserDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Создать пользователя")
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        new Thread(() -> {
                            AppDao dao = AppDatabase.get(this).dao();
                            UserEntity u = new UserEntity();
                            u.name = name;
                            long id = dao.insertUser(u);
                            runOnUiThread(() -> UserManager.setCurrentUser(this, id, name));
                        }).start();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showSelectUserDialog() {
        new Thread(() -> {
            AppDao dao = AppDatabase.get(this).dao();
            List<UserEntity> users = dao.getUsers();
            runOnUiThread(() -> {
                if (users.isEmpty()) {
                    new AlertDialog.Builder(this).setMessage("Пользователей нет. Создайте нового.").setPositiveButton("OK", null).show();
                    return;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
                for (UserEntity u : users) adapter.add(u.name + " (#" + u.id + ")");
                new AlertDialog.Builder(this)
                        .setTitle("Выбрать пользователя")
                        .setAdapter(adapter, (d, which) -> {
                            UserEntity chosen = users.get(which);
                            UserManager.setCurrentUser(this, chosen.id, chosen.name);
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });
        }).start();
    }

    private void showManageUsersDialog() {
        new Thread(() -> {
            AppDao dao = AppDatabase.get(this).dao();
            List<UserEntity> users = dao.getUsers();
            runOnUiThread(() -> {
                if (users.isEmpty()) {
                    new AlertDialog.Builder(this).setMessage("Список пуст").setPositiveButton("OK", null).show();
                    return;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
                for (UserEntity u : users) adapter.add(u.name + " (#" + u.id + ")");
                new AlertDialog.Builder(this)
                        .setTitle("Удалить пользователя")
                        .setAdapter(adapter, (d, which) -> {
                            UserEntity chosen = users.get(which);
                            new AlertDialog.Builder(this)
                                    .setMessage("Удалить " + chosen.name + " и его рекорды?")
                                    .setPositiveButton("Удалить", (dd, w) -> {
                                        new Thread(() -> {
                                            AppDao ddao = AppDatabase.get(this).dao();
                                            ddao.deleteScoresByUser(chosen.id);
                                            ddao.deleteUserById(chosen.id);
                                            if (UserManager.getCurrentUserId(this) == chosen.id) {
                                                UserManager.setCurrentUser(this, -1, "");
                                            }
                                        }).start();
                                    })
                                    .setNegativeButton("Отмена", null)
                                    .show();
                        })
                        .setNegativeButton("Закрыть", null)
                        .show();
            });
        }).start();
    }
}
