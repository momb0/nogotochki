package com.example.nogotochki;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ChangeProfileInfoActivity extends AppCompatActivity {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    EditText profileNameEditText;
    EditText profileNicknameEditText;
    EditText profileDescriptionEditText;
    Button profileSaveButton;
    String authToken;

    String profileName;
    String profileNickname;
    String profileDescription;
    String userId;
    JSONObject jsonObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_info2);

        Bundle arguments = getIntent().getExtras();
        authToken = arguments.getString("AuthToken");

        profileNameEditText = findViewById(R.id.profileNameEditText);
        profileNicknameEditText = findViewById(R.id.profileNicknameEditText);
        profileDescriptionEditText = findViewById(R.id.profileDescriptionEditText);
        profileSaveButton = findViewById(R.id.profileSaveButton);


        profileSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChangeProfileInfoActivity.this);
                alertDialog.setTitle("Обновление профиля");
                alertDialog.setMessage("Сохранить изменения?");
                alertDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Запрос на обновление профиля

                        //Первый запрос, для получения данных о пользователе
                        OkHttpClient client = new OkHttpClient();
                        final boolean[] isCompleted = {false, false, false};
                        String url = "http://176.119.157.211:5000/api/v1/users/me";
                        Request request = new Request.Builder().url(url).addHeader("Authorization", authToken).build();
                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            public void onResponse(Call call, Response response) throws IOException {
                                System.out.println("User was connected successfully");
                                try {
                                    jsonObject = new JSONObject(response.body().string());
                                    profileName = jsonObject.getString("name");
                                    profileNickname = jsonObject.getString("nickname");
                                    profileDescription = jsonObject.getString("description");
                                    userId = jsonObject.getString("id");
                                    isCompleted[0] = true;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            public void onFailure(Call call, IOException e) {
                                System.out.println("Confirmation Token sending on server has failed");
                                Toast.makeText(getApplicationContext(), "Что-то пошло не так, проблемы с авторизацией",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                        while (isCompleted[0] == false){
                        }

                        //Второй запрос, для доступа к изменению данных
                        url = "http://176.119.157.211:5000/api/v1/users/" + userId + "/services";
                        request = new Request.Builder().url(url).addHeader("Authorization", authToken).build();
                        call = client.newCall(request);
                        call.enqueue(new Callback() {
                            public void onResponse(Call call, Response response) throws IOException {
                                System.out.println("User was connected to services successfully");
                                isCompleted[1] = true;
                            }
                            public void onFailure(Call call, IOException e) {
                                System.out.println("Confirmation Token sending on server has failed");
                                Toast.makeText(getApplicationContext(), "Что-то пошло не так, проблемы с авторизацией",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                        while (isCompleted[1] == false){
                        }

                        // Запрос с изменением данных пользователя
                        url = "http://176.119.157.211:5000/api/v1/users/" + userId;
                        try {
                            jsonObject.put("name", profileNameEditText.getText());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            jsonObject.put("nickname", profileNicknameEditText.getText());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            jsonObject.put("description", profileDescriptionEditText.getText());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        RequestBody body = RequestBody.create(String.valueOf(jsonObject), JSON);

                        System.out.println(String.valueOf(jsonObject));

                        request = new Request.Builder().url(url).addHeader("Authorization", authToken).put(body).build();
                        call = client.newCall(request);
                        call.enqueue(new Callback() {
                            public void onResponse(Call call, Response response) throws IOException {
                                System.out.println("User info was changed successfully");
                                isCompleted[2] = true;
                            }
                            public void onFailure(Call call, IOException e) {
                                System.out.println("Confirmation Token sending on server has failed");
                                Toast.makeText(getApplicationContext(), "Что-то пошло не так, проблемы с авторизацией",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                        while (isCompleted[2] == false){
                        }

                        finish();
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
        alertDialog.setMessage("Вы точно хотите выйти? Изменения профиля не сохранятся.");
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