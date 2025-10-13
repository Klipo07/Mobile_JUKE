package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.api.CbrService;

public class GameFragment extends Fragment implements SensorEventListener {

    private GameView gameView;
    private TextView textScore;
    private TextView textTimer;
    private Button buttonStart;
    private Button buttonPause;
    private Button buttonBack;

    private int roundDurationSec = 60;
    private int remainingSec = 60;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SoundPool soundPool;
    private int bugScreamSoundId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        textScore = view.findViewById(R.id.textScore);
        textTimer = view.findViewById(R.id.textTimer);
        buttonStart = view.findViewById(R.id.buttonStart);
        buttonPause = view.findViewById(R.id.buttonPause);
        buttonBack = view.findViewById(R.id.buttonBack);
        gameView = view.findViewById(R.id.gameView);

        applySettingsToGameView();

        gameView.setOnScoreChangedListener(score -> textScore.setText("Очки: " + score));
        gameView.setOnBonusActivatedListener(() -> playBugScream());

        buttonStart.setOnClickListener(v -> startGame());
        buttonPause.setOnClickListener(v -> pauseGame());
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        initSensors();
        initSoundPool();
        loadGoldRate();

        return view;
    }

    private void applySettingsToGameView() {
        SharedPreferences prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE);
        int speedMultiplier = prefs.getInt("speedMultiplier", 1);
        int maxBugs = prefs.getInt("maxBugs", 10);
        int bonusIntervalSec = prefs.getInt("bonusIntervalSec", 15);
        roundDurationSec = prefs.getInt("roundDurationSec", 60); // берём из настроек, дефолт 60
        if (!isRunning && (remainingSec == 0 || remainingSec > roundDurationSec)) {
            remainingSec = roundDurationSec;
        }

        gameView.configure(speedMultiplier, maxBugs, bonusIntervalSec);
        if (!isRunning) {
            textTimer.setText(formatTime(remainingSec));
        }
    }


    private void startGame() {
        if (!isRunning) {
            // проверяем выбранного пользователя
            long uid = ensureUserSelected();
            if (uid <= 0) return;
            gameView.start();
            isRunning = true;
            toggleButtonsForRunning(true);
            startTimerInternal();
        }
    }

    private long ensureUserSelected() {
        long uid = UserManager.getCurrentUserId(requireContext());
        if (uid > 0) return uid;
        // если не выбран, пробуем получить список пользователей и предложить выбор
        new Thread(() -> {
            com.example.myapplication.db.AppDao dao = com.example.myapplication.db.AppDatabase.get(requireContext()).dao();
            java.util.List<com.example.myapplication.db.UserEntity> users = dao.getUsers();
            requireActivity().runOnUiThread(() -> {
                if (users == null || users.isEmpty()) {
                    new android.app.AlertDialog.Builder(requireContext())
                            .setMessage("Пользователей нет. Создайте во вкладке Регистрация или через меню.")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
                    for (com.example.myapplication.db.UserEntity u : users) adapter.add(u.name + " (#" + u.id + ")");
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Выбрать пользователя для записи рекорда")
                            .setAdapter(adapter, (d, which) -> {
                                com.example.myapplication.db.UserEntity chosen = users.get(which);
                                UserManager.setCurrentUser(requireContext(), chosen.id, chosen.name);
                                // после выбора сразу запускаем игру
                                startGame();
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                }
            });
        }).start();
        return -1;
    }

    private void startTimerInternal() {
        cancelTimerIfAny();
        countDownTimer = new CountDownTimer(remainingSec * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingSec = (int) (millisUntilFinished / 1000);
                textTimer.setText(formatTime(remainingSec));
            }

            @Override
            public void onFinish() {
                remainingSec = 0;
                textTimer.setText("00:00");
                gameView.stop();
                isRunning = false;
                toggleButtonsForRunning(false);
                promptUserAndSaveScore();
            }
        };
        countDownTimer.start();
    }

    private void pauseGame() {
        if (isRunning) {
            isRunning = false;
            cancelTimerIfAny();
            gameView.stop();
            toggleButtonsForRunning(false);
            // кнопка Старт теперь будет как "Продолжить"
            buttonStart.setText("Продолжить");
        }
    }

    private void cancelTimerIfAny() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void toggleButtonsForRunning(boolean running) {
        buttonStart.setVisibility(running ? View.GONE : View.VISIBLE);
        buttonPause.setVisibility(running ? View.VISIBLE : View.GONE);
    }


    private String formatTime(int totalSec) {
        int m = totalSec / 60;
        int s = totalSec % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void saveScoreToDb(long userId) {
        final int finalScore = gameView != null ? gameView.getScore() : 0;
        if (userId <= 0) return;
        new Thread(() -> {
            com.example.myapplication.db.AppDao dao = com.example.myapplication.db.AppDatabase.get(requireContext()).dao();
            com.example.myapplication.db.ScoreEntity se = new com.example.myapplication.db.ScoreEntity();
            se.userId = userId;
            se.score = finalScore;
            se.difficulty = getCurrentDifficulty();
            se.createdAt = System.currentTimeMillis();
            dao.insertScore(se);
        }).start();
    }

    private void promptUserAndSaveScore() {
        long current = UserManager.getCurrentUserId(requireContext());
        new Thread(() -> {
            com.example.myapplication.db.AppDao dao = com.example.myapplication.db.AppDatabase.get(requireContext()).dao();
            java.util.List<com.example.myapplication.db.UserEntity> users = dao.getUsers();
            requireActivity().runOnUiThread(() -> {
                if (users == null || users.isEmpty()) {
                    new android.app.AlertDialog.Builder(requireContext())
                            .setMessage("Нет пользователей. Сохранение пропущено.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
                android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
                int preselect = -1;
                for (int i = 0; i < users.size(); i++) {
                    com.example.myapplication.db.UserEntity u = users.get(i);
                    adapter.add(u.name + " (#" + u.id + ")");
                    if (u.id == current) preselect = i;
                }
                final int preselectFinal = preselect;
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Сохранить рекорд для пользователя")
                        .setSingleChoiceItems(adapter, preselect, null)
                        .setPositiveButton("Сохранить", (d, w) -> {
                            android.app.AlertDialog dialog = (android.app.AlertDialog) d;
                            int which = dialog.getListView().getCheckedItemPosition();
                            if (which >= 0) {
                                saveScoreToDb(users.get(which).id);
                            } else if (preselectFinal >= 0) {
                                saveScoreToDb(users.get(preselectFinal).id);
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });
        }).start();
    }

    private int getCurrentDifficulty() {
        // привяжем к уровню сложности из регистрации: используем seekBarDifficulty значение, сохраняя его в prefs
        // если не найдено — 0
        return requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE).getInt("difficulty", 0);
    }

    private void initSensors() {
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        
        // Загружаем звук крика жуков из файла scream.wav
        bugScreamSoundId = soundPool.load(requireContext(), R.raw.scream, 1);
    }

    private void playBugScream() {
        if (soundPool != null && bugScreamSoundId != 0) {
            // Воспроизводим звук крика жуков из файла scream.wav
            soundPool.play(bugScreamSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && gameView != null) {
            // Применяем гравитацию к насекомым при активном бонусе
            float gravityX = event.values[0]; // наклон по X
            float gravityY = event.values[1]; // наклон по Y
            gameView.applyGravity(gravityX * 0.1f, gravityY * 0.1f);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Не используется
    }

    @Override
    public void onResume() {
        super.onResume();
        applySettingsToGameView();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // При уходе с вкладки — ставим игру на паузу, но не сбрасываем
        pauseGame();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    private void loadGoldRate() {
        CbrService.getInstance().getGoldRate(new CbrService.GoldRateCallback() {
            @Override
            public void onSuccess(double goldRate) {
                if (gameView != null) {
                    gameView.setGoldRate(goldRate);
                }
            }
            
            @Override
            public void onError(String error) {
                android.util.Log.e("GameFragment", "Failed to load gold rate: " + error);
                // Используем значение по умолчанию
                if (gameView != null) {
                    gameView.setGoldRate(100.0);
                }
            }
        });
    }
}


