package com.agni.asus.quiz;

import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.hanks.htextview.HTextView;
import com.hanks.htextview.HTextViewType;

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
    String base_url="https://opentdb.com/api.php?amount=10&type=multiple";
    ArrayList<QuestionModel> questionModelArrayList;
    int question_no=0;
    String json_response;
    String category_url="";
    String difficulty_url="";
    StringBuilder request_url;
    HTextView cat,diff;
    Random random;
    Spinner spinner1,spinner2;
    TextView question_view;
    TextView optn1,optn2,optn3,optn4;
    ActionProcessButton next,previous;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.app_bar_layout_2);
        View view=getSupportActionBar().getCustomView();

        questionModelArrayList=new ArrayList<>();
        random=new Random();
        question_view=findViewById(R.id.question_view);
        next=findViewById(R.id.next);
        previous=findViewById(R.id.previous);
        optn1=findViewById(R.id.option1);
        optn2=findViewById(R.id.option2);
        optn3=findViewById(R.id.option3);
        optn4=findViewById(R.id.option4);
        cat=findViewById(R.id.category);
        diff=findViewById(R.id.difficulty);
        spinner1=view.findViewById(R.id.spinner);
        spinner2=view.findViewById(R.id.spinner2);

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
                question_no++;
                if (question_no<10){
                    question_view.setText(Html.fromHtml(questionModelArrayList.get(question_no).getQuestion())); //to render html symbols like &quot , &amp etc etc
                    diff.animateText("Difficulty : "+questionModelArrayList.get(question_no).getDifficulty());
                    cat.animateText(questionModelArrayList.get(question_no).getCategory());
                    assignAnswers(question_no);
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
                    diff.animateText("Difficulty : "+questionModelArrayList.get(question_no).getDifficulty());
                    cat.animateText(questionModelArrayList.get(question_no).getCategory());
                    assignAnswers(question_no);
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
                request_url=new StringBuilder();
                request_url.append(base_url).append(category_url).append(difficulty_url);
                Log.d("Spinner1","checked");
                //new MyAsyncTask().execute(request_url.toString());
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
                request_url = new StringBuilder();
                request_url.append(base_url).append(category_url).append(difficulty_url);
                Log.d("Spinner2","Checked");
           //     new MyAsyncTask().execute(request_url.toString());
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
        optn1.setOnClickListener(null);
        optn2.setOnClickListener(null);
        optn3.setOnClickListener(null);
        optn4.setOnClickListener(null);
        switch (pos){
            case 0:
                optn1.setText(Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer())+" tick");
                optn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(),"COrrect",Toast.LENGTH_SHORT).show();
                    }
                });
                optn2.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn3.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn4.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));

                break;
            case 1:
                optn2.setText(Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer())+" tick");
                optn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(),"COrrect",Toast.LENGTH_SHORT).show();
                    }
                });
                optn1.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn3.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn4.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));
                break;
            case 2:
                optn3.setText(Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer())+" tick");
                optn3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(),"COrrect",Toast.LENGTH_SHORT).show();
                    }
                });
                optn1.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn2.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn4.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));
                break;
            case 3:
                optn4.setText(Html.fromHtml(questionModelArrayList.get(i).getCorrect_answer())+" tick");
                optn4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(),"COrrect",Toast.LENGTH_SHORT).show();
                    }
                });
                optn1.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(0)));
                optn2.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(1)));
                optn3.setText(Html.fromHtml(questionModelArrayList.get(i).getIncorrect_answers().get(2)));
                break;
        }
    }
}
