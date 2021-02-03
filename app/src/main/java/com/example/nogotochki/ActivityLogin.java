package com.example.nogotochki;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityLogin extends AppCompatActivity {

        private Button button;
        private TextView textView;
        private EditText editTextNumber;
        private LinearLayout linearLayout;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            button = (Button) findViewById(R.id.button);
            textView = (TextView) findViewById(R.id.textView4);
            editTextNumber = (EditText) findViewById(R.id.editTextNumber);

            button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (editTextNumber.getVisibility() != View.VISIBLE)
                    {
                        textView.setVisibility(View.VISIBLE);
                        editTextNumber.setVisibility(View.VISIBLE);
                    }
                    else if (Integer.parseInt(editTextNumber.getText().toString()) == 1337)
                    {
                        Intent myIntent = new Intent(ActivityLogin.this, MainActivity.class);
                        //myIntent.putExtra("key", value); //Optional parameters
                        ActivityLogin.this.startActivity(myIntent);
                    }
                    else
                    {
                        Context context = getApplicationContext();
                        Toast.makeText(context, "1337 введи, поможет, брат",
                                Toast.LENGTH_LONG).show();
                    }
                    // click handling code
                }
            });
            //linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

            /*button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = new Button(getApplicationContext());
                    b.setText("Удалить данную кнопку №" + Integer.toString(countID + 1));
                    b.setLayoutParams(
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT)
                    );
                    b.setId(USERID + countID);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            linearLayout.removeView(v);
                        }
                    });
                    linearLayout.addView(b);
                    countID++;
                }
            });*/
        }
}