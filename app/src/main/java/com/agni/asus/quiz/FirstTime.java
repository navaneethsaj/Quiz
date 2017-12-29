package com.agni.asus.quiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;

public class FirstTime extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    TextView textView;
    EditText editText;
    ActionProcessButton skip,ok;
    private static final String pref_key="my_pref";
    private static final String isfirsttime="isfirsttime";
    private static final String user="user_name";
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time);
        sharedPreferences=getSharedPreferences(pref_key,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        textView=findViewById(R.id.textView);
        editText=findViewById(R.id.editText);
        ok=findViewById(R.id.ok_btn);
        skip=findViewById(R.id.skip_btn);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FirstTime.this, MainActivity.class);
                editor.putBoolean(isfirsttime,false);
                editor.putString(user,editText.getText().toString());
                editor.commit();
                startActivity(i);
                finish();
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FirstTime.this, MainActivity.class);
                editor.putBoolean(isfirsttime,false);
                editor.putString(user,"User");
                editor.commit();
                startActivity(i);
                finish();
            }
        });
    }
}
