package com.bornintelligence.cellc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.gson.Gson;

import java.sql.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class game extends AppCompatActivity {
    private Location location;
    private EventDate eventDate;
    private int eventIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        hideSystemUI();
        Intent intent = getIntent();
        String json = intent.getStringExtra("selected");

        Gson gson = new Gson();
        location = gson.fromJson(json,Location.class);
        ArrayList<EventDate> dates = location.getDates();

        try {
            eventIndex = getTodayEvent(dates);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        eventDate = dates.get(eventIndex);

        ImageButton start = findViewById(R.id.startButton);
        ImageButton boost = findViewById(R.id.boostButton);
        ScrollView view = findViewById(R.id.slot);
        FlingAnimation fling = new FlingAnimation(view,DynamicAnimation.SCROLL_Y);
        view.setEnabled(false);
        view.scrollTo(0,125 );
        renderSpinner();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollView view = findViewById(R.id.slot);
                ImageButton sB = findViewById(R.id.startButton);
                ImageButton bB = findViewById(R.id.boostButton);

                view.scrollTo(0,125 );
                int vol = 1500;
                fling.setStartVelocity(vol)
                        .setMinValue(20)
                        .setMaxValue(2000000)
                        .setFriction(0.001f)
                        .start();
                fling.addEndListener(endAni);
                animateBoostIn();
                animateStartOut();
            }

        });



        boost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollView view = findViewById(R.id.slot);

                view.scrollTo(0,125 );
                int min = 3000;
                double rand = (Math.random() * 80);
                int cal = (((int)Math.round(rand)) * 100) + 75;
                int vol = (min + cal);
                fling.setStartVelocity(vol)
                        .setMinValue(20)
                        .setMaxValue(2000000)
                        .setFriction(0.1f)
                        .start();
                fling.addEndListener(endAni);
                animateBoostOut();
            }

        });
        ImageView prize = findViewById(R.id.prize);
        prize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animatePrizeOut();
                animateStartIn();

                Integer total = eventDate.getTotalLeft();

                if(total < 1){
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }


    DynamicAnimation.OnAnimationEndListener endAni = new DynamicAnimation.OnAnimationEndListener(){
        @Override
        public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
            ScrollView view = findViewById(R.id.slot);
            LinearLayout line = findViewById(R.id.line);

            ImageButton sB = findViewById(R.id.startButton);
            ImageButton bB = findViewById(R.id.boostButton);
            int count = line.getChildCount();
            View vi;
            View vf = null;
            double closest = 1000;
            float id = 0;

            int[] opos = new int[2];
            line.getLocationOnScreen(opos);
            double height = view.getHeight();

            double center = (height / 2);
            for(int ii=0; ii<count; ii++) {
                vi = line.getChildAt(ii);
                int[] position = new int[2];
                vi.getLocationOnScreen(position);

                double dis = center - position[1];
                if(Math.abs(dis )<= Math.abs(closest)) {
                    closest = dis;
                    id = vi.getId();
                    vf = vi;
                }
                //do something with your child element
            }
            view.scrollBy(0, (int)(-1 * closest));
            String desc = (String) vf.getContentDescription();
            ImageView prize = findViewById(R.id.prize);
            int left = 0;
                switch (desc){
                case "cap":
                    eventDate.increaseUsedCaps();
                    left = eventDate.getCapsLeft();
                    if(left == 0){
                        renderSpinner();
                    }
                    prize.setImageResource(R.drawable.win_cap);
                    break;
                case "bottle":
                    eventDate.increaseUsedBottle();
                    left = eventDate.getBottleLeft();
                    if(left == 0){
                        renderSpinner();
                    }
                    prize.setImageResource(R.drawable.win_bottle);
                    break;
                case "card":
                    eventDate.increaseUsedHolders();
                    left = eventDate.getHoldersLeft();
                    if(left == 0){
                        renderSpinner();
                    }
                    prize.setImageResource(R.drawable.win_holder);
                    break;
                case "glasses":
                    eventDate.increaseUsedGlasses();
                    left = eventDate.getGlassesLeft();
                    if(left == 0){
                        renderSpinner();
                    }
                    prize.setImageResource(R.drawable.win_glasses);
                    break;
            }
            animateStartOut();
            animatePrize();
            animateBoostOut();
            updateFirebase();
        }
    };
    @Override
    public void onBackPressed() {
    }
    private int closestAmount(float no){

        float point = no / 250;
        int round = Math.round(point);

        return  (round * 250) ;

    }

    public void animatePrize(){
        ImageView prize = findViewById(R.id.prize);
        ObjectAnimator objectAnimatorX =  ObjectAnimator.ofFloat(prize,"ScaleX",0,1);
        objectAnimatorX.setDuration(300);
        ObjectAnimator objectAnimatorY =  ObjectAnimator.ofFloat(prize,"ScaleY",0,1);
        objectAnimatorY.setDuration(300);
        objectAnimatorY.start();
        objectAnimatorX.start();
    }

    public void animatePrizeOut(){
        ImageView prize = findViewById(R.id.prize);
        ObjectAnimator objectAnimatorX =  ObjectAnimator.ofFloat(prize,"ScaleX",1,0);
        objectAnimatorX.setDuration(300);
        ObjectAnimator objectAnimatorY =  ObjectAnimator.ofFloat(prize,"ScaleY",1,0);
        objectAnimatorY.setDuration(300);
        objectAnimatorY.start();
        objectAnimatorX.start();
    }


    public void animateStartOut(){
        ImageButton sB = findViewById(R.id.startButton);
        ObjectAnimator objectAnimatorX =  ObjectAnimator.ofFloat(sB,"ScaleX",1,0);
        objectAnimatorX.setDuration(300);
        ObjectAnimator objectAnimatorY =  ObjectAnimator.ofFloat(sB,"ScaleY",1,0);
        objectAnimatorY.setDuration(300);
        objectAnimatorY.start();
        objectAnimatorX.start();
        objectAnimatorX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ImageButton sB = findViewById(R.id.startButton);
                sB.setVisibility(View.GONE);
            }
        });
    }

    public void animateStartIn(){
        ImageButton sB = findViewById(R.id.startButton);
        sB.setVisibility(View.VISIBLE);
        ObjectAnimator objectAnimatorX =  ObjectAnimator.ofFloat(sB,"ScaleX",0,1);
        objectAnimatorX.setDuration(300);
        ObjectAnimator objectAnimatorY =  ObjectAnimator.ofFloat(sB,"ScaleY",0,1);
        objectAnimatorY.setDuration(300);
        objectAnimatorY.start();
        objectAnimatorX.start();
    }


    public void animateBoostOut(){
        ImageButton sB = findViewById(R.id.boostButton);
        ObjectAnimator objectAnimatorX =  ObjectAnimator.ofFloat(sB,"ScaleX",1,0);
        objectAnimatorX.setDuration(300);
        ObjectAnimator objectAnimatorY =  ObjectAnimator.ofFloat(sB,"ScaleY",1,0);
        objectAnimatorY.setDuration(300);
        objectAnimatorY.start();
        objectAnimatorX.start();
        objectAnimatorX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ImageButton sB = findViewById(R.id.boostButton);
                sB.setVisibility(View.GONE);
            }
        });
    }

    public void animateBoostIn(){
        ImageButton sB = findViewById(R.id.boostButton);;
        sB.setVisibility(View.VISIBLE);
        ObjectAnimator objectAnimatorX =  ObjectAnimator.ofFloat(sB,"ScaleX",0,1);
        objectAnimatorX.setDuration(300);
        ObjectAnimator objectAnimatorY =  ObjectAnimator.ofFloat(sB,"ScaleY",0,1);
        objectAnimatorY.setDuration(300);
        objectAnimatorY.start();
        objectAnimatorX.start();
    }


    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private Integer getTodayEvent(ArrayList<EventDate> map) throws ParseException {

        int index = -1;
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date today = df.parse(df.format(new Date()));

        for(int i = 0; i < map.size(); i ++) {
            EventDate item = map.get(i);
            String dateString = item.getDate();
            Date date = df.parse(dateString);
            if(today.compareTo(date) == 0)
                index = i;
        }
        return index;
    }


    private void renderSpinner(){
        LinearLayout line = findViewById(R.id.line);
        line.removeAllViews();
        int offset = (int) Math.round(Math.random() * 4);
        int y = (offset > 1)? offset - 1 : 0;
        int count = 0;
        int used = 0;
        Boolean add = false;
        Integer total = eventDate.getTotalLeft();

        if(total > 0) {
            while (count < 120) {
                ImageView iv = new ImageView(this);
                switch (y) {
                    case 0:
                        iv.setImageResource(R.drawable.cap);
                        iv.setContentDescription("cap");
                        used = eventDate.getCapsLeft();
                        if (used > 0) {
                            count++;
                            add = true;
                        } else {
                            add = false;
                        }
                        break;
                    case 1:
                        iv.setImageResource(R.drawable.bottle);
                        iv.setContentDescription("bottle");
                        used = eventDate.getBottleLeft();
                        if (used > 0) {
                            count++;
                            add = true;
                        } else {
                            add = false;
                        }
                        break;
                    case 2:
                        iv.setImageResource(R.drawable.card);
                        iv.setContentDescription("card");
                        used = eventDate.getHoldersLeft();
                        if (used > 0) {
                            count++;
                            add = true;
                        } else {
                            add = false;
                        }
                        break;
                    case 3:
                        iv.setImageResource(R.drawable.glasses);
                        iv.setContentDescription("glasses");
                        used = eventDate.getGlassesLeft();
                        if (used > 0) {
                            count++;
                            add = true;
                        } else {
                            add = false;
                        }
                        break;
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 250);
                params.gravity = Gravity.CENTER;
                iv.setLayoutParams(params);
                if (add)
                    line.addView(iv);
                y++;
                if (y == 4)
                    y = 0;
            }
        }
    }

    private void updateFirebase(){
        location.setSpecificDates(eventIndex,eventDate);
        String id = location.getId();
        Map<String, Object> docData = new HashMap<>();
            docData.put("name",location.getName());
            ArrayList<Map<String, Object>> dated = new ArrayList<>();
        ArrayList<EventDate> dats = location.getDates();
        Iterator<EventDate> dateIte = dats.iterator();
        while (dateIte.hasNext()) {
            EventDate item = dateIte.next();
            Map<String, Object> it = new HashMap<>();
            it.put("date", item.getDate().toString());
            it.put("caps", item.getCaps().toString());
            it.put("cardholders", item.getHolders().toString());
            it.put("glasses", item.getGlasses().toString());
            it.put("rings", item.getBottle().toString());
            it.put("caps_used", item.getUsedCaps().toString());
            it.put("cardholders_used", item.getUsedHolders().toString());
            it.put("glasses_used", item.getUsedGlasses().toString());
            it.put("rings_used", item.getUsedGlasses().toString());
            dated.add(it);
        }

        docData.put("dates",dated);
    if(id != null) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        db.collection("locations").document(id)
                .set(docData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("upload", "Updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("upload", "Error writing document", e);
                    }
                });
        }
    }

}
