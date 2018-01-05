package com.agni.asus.quiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.daasuu.cat.CountAnimationTextView;

import es.dmoral.toasty.Toasty;

public class SplashScreen extends AppCompatActivity {
    CountAnimationTextView load_counter;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final String pref_key="my_pref";
    private static final String score_key="score_key";
    private static final String isfirsttime="isfirsttime";
    private static final String correct_key="correctkey";
    private static final String wrong_key="wrongkey";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        load_counter=findViewById(R.id.loading_counter);
        sharedPreferences=getSharedPreferences(pref_key,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        load_counter.setAnimationDuration(1600).countAnimation(0,100);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (sharedPreferences.getBoolean(isfirsttime,true)){
                    if (haveNetworkConnection()){
                        Intent i = new Intent(SplashScreen.this, FirstTime.class);
                        editor.putString(score_key,"0");
                        editor.putString(wrong_key,"0");
                        editor.putString(correct_key,"0");
                        editor.commit();
                        startActivity(i);
                        finish();
                    }else {
                        Toasty.error(getApplicationContext(),"No Internet Connection", Toast.LENGTH_LONG,true).show();
                    }
                }else {
                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(i);
                    finish();
                    overridePendingTransition(R.anim.zoom_in,R.anim.fadeout);
                }
            }
        }, 1700);
    }
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
