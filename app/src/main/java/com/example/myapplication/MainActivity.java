package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.app.AlertDialog;
import android.widget.ArrayAdapter;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.AppDao;
import com.example.myapplication.db.UserEntity;
import java.util.List;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private TextView textCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация элементов
        textCurrentUser = findViewById(R.id.textCurrentUser);
        
        // Настройка кнопок навигации
        setupNavigationButtons();
        
        // Обновляем информацию о пользователе
        updateCurrentUserInfo();
    }

    private void setupNavigationButtons() {
        findViewById(R.id.buttonGame).setOnClickListener(v -> showFragment(new GameFragment()));
        findViewById(R.id.buttonRegistration).setOnClickListener(v -> showFragment(new RegistrationFragment()));
        findViewById(R.id.buttonRules).setOnClickListener(v -> showFragment(new RulesFragment()));
        findViewById(R.id.buttonAuthors).setOnClickListener(v -> showFragment(new AuthorsFragment()));
        findViewById(R.id.buttonRecords).setOnClickListener(v -> showFragment(new RecordsFragment()));
        findViewById(R.id.buttonSettings).setOnClickListener(v -> showFragment(new SettingsFragment()));
        
        // Клик по информации о пользователе для смены пользователя
        textCurrentUser.setOnClickListener(v -> showUserSelectionDialog());
    }

    private void showFragment(Fragment fragment) {
        // Скрываем главное меню и показываем контейнер фрагментов
        findViewById(R.id.mainMenuContainer).setVisibility(View.GONE);
        findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void updateCurrentUserInfo() {
        String userName = UserManager.getCurrentUserName(this);
        if (userName.isEmpty()) {
            textCurrentUser.setText("Пользователь не выбран");
        } else {
            textCurrentUser.setText("Текущий пользователь: " + userName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCurrentUserInfo();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // Если есть фрагменты в стеке, возвращаемся к главному меню
            getSupportFragmentManager().popBackStack();
            findViewById(R.id.mainMenuContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void showUserSelectionDialog() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.get(this);
            AppDao dao = db.dao();
            List<UserEntity> users = dao.getUsers();
            
            runOnUiThread(() -> {
                if (users.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Выбор пользователя")
                            .setMessage("Нет зарегистрированных пользователей. Создайте нового пользователя в разделе 'Регистрация'.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
                
                String[] userNames = new String[users.size()];
                for (int i = 0; i < users.size(); i++) {
                    userNames[i] = users.get(i).name;
                }
                
                new AlertDialog.Builder(this)
                        .setTitle("Выберите пользователя")
                        .setItems(userNames, (dialog, which) -> {
                            UserEntity selectedUser = users.get(which);
                            UserManager.setCurrentUser(this, selectedUser.id, selectedUser.name);
                            updateCurrentUserInfo();
                        })
                        .setPositiveButton("Удалить пользователя", (dialog, which) -> {
                            showUserDeletionDialog(users);
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });
        }).start();
    }

    private void showUserDeletionDialog(List<UserEntity> users) {
        if (users.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Удаление пользователей")
                    .setMessage("Нет пользователей для удаления.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        
        String[] userNames = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            userNames[i] = users.get(i).name;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Удалить пользователя")
                .setItems(userNames, (dialog, which) -> {
                    UserEntity userToDelete = users.get(which);
                    confirmUserDeletion(userToDelete);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void confirmUserDeletion(UserEntity userToDelete) {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы уверены, что хотите удалить пользователя \"" + userToDelete.name + "\"?\n\nЭто действие также удалит все его рекорды!")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    deleteUser(userToDelete);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteUser(UserEntity userToDelete) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.get(this);
            AppDao dao = db.dao();
            
            // Проверяем, является ли удаляемый пользователь текущим
            long currentUserId = UserManager.getCurrentUserId(this);
            boolean isCurrentUser = (currentUserId == userToDelete.id);
            
            // Удаляем пользователя и все его рекорды
            dao.deleteScoresByUser(userToDelete.id);
            dao.deleteUserById(userToDelete.id);
            
            runOnUiThread(() -> {
                if (isCurrentUser) {
                    // Если удаляем текущего пользователя, сбрасываем выбор
                    UserManager.setCurrentUser(this, -1, "");
                    updateCurrentUserInfo();
                }
                
                new AlertDialog.Builder(this)
                        .setTitle("Пользователь удален")
                        .setMessage("Пользователь \"" + userToDelete.name + "\" и все его рекорды были удалены.")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }).start();
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
                                            
                                            runOnUiThread(() -> {
                                                if (UserManager.getCurrentUserId(this) == chosen.id) {
                                                    UserManager.setCurrentUser(this, -1, "");
                                                    updateCurrentUserInfo();
                                                }
                                                
                                                new AlertDialog.Builder(this)
                                                        .setTitle("Пользователь удален")
                                                        .setMessage("Пользователь \"" + chosen.name + "\" и все его рекорды были удалены.")
                                                        .setPositiveButton("OK", null)
                                                        .show();
                                            });
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
