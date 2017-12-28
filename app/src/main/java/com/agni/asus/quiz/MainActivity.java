package com.agni.asus.quiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.daasuu.cat.CountAnimationTextView;
import com.dd.processbutton.iml.ActionProcessButton;
import com.hanks.htextview.HTextView;
import com.hanks.htextview.HTextViewType;
import com.skyfishjy.library.RippleBackground;


public class MainActivity extends AppCompatActivity {
    RippleBackground rippleTitle;
    TextView title_text;
    HTextView hTextView;
    ActionProcessButton q_and_answer_btn,quiz_start_btn;
    CountAnimationTextView score_counter;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final String pref_key="my_pref";
    private static final String score_key="score_key";
    private static final String user="user_name";
    private String score="100";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.app_bar_layout);
        View view=getSupportActionBar().getCustomView();

        title_text=view.findViewById(R.id.title_text);
        rippleTitle=view.findViewById(R.id.ripple_view);
        score_counter=(CountAnimationTextView)findViewById(R.id.score_counter);
        hTextView=(HTextView)findViewById(R.id.htextview);
        q_and_answer_btn=(ActionProcessButton)findViewById(R.id.question_answer);
        quiz_start_btn=(ActionProcessButton)findViewById(R.id.quiz_start);

        sharedPreferences=getSharedPreferences(pref_key,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        editor.putString(score_key,score);
        editor.putString(user,"USER");
        editor.commit();


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

        score_counter.setAnimationDuration(1500).countAnimation(0,Integer.parseInt(sharedPreferences.getString(score_key,"")));
        hTextView.setAnimateType(HTextViewType.TYPER);
        hTextView.animateText("Welcome "+sharedPreferences.getString(user,""));
        title_text.setText("HOME");
    }
}
