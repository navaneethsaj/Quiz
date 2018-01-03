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

import com.dd.processbutton.iml.ActionProcessButton;
import com.hanks.htextview.HTextView;
import com.hanks.htextview.HTextViewType;
import com.skyfishjy.library.RippleBackground;
import com.victor.loading.book.BookLoading;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Question_Answer extends AppCompatActivity {
    int question_no=0;
    String json_response;
    String category_url="";
    String difficulty_url="";
    StringBuilder request_url;
    String base_url="https://opentdb.com/api.php?amount=10";
    ArrayList<QuestionModel> questionModelArrayList;
    TextView question_view,answer_view;
    BookLoading bookLoading;
    RippleBackground rippleTitle;
    TextView title_text;
    Spinner spinner1,spinner2;
    HTextView cat,diff;
    ImageView imageView,tick_imageview;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    AVLoadingIndicatorView avLoadingIndicatorView;
    private static final String pref_key="my_pref";
    private static final String score_key="score_key";
    private static final String user="user_name";
    ActionProcessButton previous,next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        setContentView(R.layout.activity_question__answer);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.app_bar_layout_2);
        View view=getSupportActionBar().getCustomView();

        //btn_category=view.findViewById(R.id.category_btn);
        //btn_difficult=view.findViewById(R.id.difficulty_btn);

        sharedPreferences=getSharedPreferences(pref_key,MODE_PRIVATE);
        editor=sharedPreferences.edit();

        request_url=new StringBuilder();

        spinner1=(Spinner)view.findViewById(R.id.spinner);
        spinner2=(Spinner)view.findViewById(R.id.spinner2);
        title_text=view.findViewById(R.id.title_text);
        rippleTitle=view.findViewById(R.id.ripple_view);
        question_view=findViewById(R.id.question_view);
        answer_view=findViewById(R.id.answer_view);
        previous=findViewById(R.id.previous);
        imageView=findViewById(R.id.q_mark_imgvw);
        imageView.setVisibility(View.VISIBLE);
        tick_imageview=findViewById(R.id.tick_image);
        tick_imageview.setVisibility(View.INVISIBLE);
        cat=(HTextView)view.findViewById(R.id.category);
        diff=(HTextView)view.findViewById(R.id.difficulty);
        bookLoading=findViewById(R.id.book_loading);
        avLoadingIndicatorView=findViewById(R.id.loading_indicator);
        avLoadingIndicatorView.show();
        question_view.setVisibility(View.GONE);
        answer_view.setVisibility(View.GONE);
        bookLoading.start();
        title_text.setText("Q&A");
        next=findViewById(R.id.next);
        questionModelArrayList=new ArrayList<>();



        previous.setEnabled(false);

        new MyAsyncTask().execute(base_url);

        ArrayAdapter<CharSequence> spinnerAdapter=ArrayAdapter.createFromResource(this,R.array.category_arrays,android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner1.setAdapter(spinnerAdapter);

        ArrayAdapter<CharSequence> spinnerAdapter2=ArrayAdapter.createFromResource(this,R.array.difficulty_list,android.R.layout.simple_spinner_item);
        spinnerAdapter2.setDropDownViewResource(R.layout.spinner_layout);
        spinner2.setAdapter(spinnerAdapter2);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previous.setEnabled(true);
                question_no++;
                if (question_no < 10) {
                    question_view.setText(Html.fromHtml(questionModelArrayList.get(question_no).getQuestion())); //to render html symbols like &quot , &amp etc etc
                    answer_view.setText(questionModelArrayList.get(question_no).getCorrect_answer());
                    diff.animateText("Difficulty : "+questionModelArrayList.get(question_no).getDifficulty());
                    cat.animateText(questionModelArrayList.get(question_no).getCategory());
                }
                if (question_no==10){
                    request_url=new StringBuilder();
                    request_url.append(base_url).append(category_url).append(difficulty_url);
                    new MyAsyncTask().execute(request_url.toString());
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next.setEnabled(true);
                question_no--;
                if (question_no>=0){
                    question_view.setText(Html.fromHtml(questionModelArrayList.get(question_no).getQuestion())); //to render html symbols like &quot , &amp etc etc
                    answer_view.setText(questionModelArrayList.get(question_no).getCorrect_answer());
                    diff.animateText("Difficulty : "+questionModelArrayList.get(question_no).getDifficulty());
                    cat.animateText(questionModelArrayList.get(question_no).getCategory());
                }
                if (question_no==0){
                    previous.setEnabled(false);
                }
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        difficulty_url="";
                        break;
                    case 1:
                        difficulty_url="&difficulty=easy";
                        break;
                    case 2:
                        difficulty_url="&difficulty=medium";
                        break;
                    case 3:
                        difficulty_url="&difficulty=hard";
                        break;
                }
                Log.d("QASpinner2","executed");
                request_url=new StringBuilder();
                request_url.append(base_url).append(category_url).append(difficulty_url);
                new MyAsyncTask().execute(request_url.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position==0){
                    category_url="";
                }else {
                    category_url="&category="+Integer.toString(position+8);
                }

                Log.d("QASpinner1","executed");
                request_url = new StringBuilder();
                request_url.append(base_url).append(category_url).append(difficulty_url);
                new MyAsyncTask().execute(request_url.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    public class MyAsyncTask extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            question_no=0;
            tick_imageview.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            next.setEnabled(false);
            previous.setEnabled(false);
            questionModelArrayList.clear();
            bookLoading.start();
            bookLoading.setVisibility(View.VISIBLE);
            avLoadingIndicatorView.setVisibility(View.VISIBLE);
            avLoadingIndicatorView.show();
            answer_view.setVisibility(View.GONE);
            question_view.setVisibility(View.GONE);
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
            tick_imageview.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            bookLoading.stop();
            next.setEnabled(true);
            bookLoading.setVisibility(View.GONE);
            avLoadingIndicatorView.hide();
            avLoadingIndicatorView.setVisibility(View.INVISIBLE);
            answer_view.setVisibility(View.VISIBLE);
            question_view.setVisibility(View.VISIBLE);


            //Toast.makeText(getApplicationContext(),json_response,Toast.LENGTH_LONG).show();
            try {
                JSONObject jsonObject=new JSONObject(json_response);
                if (jsonObject.getString("response_code").equals("0")){
                    Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
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
                    answer_view.setText(questionModelArrayList.get(0).getCorrect_answer());

                    cat.setAnimateType(HTextViewType.EVAPORATE);
                    diff.setAnimateType(HTextViewType.EVAPORATE);
                    diff.animateText("Difficulty : "+questionModelArrayList.get(0).getDifficulty());
                    cat.animateText(questionModelArrayList.get(0).getCategory());

                }else {
                    Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }
}
