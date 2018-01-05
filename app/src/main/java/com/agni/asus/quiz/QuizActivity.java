package com.agni.asus.quiz;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.cat.CountAnimationTextView;
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
import com.victor.loading.book.BookLoading;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {

    String a_pholder="";
    String b_pholder="";
    String c_pholder="";
    String d_pholder="";

    Handler handler;
    Runnable runnable;

    ImageView tick1,tick2,tick3,tick4;
    String base_url="https://opentdb.com/api.php?amount=10&type=multiple";
    private static final String pref_key="my_pref";
    private static final String score_key="score_key";
    ArrayList<QuestionModel> questionModelArrayList;
    int question_no=0;
    String json_response;
    String category_url="";
    String difficulty_url="";
    int check1=0;
    int check2=0;
    BookLoading bookLoading;
    AVLoadingIndicatorView avLoadingIndicatorView;
    StringBuilder request_url;
    TextView title,nxt_vw,q_no;
    HTextView cat,diff;
    Random random;
    DatabaseReference databaseCountReference;
    FirebaseDatabase database;
    private FirebaseAuth.AuthStateListener mauthStateListener;
    private FirebaseAuth mAuth;
    ImageView imageView;
    ImageView img1,img2,img3,img4;
    Spinner spinner1,spinner2;
    TextView question_view;
    HTextView final_points;
    TextView optn1,optn2,optn3,optn4;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FancyButton next;
    ViewGroup viewGroup;
    CountAnimationTextView countAnimationTextView,next_qstn_in;
    KonfettiView konfettiView;
    int current_score=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        setContentView(R.layout.activity_quiz);
        current_score=0;
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.app_bar_layout_3);
        View view=getSupportActionBar().getCustomView();
        sharedPreferences=getSharedPreferences(pref_key,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        handler=new Handler();

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
                                Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }


                        }
                    });
        }

        database=FirebaseDatabase.getInstance();
        databaseCountReference=database.getReference("count");

        questionModelArrayList=new ArrayList<>();
        random=new Random();
        nxt_vw=findViewById(R.id.nxt_qst);
        bookLoading=findViewById(R.id.book_loading);
        final_points=findViewById(R.id.display_current_score_final);
        q_no=view.findViewById(R.id.question_no);
        img1=findViewById(R.id.img1);
        img2=findViewById(R.id.img2);
        img3=findViewById(R.id.img3);
        img4=findViewById(R.id.img4);
        tick1=findViewById(R.id.tick1);
        tick2=findViewById(R.id.tick2);
        tick3=findViewById(R.id.tick3);
        tick4=findViewById(R.id.tick4);
        tick1.setVisibility(View.GONE);
        tick2.setVisibility(View.GONE);
        tick3.setVisibility(View.GONE);
        tick4.setVisibility(View.GONE);
        viewGroup=findViewById(R.id.scroll_view);
        imageView=findViewById(R.id.q_mark_imgvw);
        avLoadingIndicatorView=findViewById(R.id.loading_indicator);
        question_view=findViewById(R.id.question_view);
        konfettiView=findViewById(R.id.viewKonfetti);
        avLoadingIndicatorView.show();
        bookLoading.start();
        title=findViewById(R.id.title_text);
        countAnimationTextView=view.findViewById(R.id.current_count);
        next_qstn_in=findViewById(R.id.next_qstn_in);
        next=findViewById(R.id.next);
        //previous=findViewById(R.id.previous);
        optn1=findViewById(R.id.option1);
        optn2=findViewById(R.id.option2);
        optn3=findViewById(R.id.option3);
        optn4=findViewById(R.id.option4);
        cat=view.findViewById(R.id.category);
        diff=view.findViewById(R.id.difficulty);
        spinner1=view.findViewById(R.id.spinner);
        spinner2=view.findViewById(R.id.spinner2);
        title.setText("Quiz");
        //previous.setVisibility(View.INVISIBLE);
        next.setText("SKIP");
        img1.setVisibility(View.INVISIBLE);
        img2.setVisibility(View.INVISIBLE);
        img3.setVisibility(View.INVISIBLE);
        img4.setVisibility(View.INVISIBLE);


        avLoadingIndicatorView.setVisibility(View.VISIBLE);
        bookLoading.setVisibility(View.VISIBLE);
        final_points.setVisibility(View.INVISIBLE);

        new MyAsyncTask().execute(base_url);

        ArrayAdapter<CharSequence> spinnerAdapter=ArrayAdapter.createFromResource(getApplicationContext(),R.array.category_arrays,android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner1.setAdapter(spinnerAdapter);

        ArrayAdapter<CharSequence> spinnerAdapter2=ArrayAdapter.createFromResource(getApplicationContext(),R.array.difficulty_list,android.R.layout.simple_spinner_item);
        spinnerAdapter2.setDropDownViewResource(R.layout.spinner_layout);
        spinner2.setAdapter(spinnerAdapter2);

        next.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                question_no++;
                optn1.setEnabled(true);
                optn2.setEnabled(true);
                optn3.setEnabled(true);
                optn4.setEnabled(true);
                //optn1.setBackgroundColor(android.R.color.transparent);
                //optn2.setBackgroundColor(android.R.color.transparent);
                //optn3.setBackgroundColor(android.R.color.transparent);
                //optn4.setBackgroundColor(android.R.color.transparent);
                tick1.setVisibility(View.GONE);
                tick2.setVisibility(View.GONE);
                tick3.setVisibility(View.GONE);
                tick4.setVisibility(View.GONE);
                next_qstn_in.setVisibility(View.INVISIBLE);
                nxt_vw.setVisibility(View.INVISIBLE);
          //      previous.setEnabled(true);
                next.setText("SKIP");
                if (question_no<10){
                    question_view.setText(Html.fromHtml(questionModelArrayList.get(question_no).getQuestion())); //to render html symbols like &quot , &amp etc etc
                    diff.animateText("Difficulty : "+questionModelArrayList.get(question_no).getDifficulty());
                    cat.animateText(questionModelArrayList.get(question_no).getCategory());
                    assignAnswers(question_no);
                    q_no.setText("Q "+Integer.toString(question_no+1)+"/10");
                }
                if (question_no==9){
                    next.setText("FINISH");
                }
                if (question_no==10){
                    next.setText("NEW ROUND");

                    imageView.setVisibility(View.INVISIBLE);
                    viewGroup.setVisibility(View.GONE);
                    optn1.setVisibility(View.GONE);
                    optn2.setVisibility(View.GONE);
                    optn3.setVisibility(View.GONE);
                    optn4.setVisibility(View.GONE);
                    nxt_vw.setVisibility(View.GONE);
                    next_qstn_in.setVisibility(View.GONE);

                    img1.setVisibility(View.INVISIBLE);
                    img2.setVisibility(View.INVISIBLE);
                    img3.setVisibility(View.INVISIBLE);
                    img4.setVisibility(View.INVISIBLE);
                    question_view.setVisibility(View.INVISIBLE);

                    cat.setVisibility(View.INVISIBLE);
                    countAnimationTextView.setVisibility(View.INVISIBLE);

                    if (current_score<10){
                        diff.animateText("Poor. Go to Q&A to practice.");
                    }else if (current_score>=10 && current_score<30){
                        diff.animateText("Below Average .Change difficulty from top right corner");
                    }else if (current_score>=30 && current_score<60){
                        diff.animateText("Good");
                    }else if (current_score>=60 && current_score<80){
                        diff.animateText("Well Done");
                    }else if (current_score>=80){
                        diff.animateText("Excellent Performance");
                    }

                    final_points.setVisibility(View.VISIBLE);
                    final_points.setAnimateType(HTextViewType.TYPER);
                    final_points.animateText("Points Earned : "+current_score);

                    if (current_score>10){
                        konfettiView.build()
                                .addColors(Color.YELLOW, Color.GREEN, Color.BLUE)
                                .setDirection(0.0, 359.0)
                                .setSpeed(1f, 5f)
                                .setFadeOutEnabled(true)
                                .setTimeToLive(2000L)
                                .addShapes(Shape.RECT, Shape.CIRCLE)
                                .addSizes(new Size(12,5))
                                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                                .stream(30, 5000L);
                    }
                }
                if (question_no==11){
                    request_url=new StringBuilder();
                    request_url.append(base_url).append(category_url).append(difficulty_url);
                    new MyAsyncTask().execute(request_url.toString());
                }
            }
        });
