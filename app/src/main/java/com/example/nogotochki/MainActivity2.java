package com.example.nogotochki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity2 extends AppCompatActivity implements LocListenerInterface {

    //Подключение и поиск GPS
    static final int GPS_CODE_REQUEST = 100; // код успешного предоставления gps пользователем
    LocationRequest locationRequest;
    LocationManager locationManager;
    MyLocListener myLocListener;
    Location loc;

    //Клиент
    OkHttpClient client;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    //Лента с мастерами/моделями
    RecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;
    //EditText searchBar;
    String searchType;
    Button searchButton;
    TextView searchInfoTextView;
    Spinner serviceTypeSpinner;
    List<ServiceProfile> services;

    //Кнопки навигации
    Button profileButton;
    Button masterButton;
    Button modelButton;

    //Переменные элементов профиля и слой, на котором они находятся
    ConstraintLayout constraintLayout;
    TextView profileName;
    TextView profilePhone;
    TextView profileInfo;
    Button changeProfileInfoButton;
    Button addServiceButton;
    boolean isSearchResponded;
    Handler mHandler;

    // Пользователь
    PeopleProfile user;
    String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Все запросы на запись от имени пользователя требуют токен авторизации
        // Который мы передаём
        Bundle arguments = getIntent().getExtras();
        authToken = arguments.getString("AuthToken");

        initialization();
        mHandler = new Handler(Looper.getMainLooper());

        // проверка наличия и использования gps и интернета (отчасти не используемый на данный момент функционал)
        //locationSettings();
        //checkPermissions();
        checkInternetConnection();

        // Дальше управление логикой кнопок

        // Переключение на профиль
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.INVISIBLE);
                //searchBar.setVisibility(View.INVISIBLE);
                searchButton.setVisibility(View.INVISIBLE);
                searchInfoTextView.setVisibility(View.INVISIBLE);
                serviceTypeSpinner.setVisibility(View.INVISIBLE);
                //
                constraintLayout.setVisibility(View.VISIBLE);

                // Показываются данные пользователя
                // По нажатию кнопки "Изменить" мы переходим на другое активити
                // И передаём токен авторизации

                // Сами данные подгружаются во вкладке OnStart(), потому что после изменения
                // данных профиля активити возобновляет работу и заново загружает данные пользователя


            }
        });

        // Кнопки в профиле: изменение профиля и создание услуги
        // Переход на активити изменения профиля
        changeProfileInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, ChangeProfileInfoActivity.class);
                // Для подключения к пользователю передаём токен
                intent.putExtra("AuthToken", authToken);

                startActivity(intent);
                onStop();
            }
        });

        // Переход на активити добавления услуги
        addServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, ServiceActivity.class);
                // Для подключения к пользователю передаём токен
                intent.putExtra("AuthToken", authToken);

                startActivity(intent);
                onStop();
            }
        });

        // Кнопка Я-модель, вкладка с поиском по мастерам
        modelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchType = "masters";
                recyclerView.setVisibility(View.VISIBLE);
                //searchBar.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.VISIBLE);
                searchInfoTextView.setVisibility(View.VISIBLE);
                serviceTypeSpinner.setVisibility(View.VISIBLE);
                //
                constraintLayout.setVisibility(View.INVISIBLE);

                services.clear();
                recyclerAdapter.notifyDataSetChanged();

                // Поиск будет производиться по мастерам, т.к. я-модель
            }
        });

        // Кнопка Я-мастер, вкладка с поиском мастеров
        masterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchType = "models";
                recyclerView.setVisibility(View.VISIBLE);
                //searchBar.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.VISIBLE);
                searchInfoTextView.setVisibility(View.VISIBLE);
                serviceTypeSpinner.setVisibility(View.VISIBLE);
                //
                constraintLayout.setVisibility(View.INVISIBLE);

                services.clear();
                recyclerAdapter.notifyDataSetChanged();

                // Поиск производится по моделям, т.к. Я-мастер
            }
        });

        // Поиск данных на сервере
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Тип людей определяется выбором вкладки модель/мастер, выбор типа сервиса - спиннером
                //String searchString = searchBar.getText().toString();
                // Сперва очищаем RecyclerView от прошлого поиска, потом делаем запрос
                services.clear();
                recyclerAdapter.notifyDataSetChanged();

                final String serviceType = serviceTypeSpinner.getSelectedItem().toString();

                isSearchResponded = false;
                String url = "http://176.119.157.211:5000/api/v1/search/" + searchType + "?last=50&type=" + serviceType;
                //создаём запрос, указывая адрес и параметры
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                //исполняем запрос асинхронно, ограничение операционной системы android
                Call call = client.newCall(request);
                call.enqueue(new Callback() {

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        //ВАЖНО! Тут не главный поток, менять UI отсюда нельзя, надо на UI-поток переключаться
                        //List<ServiceProfile> searchedServices = new ArrayList<>();
                        System.out.println("on Success");
                        try {
                            JSONArray responses = new JSONArray(response.body().string());
                            // Проверка результата запроса на наличие информации
                            if (responses.length() > 0) {
                                // Проход по всем полученным данным
                                for (int i = 0; i < responses.length(); i++)
                                {
                                    System.out.println(responses.getJSONObject(i));
                                    // Избыточная проверка
                                    if (responses.getJSONObject(i).getString("type").equals(serviceType)) {
                                        JSONObject obj = responses.getJSONObject(i);
                                        System.out.println(obj);
                                        // Объявление нового экземпляра сервиса, запись туда информации
                                        ServiceProfile model = new ServiceProfile();
                                        model.setId(obj.getString("id"));
                                        model.setPhoneNumber(obj.getString("phoneNumber"));
                                        model.setTitle(obj.getString("title"));
                                        if (model.getTitle() == null){
                                            model.setTitle(model.getId());
                                        }
                                        // Добавление в список, связанный с RecyclerView
                                        services.add(model);
                                    }
                                }

                                for (int i = 0; i < services.size(); i++)
                                {
                                    System.out.println(services.get(i).getPhoneNumber());
                                }
                            }
                            // Вывод информации, если услуг данного типа нет
                            else if (services.size() == 0) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "По вашему запросу ничего не найдено",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            /*mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mTextView.setText(mMessage); // must be inside run()
                                }
                            });*/

                            isSearchResponded = true;

                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                        call.cancel();
                    }

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        //ВАЖНО! Тут не главный поток, менять UI отсюда нельзя, надо на UI-поток переключаться
                        System.out.println("onFailure");
                        e.printStackTrace();
                        services.clear();
                        isSearchResponded = true;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Что-то пошло не так, запрос не отправлен",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        call.cancel();
                    }
                });
                //Проверка выполнения команд в другом потоке
                while (isSearchResponded == false){
                }
                // Уведомляем RecyclerView об изменении данных (даже если список остался пустым)
                recyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Через OnStart() мы загружаем информацию о пользователе в профиль, потому что при переходе
        // К изменению профиля мы стопим основной активити.
        // При выходе из активити изменения профиля мы переходим на этот активити и приложение само
        // Вызывает метод OnStart() для возобновления работы.
        client = new OkHttpClient();
        user = new PeopleProfile();
        final boolean[] isProfileResponded = {false};
        isProfileResponded[0] = false;

        String url = "http://176.119.157.211:5000/api/v1/users/me";
        Request request = new Request.Builder().url(url).addHeader("Authorization", authToken).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("User was connected successfully");
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    user.setId(jsonObject.getString("id"));
                    user.setPhoneNumber(jsonObject.getString("phoneNumber"));
                    user.setName(jsonObject.getString("name"));
                    user.setNickname(jsonObject.getString("nickname"));
                    if (user.getNickname() == null)
                    {
                        user.setNickname(user.getId());
                    }
                    user.setDescription(jsonObject.getString("description"));
                    System.out.println(user.getId());
                    System.out.println(user.getPhoneNumber());
                    System.out.println(user.getName());
                    System.out.println(user.getNickname());
                    System.out.println(user.getDescription());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                isProfileResponded[0] = true;
            }
            public void onFailure(Call call, IOException e) {
                System.out.println("Confirmation Token sending on server has failed");
                Toast.makeText(getApplicationContext(), "Что-то пошло не так, проблемы с авторизацией",
                        Toast.LENGTH_LONG).show();
                isProfileResponded[0] = true;
            }
        });

        while (isProfileResponded[0] == false){
        }

        profileName.setText(user.getName());
        profilePhone.setText(user.getPhoneNumber());
        profileInfo.setText(user.getDescription());
    }

    // Инициализация всех необходимых элементов
    void initialization(){
        services = new ArrayList<>();
        recyclerView = findViewById(R.id.RecyclerView);
        recyclerAdapter = new RecyclerAdapter(services);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        searchButton = findViewById(R.id.searchButton);
        searchInfoTextView = findViewById(R.id.searchInfoTextView);
        serviceTypeSpinner = findViewById(R.id.serviceTypeSpinner);
        isSearchResponded = false;

        ArrayAdapter<CharSequence> serviceTypesAdapter = ArrayAdapter.createFromResource(this, R.array.service_types, android.R.layout.simple_spinner_item);
        serviceTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceTypeSpinner.setAdapter(serviceTypesAdapter);

        constraintLayout = findViewById(R.id.profileLayout);
        profileButton = findViewById(R.id.profileButton);
        modelButton = findViewById(R.id.modelButton);
        masterButton = findViewById(R.id.masterButton);

        profileName = findViewById(R.id.profileNameTextView);
        profileInfo = findViewById(R.id.profileInfoTextView);
        profilePhone = findViewById(R.id.profilePhoneTextView);
        changeProfileInfoButton = findViewById(R.id.goToSettingsButton);
        addServiceButton = findViewById(R.id.addServiceButton);
    }

    // Проверка интернет соединения
    private void checkInternetConnection() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null){
            // сюда можно будет вставить дополнительные действия при подключении
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

            }

            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){

            }

        }
        else {
            // тут будет сообщение о необходимости интернета
            Toast.makeText(this, "Нет подключения, включите мобильный интернет", Toast.LENGTH_LONG);
        }


    }

    // Проверка GPS соединения, сейчас не используется
    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, GPS_CODE_REQUEST);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 5, myLocListener);
        }
    }

    // Настройки GPS соединения, сейчас не используется
    public void locationSettings(){
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * 50);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        myLocListener = new MyLocListener();
        myLocListener.setLocListenerInterface(this);
    }

    //Все перезаписанные методы

    //Обновление местоположения пользователя, сейчас не используется
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GPS_CODE_REQUEST && grantResults[0] == RESULT_OK) {
            Toast.makeText(this, "GPS, Разрешение получено", Toast.LENGTH_SHORT).show();
            checkPermissions();
        }
        else{
            Toast.makeText(this,"Нет разрешения на использование GPS", Toast.LENGTH_LONG);
            //finish();
        }
    }

    // Метод при изменении локации, здесь должно было записываться местоположение, сейчас не используется
    @Override
    public void OnLocationChanged(Location location) {
        Log.d("Longitude: ", String.valueOf(location.getLongitude()));
        Log.d("Latitude: ", String.valueOf(location.getLatitude()));
        //user.setLatitude(location.getLatitude());
        //user.setLongitude(location.getLongitude());
    }

    // Выход по нажатию кнопки "Назад" в панели навигации
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Выход");
        alertDialog.setMessage("Вы точно хотите выйти из приложения?");
        alertDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.create().show();
    }


}