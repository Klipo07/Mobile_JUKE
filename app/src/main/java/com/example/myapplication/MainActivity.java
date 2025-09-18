package com.example.myapplication;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextName;
    private RadioGroup radioGroupGender;
    private Spinner spinnerCourse;
    private SeekBar seekBarDifficulty;
    private TextView textDifficultyValue;
    private DatePicker datePicker;
    private ImageView imageViewZodiac;
    private Button buttonSubmit;
    private TextView textViewResult;

    private String selectedGender = "";
    private String zodiacSign = "";
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editTextName = findViewById(R.id.editTextName);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        spinnerCourse = findViewById(R.id.spinnerCourse);
        seekBarDifficulty = findViewById(R.id.seekBarDifficulty);
        textDifficultyValue = findViewById(R.id.textDifficultyValue);
        datePicker = findViewById(R.id.datePicker);
        imageViewZodiac = findViewById(R.id.imageViewZodiac);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewResult = findViewById(R.id.textViewResult);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"1 курс", "2 курс", "3 курс", "4 курс"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adapter);


        radioGroupGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioMale) selectedGender = "Мужской";
            else if (checkedId == R.id.radioFemale) selectedGender = "Женский";
        });


        textDifficultyValue.setText(String.valueOf(seekBarDifficulty.getProgress()));
        seekBarDifficulty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textDifficultyValue.setText(String.valueOf(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1; // month is 0-based
        int year = datePicker.getYear();
        selectedDate = day + "." + month + "." + year;
        zodiacSign = getZodiacSign(day, month);
        setZodiacImage(zodiacSign);


        datePicker.init(year, datePicker.getMonth(), day, (view, y, m, d) -> {

            int monthSelected = m + 1;
            selectedDate = d + "." + monthSelected + "." + y;
            zodiacSign = getZodiacSign(d, monthSelected);
            setZodiacImage(zodiacSign);
        });


        buttonSubmit.setOnClickListener(v -> {
            String fio = editTextName.getText().toString().trim();
            String course = spinnerCourse.getSelectedItem().toString();
            int difficulty = seekBarDifficulty.getProgress();

            if (selectedGender.isEmpty()) selectedGender = "Не выбран";

            Player player = new Player(fio, selectedGender, course, difficulty, selectedDate, zodiacSign);

            String result = "ФИО: " + player.fio + "\n" +
                    "Пол: " + player.gender + "\n" +
                    "Курс: " + player.course + "\n" +
                    "Сложность: " + player.difficulty + "\n" +
                    "Дата рождения: " + player.birthDate + "\n" +
                    "Знак зодиака: " + player.zodiac;

            textViewResult.setText(result);
        });
    }


    private String getZodiacSign(int day, int month) {
        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "Овен";
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "Телец";
        if ((month == 5 && day >= 21) || (month == 6 && day <= 20)) return "Близнецы";
        if ((month == 6 && day >= 21) || (month == 7 && day <= 22)) return "Рак";
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "Лев";
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "Дева";
        if ((month == 9 && day >= 23) || (month == 10 && day <= 22)) return "Весы";
        if ((month == 10 && day >= 23) || (month == 11 && day <= 21)) return "Скорпион";
        if ((month == 11 && day >= 22) || (month == 12 && day <= 21)) return "Стрелец";
        if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) return "Козерог";
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "Водолей";
        return "Рыбы";
    }


    private void setZodiacImage(String zodiac) {
        int resId = R.drawable.aries; // default
        switch (zodiac) {
            case "Овен": resId = R.drawable.aries; break;
            case "Телец": resId = R.drawable.taurus; break;
            case "Близнецы": resId = R.drawable.gemini; break;
            case "Рак": resId = R.drawable.cancer; break;
            case "Лев": resId = R.drawable.leo; break;
            case "Дева": resId = R.drawable.virgo; break;
            case "Весы": resId = R.drawable.libra; break;
            case "Скорпион": resId = R.drawable.scorpio; break;
            case "Стрелец": resId = R.drawable.sagittarius; break;
            case "Козерог": resId = R.drawable.capricorn; break;
            case "Водолей": resId = R.drawable.aquarius; break;
            case "Рыбы": resId = R.drawable.pisces; break;
        }
        imageViewZodiac.setImageResource(resId);
    }
}
