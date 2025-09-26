package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GameFragment extends Fragment {

    private GameView gameView;
    private TextView textScore;
    private TextView textTimer;
    private Button buttonStart;
    private Button buttonPause;

    private int roundDurationSec = 60;
    private int remainingSec = 60;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        textScore = view.findViewById(R.id.textScore);
        textTimer = view.findViewById(R.id.textTimer);
        buttonStart = view.findViewById(R.id.buttonStart);
        buttonPause = view.findViewById(R.id.buttonPause);
        gameView = view.findViewById(R.id.gameView);

        applySettingsToGameView();

        gameView.setOnScoreChangedListener(score -> textScore.setText("Очки: " + score));

        buttonStart.setOnClickListener(v -> startGame());
        buttonPause.setOnClickListener(v -> pauseGame());

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

    @Override
    public void onResume() {
        super.onResume();
        applySettingsToGameView();
    }

    private void startGame() {
        if (!isRunning) {
            // если игра не шла — запускаем
            gameView.start();
            isRunning = true;
            toggleButtonsForRunning(true);
            startTimerInternal();
        }
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

    @Override
    public void onPause() {
        super.onPause();
        // При уходе с вкладки — ставим игру на паузу, но не сбрасываем
        pauseGame();
    }

    private String formatTime(int totalSec) {
        int m = totalSec / 60;
        int s = totalSec % 60;
        return String.format("%02d:%02d", m, s);
    }
}


