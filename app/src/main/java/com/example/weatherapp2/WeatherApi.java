package com.example.weatherapp2;

import com.example.weatherapp2.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Call<WeatherResponse> getWeather(
            @Query("q") String location,
            @Query("appid") String apiKey,
            @Query("units") String units);
}
