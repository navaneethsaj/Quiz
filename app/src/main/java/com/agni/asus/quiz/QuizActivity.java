package com.agni.asus.quiz;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.cat.CountAnimationTextView;
import com.dd.processbutton.iml.ActionProcessButton;
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
    TextView title,nxt_vw;
    HTextView cat,diff;
    Random random;
    ImageView imageView;
    ImageView img1,img2,img3,img4;
    Spinner spinner1,spinner2;
    TextView question_view;
    HTextView final_points;
    TextView optn1,optn2,optn3,optn4;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ActionProcessButton next;
    CountAnimationTextView countAnimationTextView,next_qstn_in;
    int current_score=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        current_score=0;
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.app_bar_layout_2);
        View view=getSupportActionBar().getCustomView();
        sharedPreferences=getSharedPreferences(pref_key,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        handler=new Handler();

        questionModelArrayList=new ArrayList<>();
        random=new Random();
        nxt_vw=findViewById(R.id.nxt_qst);
        bookLoading=findViewById(R.id.book_loading);
        final_points=findViewById(R.id.display_current_score_final);
        img1=findViewById(R.id.img1);
        img2=findViewById(R.id.img2);
        img3=findViewById(R.id.img3);
        img4=findViewById(R.id.img4);
        imageView=findViewById(R.id.q_mark_imgvw);
        avLoadingIndicatorView=findViewById(R.id.loading_indicator);
        question_view=findViewById(R.id.question_view);
        avLoadingIndicatorView.show();
        bookLoading.start();
        title=findViewById(R.id.title_text);
        countAnimationTextView=findViewById(R.id.current_count);
        next_qstn_in=findViewById(R.id.next_qstn_in);
        next=findViewById(R.id.next);
        //previous=findViewById(R.id.previous);
        optn1=findViewById(R.id.option1);
        optn2=findViewById(R.id.option2);
        optn3=findViewById(R.id.option3);
        optn4=findViewById(R.id.option4);
        cat=findViewById(R.id.category);
        diff=findViewById(R.id.difficulty);
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
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(spinnerAdapter);

        ArrayAdapter<CharSequence> spinnerAdapter2=ArrayAdapter.createFromResource(getApplicationContext(),R.array.difficulty_list,android.R.layout.simple_spinner_item);
        spinnerAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(spinnerAdapter2);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                question_no++;
                optn1.setEnabled(true);
                optn2.setEnabled(true);
                optn3.setEnabled(true);
                optn4.setEnabled(true);
                next_qstn_in.setVisibility(View.INVISIBLE);
                nxt_vw.setVisibility(View.INVISIBLE);
          //      previous.setEnabled(true);
                next.setText("SKIP");
                if (question_no<10){
                    question_view.setText(Html.fromHtml(questionModelArrayList.get(question_no).getQuestion())); //to render html symbols like &quot , &amp etc etc
                    diff.animateText("Difficulty : "+questionModelArrayList.get(question_no).getDifficulty());
                    cat.animateText(questionModelArrayList.get(question_no).getCategory());
                    assignAnswers(question_no);
                }
                if (question_no==9){
                    next.setText("FINISH");
                }
                if (question_no==10){
                    next.setText("NEW ROUND");

                    imageView.setVisibility(View.INVISIBLE);
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

                    diff.animateText("Well Done");

                    final_points.setVisibility(View.VISIBLE);
                    final_points.setAnimateType(HTextViewType.TYPER);
                    final_points.animateText("Points Earned : "+current_score);
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
    public class MyAsyncTask extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            Log.d("mysynctask :", "done");
            super.onPreExecute();
            questionModelArrayList.clear();
            question_no=0;
            //previous.setEnabled(false);
            avLoadingIndicatorView.show();
            bookLoading.start();
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
                json_response=response.body().string();  // directly stored in global variable
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
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

            nxt_vw.setVisibility(View.INVISIBLE);
            next_qstn_in.setVisibility(View.INVISIBLE);

            img1.setVisibility(View.VISIBLE);
            img2.setVisibility(View.VISIBLE);
            img3.setVisibility(View.VISIBLE);
            img4.setVisibility(View.VISIBLE);

            //Toast.makeText(getApplicationContext(),json_response,Toast.LENGTH_LONG).show();
            try {
                JSONObject jsonObject=new JSONObject(json_response);
                if (jsonObject.getString("response_code").equals("0")){
                    Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void assignAnswers(int i) {
        Log.d("assignanswer" ," executed");
        int pos=random.nextInt(4);

        View.OnClickListener wrongClick=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int score=Integer.valueOf(sharedPreferences.getString(score_key,""));
                score-=4;
                current_score-=4;
                countAnimationTextView.setAnimationDuration(500).countAnimation(current_score+4,current_score);
                editor.putString(score_key,String.valueOf(score));
                editor.commit();
                Toast.makeText(getApplicationContext(),"Wrong",Toast.LENGTH_SHORT).show();
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

        View.OnClickListener correctClick=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int score=Integer.valueOf(sharedPreferences.getString(score_key,""));
                score+=10;
                current_score+=10;
                countAnimationTextView.setAnimationDuration(500).countAnimation(current_score-10,current_score);
                editor.putString(score_key,String.valueOf(score));
                editor.commit();
                Toast.makeText(getApplicationContext(),"COrrect",Toast.LENGTH_SHORT).show();
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
                optn1.setText(a_pholder+Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer())+" tick");
                optn1.setOnClickListener(correctClick);
                optn2.setText(b_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn3.setText(c_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn4.setText(d_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));

                break;
            case 1:
                optn2.setText(b_pholder+Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer())+" tick");
                optn2.setOnClickListener(correctClick);
                optn1.setText(a_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn3.setText(c_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn4.setText(d_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));
                break;
            case 2:
                optn3.setText(c_pholder+Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer())+" tick");
                optn3.setOnClickListener(correctClick);
                optn1.setText(a_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn2.setText(b_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn4.setText(d_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));
                break;
            case 3:
                optn4.setText(d_pholder+Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer())+" tick");
                optn4.setOnClickListener(correctClick);
                optn1.setText(a_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn2.setText(b_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn3.setText(c_pholder+Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));
                break;
        }
    }
}
