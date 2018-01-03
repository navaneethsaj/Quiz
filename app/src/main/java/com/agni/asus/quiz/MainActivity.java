package com.agni.asus.quiz;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.cat.CountAnimationTextView;
import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hanks.htextview.HTextView;
import com.hanks.htextview.HTextViewType;
import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    RippleBackground rippleTitle;
    TextView title_text,myMessage;
    HTextView hTextView;
    ActionProcessButton q_and_answer_btn,quiz_start_btn;
    CountAnimationTextView score_counter;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final String isfirsttime="isfirsttime";
    private static final String unique_id="unique_key";
    private static final String pref_key="my_pref";
    private static final String score_key="score_key";
    private static final String user="user_name";
    private String score="100";
    FirebaseDatabase database;
    DatabaseReference databaseCountReference,databaseReference_user,databaseReference_mymessage;
    private FirebaseAuth.AuthStateListener mauthStateListener;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.app_bar_layout);
        final View view=getSupportActionBar().getCustomView();

        mAuth = FirebaseAuth.getInstance();
        mauthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("tag", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("tag", "onAuthStateChanged:signed_out");
                }

            }
        };

        if (mAuth.getCurrentUser() == null){
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("mauth", "OnComplete : " +task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Log.w("mauth", "Failed : ", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }


                        }
                    });
        }

        myMessage=view.findViewById(R.id.textViewMyMessage);
        title_text=view.findViewById(R.id.title_text);
        rippleTitle=view.findViewById(R.id.ripple_view);
        score_counter=(CountAnimationTextView)findViewById(R.id.score_counter);
        hTextView=(HTextView)findViewById(R.id.htextview);
        q_and_answer_btn=(ActionProcessButton)findViewById(R.id.question_answer);
        quiz_start_btn=(ActionProcessButton)findViewById(R.id.quiz_start);

        sharedPreferences=getSharedPreferences(pref_key,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        //editor.putString(score_key,score);
        //editor.putString(user,"USER");
        //editor.commit();

        Toast.makeText(getApplicationContext(),sharedPreferences.getString(unique_id,""),Toast.LENGTH_SHORT).show();

        database=FirebaseDatabase.getInstance();
        databaseCountReference=database.getReference("count");
        databaseReference_user=database.getReference("users");
        databaseReference_mymessage=database.getReference("mymessage");
        databaseCountReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                incrementMainCount(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseReference_mymessage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myMessage.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        score_counter.setAnimationDuration(1500).countAnimation(0,Integer.parseInt(sharedPreferences.getString(score_key,"")));
        hTextView.setAnimateType(HTextViewType.TYPER);
        hTextView.animateText("Welcome "+sharedPreferences.getString(user,""));
        title_text.setText("HOME");

        rippleTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN){
                    rippleTitle.startRippleAnimation();
                }else {
                    rippleTitle.stopRippleAnimation();
                }
                return true;
            }
        });
        q_and_answer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),Question_Answer.class);
                startActivity(intent);
            }
        });

        quiz_start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),QuizActivity.class);
                startActivity(intent);
                //score_counter.setAnimationDuration(1500).countAnimation(0,Integer.parseInt(sharedPreferences.getString(score_key,"")));
            }
        });

        //new FireFunctionTriggerAsyncTask().execute("https://us-central1-quiz-72bee.cloudfunctions.net/helloWorld");
        if (sharedPreferences.getString(unique_id,"").length()>1){
            databaseReference_user.child(sharedPreferences.getString(unique_id,"")).child("score").setValue(sharedPreferences.getString(score_key,""));
            Calendar calander = Calendar.getInstance();
            SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String Date = simpledateformat.format(calander.getTime());
            databaseReference_user.child(sharedPreferences.getString(unique_id,"")).child("log").push().setValue("Opened (onCreate) : "+Date);
        }

    }

    private void incrementMainCount(DataSnapshot dataSnapshot) {
        String count=dataSnapshot.child("mainactivity").getValue().toString();
        Log.d("count",count);
        databaseCountReference.child("mainactivity").setValue(Integer.toString(Integer.valueOf(count)+1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseDatabase.getInstance() != null)
        {
            FirebaseDatabase.getInstance().goOnline();
        }
        score_counter.setAnimationDuration(1500).countAnimation(0,Integer.parseInt(sharedPreferences.getString(score_key,"")));
        if (sharedPreferences.getString(unique_id,"").length()>1){
            databaseReference_user.child(sharedPreferences.getString(unique_id,"")).child("score").setValue(sharedPreferences.getString(score_key,""));
        }
        databaseCountReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                incrementMainCount(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class FireFunctionTriggerAsyncTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient okHttpClient=new OkHttpClient();
            Request request=new Request.Builder()
                    .url(strings[0])
                    .build();
            Response response=null;
            try {
                response=okHttpClient.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(),"Called",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mauthStateListener);
    }

    @Override
    protected void onStop() {
        if (mauthStateListener != null) {
            mAuth.removeAuthStateListener(mauthStateListener);
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(FirebaseDatabase.getInstance()!=null)
        {
            FirebaseDatabase.getInstance().goOffline();
        }
    }

    @Override
    public void onBackPressed() {
        Calendar calander = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String Date = simpledateformat.format(calander.getTime());
        if (sharedPreferences.getString(unique_id,"").length()>1){
            databaseReference_user.child(sharedPreferences.getString(unique_id,"")).child("log").push().setValue("Closed(BackPressed) : "+Date);
        }
        finish();
        super.onBackPressed();
    }


}
