package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private SeekBar seekBarSpeed;
    private TextView textSpeedValue;
    private SeekBar seekBarCockroaches;
    private TextView textCockroachesValue;
    private SeekBar seekBarBonusInterval;
    private TextView textBonusIntervalValue;
    private SeekBar seekBarRoundDuration;
    private TextView textRoundDurationValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Инициализация элементов
        seekBarSpeed = view.findViewById(R.id.seekBarSpeed);
        textSpeedValue = view.findViewById(R.id.textSpeedValue);
        seekBarCockroaches = view.findViewById(R.id.seekBarCockroaches);
        textCockroachesValue = view.findViewById(R.id.textCockroachesValue);
        seekBarBonusInterval = view.findViewById(R.id.seekBarBonusInterval);
        textBonusIntervalValue = view.findViewById(R.id.textBonusIntervalValue);
        seekBarRoundDuration = view.findViewById(R.id.seekBarRoundDuration);
        textRoundDurationValue = view.findViewById(R.id.textRoundDurationValue);

        // Настройка SeekBar для скорости игры
        setupSeekBar(seekBarSpeed, textSpeedValue, 1, 10, "x");
        
        // Настройка SeekBar для количества тараканов
        setupSeekBar(seekBarCockroaches, textCockroachesValue, 5, 50, "");
        
        // Настройка SeekBar для интервала бонусов
        setupSeekBar(seekBarBonusInterval, textBonusIntervalValue, 5, 60, " сек");
        
        // Настройка SeekBar для длительности раунда
        setupSeekBar(seekBarRoundDuration, textRoundDurationValue, 30, 300, " сек");

        return view;
    }

    private void setupSeekBar(SeekBar seekBar, TextView textView, int min, int max, String suffix) {
        seekBar.setMax(max - min);
        textView.setText(min + suffix);
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = progress + min;
                textView.setText(value + suffix);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
