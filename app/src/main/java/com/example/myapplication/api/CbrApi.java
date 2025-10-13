package com.example.myapplication.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CbrApi {
    
    @GET("XML_daily.asp")
    Call<CbrResponse> getDailyRates();
}
