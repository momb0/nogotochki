package com.example.nogotochki;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ActivityLogin extends AppCompatActivity {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

        private Button button;
        private TextView textView;
        private EditText editTextNumber;
        private LinearLayout linearLayout;
        private EditText editTextPhone;
        String phoneNumber;
        String codeSMS;

        OkHttpClient client;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            client = new OkHttpClient();

            button = (Button) findViewById(R.id.button);
            textView = (TextView) findViewById(R.id.textView4);
            editTextPhone = (EditText) findViewById(R.id.editTextPhone);
            editTextNumber = (EditText) findViewById(R.id.editTextNumber);

            button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    boolean checkPhone = checkPhoneNumber();
                    boolean checkSMS = false;
                    if (checkPhone == true){
                        checkSMS = checkSMSCode();
                    }

                    // если проверку кода прошло успешно
                    if (checkPhone == true && checkSMS == true)
                    {
                        // Запрос post на апи, отсылаем вместе с кодом из смс
                        final String[] responses = new String[]{null, null, null};
                        String json = "{\"phoneNumber\":\"" + phoneNumber + "\"," +
                                "\"confirmationType\":\"authentication\"," +
                                "\"confirmationCode\":\"" + codeSMS + "\"}";
                        String url = "http://176.119.157.211:5000/api/v1/confirmation/sms";
                        RequestBody body = RequestBody.create(json, JSON);
                        Request request = new Request.Builder().url(url).post(body).build();
                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            public void onResponse(Call call, Response response) throws IOException {
                                System.out.println("SMS-code Successfully sent on server");
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    responses[0] = jsonObject.getString("confirmationToken");
                                    System.out.println(responses[0]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            public void onFailure(Call call, IOException e) {
                                System.out.println("SMS-code sending on server has failed");
                                Toast.makeText(getApplicationContext(), "Что-то пошло не так, проблемы с авторизацией",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                        while (responses[0] == null){
                        }

                        // Запрос post на апи, Post authentication с Confirmation токеном
                        json = "";
                        url = "http://176.119.157.211:5000/api/v1/authentication";
                        body = RequestBody.create(json, JSON);
                        request = new Request.Builder().url(url).addHeader("Confirmation", responses[0]).post(body).build();
                        call = client.newCall(request);
                        call.enqueue(new Callback() {
                            public void onResponse(Call call, Response response) throws IOException {
                                System.out.println("Confirmation Token was sent successfully");
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());

                                    responses[1] = jsonObject.getString("authorizationToken");
                                    System.out.println(responses[1]);
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

                        while (responses[1] == null){
                        }

                        Intent intent = new Intent(ActivityLogin.this, MainActivity2.class);
                        //intent.putExtra(PeopleProfile.class.getSimpleName(), user);
                        String authToken = responses[1];
                        intent.putExtra("AuthToken", authToken); //Optional parameters


                        finish();
                        startActivity(intent);

                    }
                }
            });
        }

    public boolean checkPhoneNumber() {
        // проверка длины телефона
        String phone = editTextPhone.getText().toString();
        if (phone.length() != 11)
        {
            Toast.makeText(getApplicationContext(), "Номер телефона должен содержать 11 цифр",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        // проверка формата ввода номера телефона
        else if (!phone.startsWith("79"))
        {
            Toast.makeText(getApplicationContext(), "Неправильный формат телефона",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        // Телефон введён правильно, присылаем код, открываем доступ к окну с вводом кода из смс
        // После первой проверки пользователь не сможет войти, т.к. ему надо ввести смс-код
        else if (editTextNumber.getVisibility() != View.VISIBLE)
        {
            phoneNumber = editTextPhone.getText().toString();
            final boolean[] isConnected = {Boolean.parseBoolean(null)};
            postPhone(isConnected);
            while (isConnected[0] == Boolean.parseBoolean(null)){
            }
            if (isConnected[0]) {
                textView.setVisibility(View.VISIBLE);
                editTextNumber.setVisibility(View.VISIBLE);
            }
            return false;
        }

        //Если введёный телефон был изменён
        else if (!phone.equals(phoneNumber))
        {
            phoneNumber = phone;
            Toast.makeText(getApplicationContext(), "Жди новую смску с кодом, ты изменил телефон",
                    Toast.LENGTH_LONG).show();
            final boolean[] isConnected = {false};
            postPhone(isConnected);

            return false;
        }

        // Номер введён правильно, вторая или более попытка
        else
        {
            // Здесь никаких кодов не отправляем, отправляем после проверки смс-кода
            return true;
        }
    }

    public boolean checkSMSCode()
    {

        // Проверка длины кода из смс
        if (editTextNumber.length() != 4)
        {
            Toast.makeText(getApplicationContext(), "Код должен состоять из четырёх цифр",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // Проверка правильности ввода кода из смс
        else if (!editTextNumber.getText().toString().equals(codeSMS))
        {
            Toast.makeText(getApplicationContext(), "Неверный код, 1337 введи, поможет, брат",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // Код введён правильно
        else
        {
            //запрос на сервер будет в теле onCreate
            return true;
        }
    }

    public void postPhone(final boolean[] isConnected){

        String json = "{\"phoneNumber\":\"" + phoneNumber + "\",\"confirmationType\":\"authentication\"}";
        String url = "http://176.119.157.211:5000/api/v1/confirmation/sms/sending";
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(url).post(body).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("on Success");
                System.out.println(response.body().string());
                isConnected[0] = true;
            }
            public void onFailure(Call call, IOException e) {
                System.out.println("onFailure");
                Toast.makeText(getApplicationContext(), "Что-то пошло не так, мы не можем прислать вам смс",
                        Toast.LENGTH_LONG).show();
                isConnected[0] = false;
            }
        });
        codeSMS = "1337";
    }
}