package com.example.nakatsukakentaro.socketex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class InpputAddressActivity extends AppCompatActivity {
    private EditText ipEditText;
    private EditText portEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_address);

        ipEditText = (EditText) findViewById(R.id.ipedittext);
        portEditText = (EditText) findViewById(R.id.portedittext);

    }

    public void connectButton(View view) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("ipaddress", ipEditText.getText().toString());
        intent.putExtra("portnumber", portEditText.getText().toString());

        startActivity(intent);
    }
}
