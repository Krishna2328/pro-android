package com.nativetemplate.activities;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText susername;
    Button slogin_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        susername = (EditText) findViewById(R.id.xusername);
        slogin_button = (Button) findViewById(R.id.xlogin_button);

        slogin_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.xlogin_button){
            Intent goto_home = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(goto_home);
        }
    }
}
