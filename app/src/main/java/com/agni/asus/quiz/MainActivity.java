package com.agni.asus.quiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.cat.CountAnimationTextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hanks.htextview.HTextView;
import com.hanks.htextview.HTextViewType;
import com.skyfishjy.library.RippleBackground;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MainActivity extends AppCompatActivity {
    TextView title_text,myMessage;
    HTextView hTextView;
    FancyButton q_and_answer_btn,quiz_start_btn;
    CountAnimationTextView score_counter;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView playstore,facebook;
    ImageView addQuestion,about,home;
    private static final String isfirsttime="isfirsttime";
    private static final String unique_id="unique_key";
    private static final String pref_key="my_pref";
    private static final String score_key="score_key";
    private static final String user="user_name";
    int flag=0;
    public static String FACEBOOK_URL = "https://www.facebook.com/98agni/";
    public static String FACEBOOK_PAGE_ID = "AGNI";
    FirebaseDatabase database;
    String SHOWCASE_ID="998899";
    DatabaseReference databaseCountReference,databaseReference_user,databaseReference_mymessage;
    private FirebaseAuth.AuthStateListener mauthStateListener;
    private FirebaseAuth mAuth;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.app_bar_layout);
        final View view=getSupportActionBar().getCustomView();


        MobileAds.initialize(this,
                "ca-app-pub-6163150982101301~3875090084");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6163150982101301/8625263008");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        addQuestion=view.findViewById(R.id.add_question);
        home=view.findViewById(R.id.home_image);
        about=view.findViewById(R.id.about);
        q_and_answer_btn=findViewById(R.id.question_answer);
        quiz_start_btn=findViewById(R.id.quiz_start);
        myMessage=view.findViewById(R.id.textViewMyMessage);
        title_text=view.findViewById(R.id.title_text);
        score_counter=(CountAnimationTextView)findViewById(R.id.score_counter);
        hTextView=(HTextView)findViewById(R.id.htextview);

        if(! haveNetworkConnection()){
            Toasty.warning(getApplicationContext(),"No Internet. Restart app",Toast.LENGTH_LONG,true).show();
            quiz_start_btn.setEnabled(false);
            q_and_answer_btn.setEnabled(false);
            addQuestion.setEnabled(false);
        }

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


        sharedPreferences=getSharedPreferences(pref_key,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        //editor.putString(score_key,score);
        //editor.putString(user,"USER");
        //editor.commit();

        //Toast.makeText(getApplicationContext(),sharedPreferences.getString(unique_id,""),Toast.LENGTH_SHORT).show();

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
        if (haveNetworkConnection()){

            hTextView.animateText("Welcome "+sharedPreferences.getString(user,""));
        }else {

            hTextView.animateText("Turn On Internet. Restart App");
        }
        title_text.setText("HOME");
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

        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://opentdb.com/"));
                startActivity(intent);
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title_text.setText("About");
                home.setVisibility(View.INVISIBLE);
                flag=1;
                setContentView(R.layout.layout);
                playstore=findViewById(R.id.playstore);
                playstore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=package com.agni.asus.quiz"));
                        startActivity(intent);
                    }
                });
                facebook=findViewById(R.id.facebook);
                facebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                        String facebookUrl = getFacebookPageURL(getApplicationContext());
                        facebookIntent.setData(Uri.parse(facebookUrl));
                        startActivity(facebookIntent);
                    }
                });
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

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(100); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(quiz_start_btn,
                "QUIZ\n\nPoints Earned\n\nEasy       :  5pts\nMedium :  10pts\nHard       :  15pts\n\nWrong Answer : -4pts", "GOT IT");

        sequence.addSequenceItem(q_and_answer_btn,
                "Q&A\n\nOften visit here for knowledge", "GOT IT");

        sequence.addSequenceItem(addQuestion,
                "Suggest or Add question to the database","GOT IT");

        sequence.addSequenceItem(about,
                "ABOUT\n\nRate/Review/Suggest Improvments","GOT IT");

        sequence.start();

    }



    private void incrementMainCount(DataSnapshot dataSnapshot) {
        String count=dataSnapshot.child("mainactivity").getValue().toString();
        Log.d("count",count);
        databaseCountReference.child("mainactivity").setValue(Integer.toString(Integer.valueOf(count)+1));
    }

    @Override
    protected void onResume() {
        if (FirebaseDatabase.getInstance() != null)
        {
            FirebaseDatabase.getInstance().goOnline();
        }
        super.onResume();
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
        if(FirebaseDatabase.getInstance()!=null)
        {
            FirebaseDatabase.getInstance().goOffline();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (flag==0){
            Calendar calander = Calendar.getInstance();
            SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String Date = simpledateformat.format(calander.getTime());
            if (sharedPreferences.getString(unique_id,"").length()>1){
                databaseReference_user.child(sharedPreferences.getString(unique_id,"")).child("log").push().setValue("Closed(BackPressed) : "+Date);
            }
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            finish();
            super.onBackPressed();
        }else if (flag==1){
            flag=0;
            title_text.setText("HOME");
            home.setVisibility(View.VISIBLE);
            setContentView(R.layout.activity_main);
        }

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
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }

}