/*
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next.setEnabled(true);
                question_no--;
                if (question_no>=0){
                    question_view.setText(Html.fromHtml(questionModelArrayList.get(question_no).getQuestion())); //to render html symbols like &quot , &amp etc etc
                    diff.animateText("Difficulty : "+questionModelArrayList.get(question_no).getDifficulty());
                    cat.animateText(questionModelArrayList.get(question_no).getCategory());
                    assignAnswers(question_no);
                }
                if (question_no==0){
                    previous.setEnabled(false);
                }
            }
        });
*/
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (++check2>1) {
                    switch (position) {
                        case 0:
                            difficulty_url = "";
                            break;
                        case 1:
                            difficulty_url = "&difficulty=easy";
                            break;
                        case 2:
                            difficulty_url = "&difficulty=medium";
                            break;
                        case 3:
                            difficulty_url = "&difficulty=hard";
                            break;
                    }
                    request_url = new StringBuilder();
                    request_url.append(base_url).append(category_url).append(difficulty_url);
                    Log.d("Spinner2", "checked");
                    new MyAsyncTask().execute(request_url.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (++check1>1){
                    if (position==0){
                        category_url="";
                    }else {
                        category_url="&category="+Integer.toString(position+8);
                    }
                    request_url = new StringBuilder();
                    request_url.append(base_url).append(category_url).append(difficulty_url);
                    Log.d("Spinner1","Checked");
                    new MyAsyncTask().execute(request_url.toString());

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onResume() {
        if (FirebaseDatabase.getInstance() != null)
        {
            FirebaseDatabase.getInstance().goOnline();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(FirebaseDatabase.getInstance()!=null)
        {
            FirebaseDatabase.getInstance().goOffline();
        }
        super.onPause();
    }

    public class MyAsyncTask extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            Log.d("mysynctask :", "done");
            current_score=0;
            super.onPreExecute();
            questionModelArrayList.clear();
            question_no=0;
            countAnimationTextView.countAnimation(0,0);
            //previous.setEnabled(false);
            avLoadingIndicatorView.show();
            bookLoading.start();
            viewGroup.setVisibility(View.GONE);
            final_points.setVisibility(View.INVISIBLE);
            countAnimationTextView.setVisibility(View.INVISIBLE);
            avLoadingIndicatorView.setVisibility(View.VISIBLE);
            bookLoading.setVisibility(View.VISIBLE);
            next.setEnabled(false);
            imageView.setVisibility(View.INVISIBLE);
            question_view.setVisibility(View.INVISIBLE);
            optn1.setVisibility(View.GONE);
            optn2.setVisibility(View.GONE);
            optn3.setVisibility(View.GONE);
            optn4.setVisibility(View.GONE);
            nxt_vw.setVisibility(View.GONE);
            next.setText("SKIP");
            next_qstn_in.setVisibility(View.GONE);

            img1.setVisibility(View.INVISIBLE);
            img2.setVisibility(View.INVISIBLE);
            img3.setVisibility(View.INVISIBLE);
            img4.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient okHttpClient=new OkHttpClient();
            Response response=null;
            Request request=new Request.Builder()
                    .url(strings[0])
                    .build();
            try {
                response=okHttpClient.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (response != null) {
                    json_response=response.body().string();  // directly stored in global variable
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (json_response==null){
                Toasty.error(getApplicationContext(),"Server Error\nTry after some time",Toast.LENGTH_LONG,true).show();
            }

            avLoadingIndicatorView.hide();
            countAnimationTextView.setVisibility(View.VISIBLE);
            avLoadingIndicatorView.setVisibility(View.GONE);
            bookLoading.stop();
            bookLoading.setVisibility(View.GONE);
            next.setEnabled(true);

            imageView.setVisibility(View.VISIBLE);
            final_points.setVisibility(View.INVISIBLE);
            question_view.setVisibility(View.VISIBLE);
            optn1.setVisibility(View.VISIBLE);
            optn2.setVisibility(View.VISIBLE);
            optn3.setVisibility(View.VISIBLE);
            optn4.setVisibility(View.VISIBLE);

            viewGroup.setVisibility(View.VISIBLE);
            nxt_vw.setVisibility(View.INVISIBLE);
            next.setText("SKIP");
            next_qstn_in.setVisibility(View.INVISIBLE);
            q_no.setText("Q 1/10");

            img1.setVisibility(View.VISIBLE);
            img2.setVisibility(View.VISIBLE);
            img3.setVisibility(View.VISIBLE);
            img4.setVisibility(View.VISIBLE);

            cat.setVisibility(View.VISIBLE);

            databaseCountReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    incrementQuizcount(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //Toast.makeText(getApplicationContext(),json_response,Toast.LENGTH_LONG).show();
            try {
                JSONObject jsonObject=new JSONObject(json_response);
                if (jsonObject.getString("response_code").equals("0")){
                    //Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                    JSONArray jsonArray=jsonObject.getJSONArray("results");
                    for (int i=0;i<jsonArray.length();++i){
                        ArrayList<String> incorrect_ans=new ArrayList<>();
                        for (int j=0;j<jsonArray.getJSONObject(i).getJSONArray("incorrect_answers").length();++j){
                            incorrect_ans.add(jsonArray.getJSONObject(i).getJSONArray("incorrect_answers").getString(j));
                        }
                        //QuestionModel question=new QuestionModel("cat","typ","xx","que","crt",incorrect_ans);
                        QuestionModel question=new QuestionModel(jsonArray.getJSONObject(i).getString("category"),
                                jsonArray.getJSONObject(i).getString("type"),
                                jsonArray.getJSONObject(i).getString("difficulty"),
                                jsonArray.getJSONObject(i).getString("question"),
                                jsonArray.getJSONObject(i).getString("correct_answer"),incorrect_ans);
                        questionModelArrayList.add(question);
                    }

                    question_view.setText(Html.fromHtml(questionModelArrayList.get(0).getQuestion())); //to render html symbols like &quot , &amp etc etc
                    cat.setAnimateType(HTextViewType.EVAPORATE);
                    diff.setAnimateType(HTextViewType.EVAPORATE);
                    diff.animateText("Difficulty : "+questionModelArrayList.get(0).getDifficulty());
                    cat.animateText(questionModelArrayList.get(0).getCategory());

                    assignAnswers(0);

                }else {
                    Toasty.warning(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT,true).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void assignAnswers(final int i) {
        Log.d("assignanswer" ," executed");
        final int pos=random.nextInt(4);

        View.OnClickListener wrongClick=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int score=Integer.valueOf(sharedPreferences.getString(score_key,""));
                score-=4;
                current_score-=4;
                countAnimationTextView.setAnimationDuration(500).countAnimation(current_score+4,current_score);
                editor.putString(score_key,String.valueOf(score));
                editor.commit();
                Toasty.error(getApplicationContext(),"Wrong",Toast.LENGTH_SHORT,true).show();
                optn1.setEnabled(false);
                optn2.setEnabled(false);
                optn3.setEnabled(false);
                optn4.setEnabled(false);
                next.setText("NEXT");

                if (question_no==9){
                    next.setText("FINISH");
                }

                runnable=new Runnable() {
                    @Override
                    public void run() {
                        next.performClick();
                    }
                };

                switch (pos){
                    case 0:
                        //optn1.setBackgroundColor(getResources().getColor(R.color.correct_ans_bg));
                        tick1.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        //optn2.setBackgroundColor(getResources().getColor(R.color.correct_ans_bg));
                        tick2.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        //optn3.setBackgroundColor(getResources().getColor(R.color.correct_ans_bg));
                        tick3.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        //optn4.setBackgroundColor(getResources().getColor(R.color.correct_ans_bg));
                        tick4.setVisibility(View.VISIBLE);
                        break;
                }

                next_qstn_in.setVisibility(View.VISIBLE);
                nxt_vw.setVisibility(View.VISIBLE);

                next_qstn_in.clearAnimation();
                next_qstn_in.setAnimationDuration(3000).countAnimation(3,0);
                handler.postDelayed(runnable,3000);


            }
        };

        View.OnClickListener correctClick=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int score=Integer.valueOf(sharedPreferences.getString(score_key,""));
                if (questionModelArrayList.get(i).getDifficulty().equals("hard")){
                    score+=15;
                    current_score+=15;
                }
                if (questionModelArrayList.get(i).getDifficulty().equals("medium")){
                    score+=10;
                    current_score+=10;
                }
                if (questionModelArrayList.get(i).getDifficulty().equals("easy")){
                    score+=5;
                    current_score+=5;
                }
                countAnimationTextView.setAnimationDuration(500).countAnimation(current_score-10,current_score);
                editor.putString(score_key,String.valueOf(score));
                editor.commit();
                Toasty.success(getApplicationContext(),"Correct",Toast.LENGTH_SHORT,true).show();
                optn1.setEnabled(false);
                optn2.setEnabled(false);
                optn3.setEnabled(false);
                optn4.setEnabled(false);
                next.setText("NEXT");
                if (question_no==9){
                    next.setText("FINISH");
                }

                runnable=new Runnable() {
                    @Override
                    public void run() {
                        next.performClick();
                    }
                };

                next_qstn_in.setVisibility(View.VISIBLE);
                nxt_vw.setVisibility(View.VISIBLE);

                next_qstn_in.clearAnimation();
                next_qstn_in.setAnimationDuration(3000).countAnimation(3,0);
                handler.postDelayed(runnable,3000);


            }
        };

        optn1.setOnClickListener(wrongClick);
        optn2.setOnClickListener(wrongClick);
        optn3.setOnClickListener(wrongClick);
        optn4.setOnClickListener(wrongClick);

        switch (pos){
            case 0:
                optn1.setText(a_pholder+Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer()));
                optn1.setOnClickListener(correctClick);
                optn2.setText(b_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn3.setText(c_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn4.setText(d_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));

                break;
            case 1:
                optn2.setText(b_pholder+Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer()));
                optn2.setOnClickListener(correctClick);
                optn1.setText(a_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn3.setText(c_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn4.setText(d_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));
                break;
            case 2:
                optn3.setText(c_pholder+Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer()));
                optn3.setOnClickListener(correctClick);
                optn1.setText(a_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn2.setText(b_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn4.setText(d_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));
                break;
            case 3:
                optn4.setText(d_pholder+Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer()));
                optn4.setOnClickListener(correctClick);
                optn1.setText(a_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn2.setText(b_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn3.setText(c_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));
                break;
        }
    }
    private void incrementQuizcount(DataSnapshot dataSnapshot) {
        String count=dataSnapshot.child("quizStart").getValue().toString();
        Log.d("buttoncount",count);
        databaseCountReference.child("quizStart").setValue(Integer.toString(Integer.valueOf(count)+1));
    }
}
