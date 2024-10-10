package com.example.weatherapp2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager; // Import ini untuk menonaktifkan keyboard
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.InputType;

import com.example.weatherapp2.models.WeatherResponse;
import com.squareup.picasso.Picasso;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private EditText locationInput;
    private Button getWeatherButton;
    private TextView weatherResult;
    private TextView temperature;
    private TextView weatherDescription;
    private ImageView weatherIcon;

    // API key Anda
    private static final String API_KEY = "a668ac10933a12eaec20ca129d889377";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Pastikan ini mengacu ke layout yang benar

        // Menghubungkan variabel dengan elemen UI di layout
        locationInput = findViewById(R.id.locationInput);
        getWeatherButton = findViewById(R.id.getWeatherButton);
        weatherResult = findViewById(R.id.weatherResult);
        temperature = findViewById(R.id.temperature);
        weatherDescription = findViewById(R.id.weatherDescription);
        weatherIcon = findViewById(R.id.weatherIcon);

        // Menonaktifkan saran otomatis pada EditText
        locationInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        // Atur visibilitas awal
        weatherIcon.setVisibility(View.GONE); // Sembunyikan ikon cuaca awalnya

        // Menangani klik tombol untuk mendapatkan cuaca
        getWeatherButton.setOnClickListener(v -> getWeather());
    }

    private void getWeather() {
        String location = locationInput.getText().toString().trim();
        if (location.isEmpty()) {
            weatherResult.setText("Silakan masukkan lokasi");
            return; // Kembali jika lokasi kosong
        }

        // Menyembunyikan keyboard setelah input lokasi
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(locationInput.getWindowToken(), 0);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);
        Call<WeatherResponse> call = weatherApi.getWeather(location, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();

                    weatherResult.setText(weatherResponse.getName() != null ? weatherResponse.getName() : "Nama lokasi tidak ditemukan.");

                    if (weatherResponse.getMain() != null) {
                        double temp = weatherResponse.getMain().getTemp();
                        temperature.setText(String.format("%.0f °C", temp)); // Dua angka desimal
                    } else {
                        temperature.setText("Suhu tidak tersedia.");
                    }

                    if (weatherResponse.getWeather() != null && !weatherResponse.getWeather().isEmpty()) {
                        weatherDescription.setText(weatherResponse.getWeather().get(0).getDescription());

                        String icon = weatherResponse.getWeather().get(0).getIcon();
                        String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";

                        Picasso.get().load(iconUrl).into(weatherIcon, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d("Picasso", "Icon loaded successfully");
                                weatherIcon.setVisibility(View.VISIBLE); // Tampilkan ikon cuaca
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("PicassoError", "Error loading image: " + e.getMessage());
                                weatherIcon.setVisibility(View.GONE); // Sembunyikan ikon jika ada kesalahan
                            }
                        });

                        // Mengatur visibilitas untuk elemen cuaca
                        temperature.setVisibility(View.VISIBLE);
                        weatherDescription.setVisibility(View.VISIBLE);
                    } else {
                        weatherDescription.setText("Deskripsi cuaca tidak tersedia.");
                    }
                } else {
                    weatherResult.setText("Gagal mendapatkan data cuaca");
                    clearWeatherInfo(); // Bersihkan informasi cuaca
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                weatherResult.setText("Kesalahan: " + t.getMessage());
                clearWeatherInfo(); // Bersihkan informasi cuaca
            }
        });
    }

    // Metode untuk menghapus informasi cuaca
    private void clearWeatherInfo() {
        temperature.setText("");
        weatherDescription.setText("");
        weatherIcon.setImageResource(0); // Menghapus ikon, tapi tidak menyembunyikan ImageView
        weatherIcon.setVisibility(View.GONE); // Sembunyikan ikon cuaca
    }
}
