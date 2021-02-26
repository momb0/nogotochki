package com.example.nogotochki;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServiceActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client;
    JSONObject jsonObject;

    EditText createServiceNameEditText;
    EditText createServiceDescriptionEditText;
    EditText createServicePhoneNumberEditText;
    Spinner createServiceSpinner;
    Spinner createUserTypeSpinner;
    Button saveServiceButton;
    String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Bundle arguments = getIntent().getExtras();
        authToken = arguments.getString("AuthToken");

        createServiceNameEditText = findViewById(R.id.createServiceNameEditText);
        createServiceDescriptionEditText = findViewById(R.id.createServiceDescriptionEditText);
        createServicePhoneNumberEditText = findViewById(R.id.createServicePhoneNumberEditText);
        createServiceSpinner = findViewById(R.id.createServiceTypeSpinner);
        createUserTypeSpinner = findViewById(R.id.createUserTypeSpinner);
        saveServiceButton = findViewById(R.id.saveServiceButton);

        ArrayAdapter<CharSequence> servicesAdapter = ArrayAdapter.createFromResource(this, R.array.service_types, android.R.layout.simple_spinner_item);
        servicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        createUserTypeSpinner.setAdapter(servicesAdapter);

        ArrayAdapter<CharSequence> userTypesAdapter = ArrayAdapter.createFromResource(this, R.array.user_types, android.R.layout.simple_spinner_item);
        userTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        createServiceSpinner.setAdapter(userTypesAdapter);

        saveServiceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ServiceActivity.this);
                alertDialog.setTitle("Сохранить услугу");
                alertDialog.setMessage("Сохранить созданную услугу?");
                alertDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        client = new OkHttpClient();
                        ServiceProfile service = new ServiceProfile();
                        final String[] phone = new String[1];
                        final boolean[] isCompleted = {false, false};

                        if (checkPhoneNumber()) {
                            String serviceType = createServiceSpinner.getSelectedItem().toString();
                            Log.d("ServiceType", serviceType);
                            String userType = createUserTypeSpinner.getSelectedItem().toString();
                            Log.d("UserType", userType);
                            System.out.println(serviceType);
                            String url = "http://176.119.157.211:5000/api/v1/services";
                            String json = "{\"type\":\"" + userType + "\",\"searchType\":\"" + serviceType + "\"}";
                            Log.d("json", json);
                            RequestBody body = RequestBody.create(json, JSON);
                            Request request = new Request.Builder().url(url).addHeader("Authorization", authToken).post(body).build();
                            Call call = client.newCall(request);
                            call.enqueue(new Callback() {
                                public void onResponse(Call call, Response response) throws IOException {
                                    System.out.println("User was connected successfully");
                                    try {
                                        jsonObject = new JSONObject(response.body().string());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    isCompleted[0] = true;
                                }

                                public void onFailure(Call call, IOException e) {
                                    System.out.println("Authorization Token sending on server has failed");
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Что-то пошло не так, запрос не получен",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });

                            while (isCompleted[0] == false) {
                            }

                            String id = null;
                            try {
                                id = jsonObject.getString("id");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            JSONObject putRequest = jsonObject;

                            try {
                                putRequest.put("title", createServiceNameEditText.getText());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                putRequest.put("description", createServiceDescriptionEditText.getText());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                putRequest.put("phoneNumber", createServicePhoneNumberEditText.getText());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            url = "http://176.119.157.211:5000/api/v1/services/" + id;
                            body = RequestBody.create(putRequest.toString(), JSON);
                            request = new Request.Builder().url(url).addHeader("Authorization", authToken).put(body).build();
                            call = client.newCall(request);
                            call.enqueue(new Callback() {
                                public void onResponse(Call call, Response response) throws IOException {
                                    System.out.println("Service was created successfully");
                                    System.out.println(response.body().string());
                                    isCompleted[1] = true;
                                }

                                public void onFailure(Call call, IOException e) {
                                    System.out.println("Authorization Token sending on server has failed");
                                    Toast.makeText(getApplicationContext(), "Что-то пошло не так, проблемы с созданием услуги3",
                                            Toast.LENGTH_LONG).show();
                                }
                            });

                            while (isCompleted[1] == false) {
                            }
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Телефон введён неправильно",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
                alertDialog.create().show();


            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Сбросить изменения");
        alertDialog.setMessage("Вы точно хотите выйти? Созданная услуга не сохранится.");
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

    public boolean checkPhoneNumber() {
        // проверка длины телефона
        String phone = createServicePhoneNumberEditText.getText().toString();
        if (phone.length() != 11)
        {
            Toast.makeText(getApplicationContext(), "Номер телефона должен содержать 11 цифр",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        // проверка формата ввода номера телефона
        else if (!phone.startsWith("79"))
        {
            Toast.makeText(getApplicationContext(), "Неправильный формат телефона\nВведите телефон формата 79ХХХХХХХХХ",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        // Номер введён правильно
        else
        {
            return true;
        }
    }

    // Методы выпадающего списка
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String type = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}