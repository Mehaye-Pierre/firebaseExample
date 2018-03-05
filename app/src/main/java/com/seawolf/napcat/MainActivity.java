package com.seawolf.napcat;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.ConstraintHorizontalLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private int currentSlidePage = 0;
    private long time;
    private Timer timer;
    private final static int MAXPAGE = 2;
    private String userUid;

    //FIREBASE
    private FirebaseFirestore db;
    private final static String TAG = "FirestoreDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.buttonShop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseTime();
            }
        });
        findViewById(R.id.buttonSlideRIght).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPage();
            }
        });
        findViewById(R.id.buttonSlideLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousPage();
            }
        });
        fillPage();


        //FIREBASE
        db = FirebaseFirestore.getInstance();
        this.userUid = FirebaseAuth.getInstance().getUid();
        setCurrentTimeFromStore();

        runAnimation();
        timer=new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        time++;
                        setAndUpdateTime(time,false);
                    }
                });
            }
        }, 1000, 1000);

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.Reset:
                reset();
                return true;
            case R.id.Logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fillPage(){
        //TODO
    }


    private void nextPage(){
        this.currentSlidePage = (currentSlidePage+1)%MAXPAGE;
        fillPage();
    }

    private void previousPage(){
        this.currentSlidePage = (currentSlidePage-1)%MAXPAGE;
        fillPage();
    }

    //sets the time to 0 and update the store
    private void reset(){
        setAndUpdateTime(0,true);
    }

    private void increaseTime(){
        this.time += 60;

    }

    private void addCatItem(int id){
        LinearLayout layout = findViewById(R.id.linearLayoutToysDisplay);
        ImageButton button = new ImageButton(this);
        button.setImageResource(R.drawable.toy_0);
        button.setScaleType(ImageView.ScaleType.CENTER_CROP);
        layout.addView(button);
    }

    private void runAnimation(){
        ImageView cat = findViewById(R.id.imageViewCat);
        final AnimationDrawable animation = (AnimationDrawable) cat.getDrawable();
        animation.start();
        Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                animation.stop();
                animation.selectDrawable(0);
                animation.start();
            }
        };
    }

    //FIREBASE
    private void logout(){
        FirebaseAuth.getInstance().signOut();
        this.timer.cancel();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }


    //FIREBASE
    private void addCurrentTimeToStore(){
        Map<String, Object> user = new HashMap<>();
        user.put("time",this.time);

        db.collection("users").document(this.userUid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    //FIREBASE
    private void setCurrentTimeFromStore() {
        db.collection("users").document(this.userUid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        setAndUpdateTime((long) document.get("time"),false);
                        Log.d(TAG, "Success"+ (long) document.get("time"));
                    } else {
                        Log.d(TAG, "No such document");
                        setAndUpdateTime(0,true);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setAndUpdateTime(long t, boolean forceUpdate){
        this.time = t;
        ((TextView)findViewById(R.id.textViewPlaytime)).setText(String.valueOf((time)));
        if (time%15 == 0 || forceUpdate){
            addCurrentTimeToStore();
        }
    }
}