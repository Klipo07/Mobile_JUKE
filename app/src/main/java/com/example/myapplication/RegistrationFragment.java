package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;

public class RegistrationFragment extends Fragment {

    private EditText editTextName;
    private RadioGroup radioGroupGender;
    private Spinner spinnerCourse;
    private SeekBar seekBarDifficulty;
    private TextView textDifficultyValue;
    private DatePicker datePicker;
    private ImageView imageViewZodiac;
    private Button buttonSubmit;
    private Button buttonBack;
    private TextView textViewResult;

    private String selectedGender = "";
    private String zodiacSign = "";
    private String selectedDate = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        editTextName = view.findViewById(R.id.editTextName);
        radioGroupGender = view.findViewById(R.id.radioGroupGender);
        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        seekBarDifficulty = view.findViewById(R.id.seekBarDifficulty);
        textDifficultyValue = view.findViewById(R.id.textDifficultyValue);
        datePicker = view.findViewById(R.id.datePicker);
        imageViewZodiac = view.findViewById(R.id.imageViewZodiac);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonBack = view.findViewById(R.id.buttonBack);
        textViewResult = view.findViewById(R.id.textViewResult);

        // Настройка спиннера
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"1 курс", "2 курс", "3 курс", "4 курс"}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(getContext().getResources().getColor(android.R.color.white));
                return view;
            }
            
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(getContext().getResources().getColor(android.R.color.black));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adapter);

        // Настройка радиокнопок
        radioGroupGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioMale) selectedGender = "Мужской";
            else if (checkedId == R.id.radioFemale) selectedGender = "Женский";
        });

        // Настройка SeekBar
        textDifficultyValue.setText(String.valueOf(seekBarDifficulty.getProgress()));
        seekBarDifficulty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textDifficultyValue.setText(String.valueOf(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Настройка DatePicker
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1; // month is 0-based
        int year = datePicker.getYear();
        selectedDate = day + "." + month + "." + year;
        zodiacSign = getZodiacSign(day, month);
        setZodiacImage(zodiacSign);

        datePicker.init(year, datePicker.getMonth(), day, (view1, y, m, d) -> {
            int monthSelected = m + 1;
            selectedDate = d + "." + monthSelected + "." + y;
            zodiacSign = getZodiacSign(d, monthSelected);
            setZodiacImage(zodiacSign);
        });
        
        // Установка белого цвета текста для DatePicker
        setDatePickerTextColor(datePicker);

        // Настройка кнопок
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
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


            requireContext().getSharedPreferences("game_prefs", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putInt("difficulty", difficulty)
                    .apply();


            new Thread(() -> {
                com.example.myapplication.db.AppDao dao = com.example.myapplication.db.AppDatabase.get(requireContext()).dao();
                com.example.myapplication.db.UserEntity existing = dao.getUserByName(fio);
                long userId;
                if (existing == null) {
                    com.example.myapplication.db.UserEntity u = new com.example.myapplication.db.UserEntity();
                    u.name = fio.isEmpty() ? "Игрок" : fio;
                    userId = dao.insertUser(u);
                } else {
                    userId = existing.id;
                }
                long finalUserId = userId;
                requireActivity().runOnUiThread(() -> UserManager.setCurrentUser(requireContext(), finalUserId, fio));
            }).start();
        });

        return view;
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
    
    private void setDatePickerTextColor(DatePicker datePicker) {
        // Установка белого цвета текста для всех дочерних элементов DatePicker
        for (int i = 0; i < ((ViewGroup) datePicker).getChildCount(); i++) {
            View child = ((ViewGroup) datePicker).getChildAt(i);
            if (child instanceof ViewGroup) {
                for (int j = 0; j < ((ViewGroup) child).getChildCount(); j++) {
                    View grandChild = ((ViewGroup) child).getChildAt(j);
                    if (grandChild instanceof TextView) {
                        ((TextView) grandChild).setTextColor(getContext().getResources().getColor(android.R.color.white));
                    }
                }
            }
        }
    }
}
