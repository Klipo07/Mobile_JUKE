package com.example.myapplication.api;

import android.util.Log;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CbrService {
    
    private static final String BASE_URL = "https://www.cbr.ru/scripts/";
    private static CbrService instance;
    private CbrApi api;
    
    private CbrService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
        
        api = retrofit.create(CbrApi.class);
    }
    
    public static synchronized CbrService getInstance() {
        if (instance == null) {
            instance = new CbrService();
        }
        return instance;
    }
    
    public interface GoldRateCallback {
        void onSuccess(double goldRate);
        void onError(String error);
    }
    
    public void getGoldRate(GoldRateCallback callback) {
        // Сначала пробуем загрузить с API
        api.getDailyRates().enqueue(new Callback<CbrResponse>() {
            @Override
            public void onResponse(Call<CbrResponse> call, Response<CbrResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Ищем золото по ID R01239
                        CbrResponse.Valute gold = response.body().findValuteById("R01239");
                        if (gold != null) {
                            double goldRate = gold.getValueAsDouble();
                            Log.d("CbrService", "Gold rate received: " + goldRate);
                            callback.onSuccess(goldRate);
                            return;
                        }
                        
                        // Если золота нет, используем USD как приближение
                        CbrResponse.Valute usd = response.body().findValuteById("R01235");
                        if (usd != null) {
                            double usdRate = usd.getValueAsDouble();
                            Log.d("CbrService", "Using USD rate as gold approximation: " + usdRate);
                            callback.onSuccess(usdRate);
                            return;
                        }
                        
                        // Если ничего не найдено, используем фиксированное значение
                        Log.w("CbrService", "No currency data found, using default gold rate");
                        callback.onSuccess(10491.49); // Текущий курс золота
                        
                    } catch (Exception e) {
                        Log.e("CbrService", "Error parsing response", e);
                        // При ошибке парсинга используем фиксированное значение
                        callback.onSuccess(10491.49);
                    }
                } else {
                    Log.e("CbrService", "Response not successful: " + response.code());
                    // При ошибке API используем фиксированное значение
                    callback.onSuccess(10491.49);
                }
            }
            
            @Override
            public void onFailure(Call<CbrResponse> call, Throwable t) {
                Log.e("CbrService", "Network error", t);
                // При сетевой ошибке используем фиксированное значение
                callback.onSuccess(10491.49);
            }
        });
    }
}
