package com.agni.asus.quiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;

public class FirstTime extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    TextView textView;
    EditText editText;
    ActionProcessButton skip,ok;
    private static final String pref_key="my_pref";
    private static final String isfirsttime="isfirsttime";
    private static final String user="user_name";
    private static final String unique_id="unique_key";
    SharedPreferences.Editor editor;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference,databaseReference_user;
    private FirebaseAuth.AuthStateListener mauthStateListener;
    private FirebaseAuth mAuth;
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
        setTitle("Welcome");
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("count");
        databaseReference_user= firebaseDatabase.getReference("users");

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


        skip.setVisibility(View.GONE); // comment for skip option

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().length()>2){
                    Intent i = new Intent(FirstTime.this, MainActivity.class);
                    editor.putBoolean(isfirsttime,false);
                    editor.putString(user,editText.getText().toString());
                    editor.commit();
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            incrementCount(dataSnapshot);
                            registerUser(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    Log.d("ok","called");
                    startActivity(i);
                    finish();
                }else if (editText.getText().length()==0){
                    Toast.makeText(getApplicationContext(),"Enter your Name or Nick Name",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"Name should be atleast 3 letter in length",Toast.LENGTH_SHORT).show();
                }
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FirstTime.this, MainActivity.class);
                editor.putBoolean(isfirsttime,false);
                editor.putString(user,"User");
                editor.commit();
                Log.d("skip","Called");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        incrementCountSkip(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                startActivity(i);
                finish();
            }
        });
    }

    private void registerUser(DataSnapshot dataSnapshot) {
        String unique_key=databaseReference_user.push().getKey();
        //Toast.makeText(getApplicationContext(),unique_key,Toast.LENGTH_SHORT).show();
        editor=sharedPreferences.edit();
        editor.putString(unique_id,unique_key);
        editor.commit();
        //Toast.makeText(getApplicationContext(),sharedPreferences.getString(unique_id,""),Toast.LENGTH_SHORT).show();
        databaseReference_user.child(unique_key).child("name").setValue(editText.getText().toString());
    }

    private void incrementCount(DataSnapshot dataSnapshot) {
        String count=dataSnapshot.child("registration").child("registered").getValue().toString();
        databaseReference.child("registration").child("registered").setValue(Integer.toString(Integer.valueOf(count)+1));
        Log.d("CountZ",count);
        Log.d("IncCounter","called");
    }

    private void incrementCountSkip(DataSnapshot dataSnapshot) {
        String count=dataSnapshot.child("registration").child("anonymous").getValue().toString();
        databaseReference.child("registration").child("anonymous").setValue(Integer.toString(Integer.valueOf(count)+1));
        Log.d("CountZ",count);
        Log.d("IncCounter","called");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mauthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mauthStateListener != null) {
            mAuth.removeAuthStateListener(mauthStateListener);
        }
    }
}
